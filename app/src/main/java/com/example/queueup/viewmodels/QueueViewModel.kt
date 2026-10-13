// VIEWMODEL LAYER (VM in MVVM): Manages Campus Services list, Active Queue ticket, Queue History, and Simulation.

package com.example.queueup.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.queueup.clevertap.QueueCleverTapBridge
import com.example.queueup.models.ActiveQueueData
import com.example.queueup.models.JoinQueueRequest
import com.example.queueup.models.QueueEntry
import com.example.queueup.models.Service
import com.example.queueup.network.ApiClient
import com.example.queueup.network.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

class QueueViewModel(
    private val notificationHelper: NotificationHelper,
    private val inspectorViewModel: ActivityInspectorViewModel
) : ViewModel() {

    // Home screen state: 12 campus services, category filter, active ticket, and tips.
    var services by mutableStateOf<List<Service>>(emptyList())
    var selectedCategory by mutableStateOf("All")
    var activeQueueData by mutableStateOf<ActiveQueueData?>(null)
    var queueTips by mutableStateOf<List<String>>(emptyList())

    // Service Detail and Queue History screen state.
    var selectedService by mutableStateOf<Service?>(null)
    var queueHistory by mutableStateOf<List<QueueEntry>>(emptyList())

    // UI loading and error banner state.
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // Background job for the automatic 30-second queue position advancement ticker.
    private var autoUpdateJob: Job? = null

    // Loads all 12 campus counters, the user's active queue ticket, and campus tips for HomeScreen.
    fun loadHomeDashboardData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val servicesRes = ApiClient.apiService.getAllCampusServices()
                if (servicesRes.isSuccessful) {
                    services = servicesRes.body() ?: emptyList()
                }

                val activeRes = ApiClient.apiService.getActiveQueue()
                if (activeRes.isSuccessful) {
                    activeQueueData = activeRes.body()?.activeQueue
                    if (activeQueueData != null && activeQueueData!!.queueEntry.position > 1) {
                        startAutoQueuePositionPolling()
                    }
                }

                val tipsRes = ApiClient.apiService.getCampusTips()
                if (tipsRes.isSuccessful) {
                    queueTips = tipsRes.body()?.tips ?: emptyList()
                }
            } catch (e: Exception) {
                errorMessage = "Unable to load campus counters. Check backend connection."
            }
            isLoading = false
        }
    }

    // Returns services filtered by the selected category chip on HomeScreen.
    fun getFilteredServices(): List<Service> {
        if (selectedCategory == "All") return services
        return services.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    // Loads details for a specific campus counter when the user opens ServiceDetailScreen.
    fun loadServiceDetail(serviceId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = ApiClient.apiService.getCampusServiceById(serviceId)
                if (response.isSuccessful && response.body() != null) {
                    val service = response.body()!!
                    selectedService = service

                    // Dispatch CleverTap Service Viewed event per catalog schema
                    QueueCleverTapBridge.onServiceViewed(
                        serviceName = service.name,
                        serviceId = service.id,
                        waitingCount = service.currentWaitingCount
                    )

                    inspectorViewModel.logEvent(
                        eventName = "Service_Viewed",
                        summary = "Viewed counter: ${service.name}",
                        properties = mapOf(
                            "service_id" to service.id,
                            "service_name" to service.name,
                            "category" to service.category,
                            "current_waiting_count" to service.currentWaitingCount,
                            "average_service_time_min" to service.averageServiceTime
                        )
                    )
                } else {
                    errorMessage = "Could not load service details"
                    QueueCleverTapBridge.onError("Could not load service details", 404)
                }
            } catch (e: Exception) {
                errorMessage = "Network error while loading service details"
                QueueCleverTapBridge.onError("Network error while loading service details", 500)
            }
            isLoading = false
        }
    }

    // Enters the user into the queue for the selected campus counter and triggers a notification.
    fun joinQueueForService(serviceId: String, onJoinedSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = ApiClient.apiService.joinQueue(JoinQueueRequest(serviceId))
                if (response.isSuccessful && response.body() != null) {
                    val joinedData = response.body()!!
                    activeQueueData = joinedData

                    // Bridge event and profile increment to CleverTap SDK per documentation
                    QueueCleverTapBridge.onQueueJoined(
                        serviceId = joinedData.queueEntry.serviceId,
                        serviceName = joinedData.queueEntry.service.name,
                        initialPosition = joinedData.queueEntry.position,
                        estimatedWaitMinutes = joinedData.estimatedWaitMinutes
                    )

                    notificationHelper.showNotification(
                        title = "Joined Queue: ${joinedData.queueEntry.service.name}",
                        message = "Your Position is #${joinedData.queueEntry.position} (${joinedData.peopleAhead} ahead)"
                    )

                    inspectorViewModel.logEvent(
                        eventName = "Queue_Joined",
                        summary = "Joined ${joinedData.queueEntry.service.name} at Position #${joinedData.queueEntry.position}",
                        properties = mapOf(
                            "ticket_id" to joinedData.queueEntry.id,
                            "service_id" to joinedData.queueEntry.serviceId,
                            "service_name" to joinedData.queueEntry.service.name,
                            "category" to joinedData.queueEntry.service.category,
                            "initial_position" to joinedData.queueEntry.position,
                            "people_ahead" to joinedData.peopleAhead,
                            "estimated_wait_minutes" to joinedData.estimatedWaitMinutes
                        )
                    )
                    isLoading = false
                    startAutoQueuePositionPolling()
                    onJoinedSuccess()
                } else {
                    isLoading = false
                    val errorMsg = parseError(response.errorBody()?.string(), "Failed to join queue")
                    errorMessage = errorMsg
                    QueueCleverTapBridge.onError(errorMsg, 400)
                }
            } catch (e: Exception) {
                isLoading = false
                val errorMsg = "Network error while joining queue"
                errorMessage = errorMsg
                QueueCleverTapBridge.onError(errorMsg, 500)
            }
        }
    }

    // Starts an automatic 30-second timer to advance the queue line and notify the student.
    fun startAutoQueuePositionPolling() {
        if (autoUpdateJob?.isActive == true) return
        autoUpdateJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000L) // Wait 30 seconds between automatic queue movements
                val currentTicket = activeQueueData
                if (currentTicket != null && currentTicket.queueEntry.position > 1) {
                    refreshAndAdvanceQueueTurn()
                } else if (currentTicket == null) {
                    break
                }
            }
        }
    }

    // Cancels the 30-second automatic queue timer when leaving line or checking in.
    fun stopAutoQueuePositionPolling() {
        autoUpdateJob?.cancel()
        autoUpdateJob = null
    }

    // Fetches the user's current active queue ticket from GET /api/queues/my-active.
    fun fetchActiveQueueStatus() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = ApiClient.apiService.getActiveQueue()
                if (response.isSuccessful) {
                    activeQueueData = response.body()?.activeQueue
                    if (activeQueueData != null && activeQueueData!!.queueEntry.position > 1) {
                        startAutoQueuePositionPolling()
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Failed to fetch active queue status"
            }
            isLoading = false
        }
    }

    // Advances the simulated line by 1 turn when the user taps Refresh or when the 30s timer fires.
    fun refreshAndAdvanceQueueTurn() {
        val currentTicketId = activeQueueData?.queueEntry?.id ?: return
        val previousPosition = activeQueueData?.queueEntry?.position ?: 1

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = ApiClient.apiService.refreshQueue(currentTicketId)
                if (response.isSuccessful) {
                    val updated = response.body()?.activeQueue
                    activeQueueData = updated

                    if (updated != null) {
                        val newPos = updated.queueEntry.position

                        // Dispatch CleverTap position update and turn ready event per documentation
                        QueueCleverTapBridge.onQueuePositionUpdated(
                            serviceId = updated.queueEntry.serviceId,
                            serviceName = updated.queueEntry.service.name,
                            previousPosition = previousPosition,
                            newPosition = newPos,
                            peopleAhead = updated.peopleAhead,
                            estimatedWaitMinutes = updated.estimatedWaitMinutes
                        )

                        if (newPos == 1 && updated.peopleAhead == 0) {
                            stopAutoQueuePositionPolling()
                            notificationHelper.showNotification(
                                title = "It's Your Turn! (#1)",
                                message = "Please proceed to ${updated.queueEntry.service.name}"
                            )
                            inspectorViewModel.logEvent(
                                eventName = "Ticket_Called",
                                summary = "Reached #1 at ${updated.queueEntry.service.name}",
                                properties = mapOf(
                                    "ticket_id" to updated.queueEntry.id,
                                    "service_name" to updated.queueEntry.service.name,
                                    "position" to 1,
                                    "status" to "READY_AT_COUNTER"
                                )
                            )
                        } else {
                            if (newPos < previousPosition) {
                                notificationHelper.showNotification(
                                    title = "Queue Position Update (#$newPos)",
                                    message = "You are now #$newPos (${updated.peopleAhead} ahead) at ${updated.queueEntry.service.name}"
                                )
                            }
                            inspectorViewModel.logEvent(
                                eventName = "Queue_Position_Updated",
                                summary = "Moved from #$previousPosition to #$newPos at ${updated.queueEntry.service.name}",
                                properties = mapOf(
                                    "ticket_id" to updated.queueEntry.id,
                                    "service_name" to updated.queueEntry.service.name,
                                    "previous_position" to previousPosition,
                                    "new_position" to newPos,
                                    "people_ahead" to updated.peopleAhead,
                                    "estimated_wait_minutes" to updated.estimatedWaitMinutes
                                )
                            )
                        }
                    }
                    // Also refresh the services list counts in the background.
                    val servicesRes = ApiClient.apiService.getAllCampusServices()
                    if (servicesRes.isSuccessful) {
                        services = servicesRes.body() ?: services
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Failed to refresh queue status"
                QueueCleverTapBridge.onError("Failed to refresh queue status", 500)
            }
            isLoading = false
        }
    }

    // Completes the active queue ticket when the user finishes their turn at the counter.
    fun completeActiveQueue(onCompleted: () -> Unit = {}) {
        stopAutoQueuePositionPolling()
        val currentData = activeQueueData ?: return
        val ticketId = currentData.queueEntry.id
        val serviceName = currentData.queueEntry.service.name

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = ApiClient.apiService.completeQueue(ticketId)
                if (response.isSuccessful) {
                    activeQueueData = null

                    // Dispatch CleverTap Queue Served and Charged events per documentation
                    QueueCleverTapBridge.onQueueServed(
                        serviceId = currentData.queueEntry.serviceId,
                        serviceName = serviceName,
                        ticketId = ticketId
                    )

                    val chargedTxnId = "TXN-${ticketId.take(8).uppercase()}"
                    notificationHelper.showNotification(
                        title = "Service Completed",
                        message = "Checked out from $serviceName"
                    )
                    inspectorViewModel.logEvent(
                        eventName = "Service_Completed",
                        summary = "Completed service at $serviceName",
                        properties = mapOf(
                            "ticket_id" to ticketId,
                            "service_name" to serviceName,
                            "final_status" to "SERVED"
                        )
                    )
                    inspectorViewModel.logEvent(
                        eventName = "Charged",
                        summary = "Campus checkout transaction at $serviceName ($chargedTxnId)",
                        properties = mapOf(
                            "Charged ID" to chargedTxnId,
                            "Payment Mode" to "Campus Digital Card",
                            "Service" to serviceName
                        )
                    )
                    loadHomeDashboardData()
                    onCompleted()
                }
            } catch (e: Exception) {
                errorMessage = "Failed to complete service"
                QueueCleverTapBridge.onError("Failed to complete service", 500)
            }
            isLoading = false
        }
    }

    // Cancels and leaves the active queue ticket when the user taps Leave Queue.
    fun leaveActiveQueue(onLeftQueue: () -> Unit = {}) {
        stopAutoQueuePositionPolling()
        val currentData = activeQueueData ?: return
        val ticketId = currentData.queueEntry.id
        val serviceName = currentData.queueEntry.service.name
        val positionWhenLeft = currentData.queueEntry.position

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = ApiClient.apiService.leaveQueue(ticketId)
                if (response.isSuccessful) {
                    activeQueueData = null

                    // Dispatch CleverTap Queue Left event per documentation
                    QueueCleverTapBridge.onQueueLeft(
                        serviceName = serviceName,
                        queuePosition = positionWhenLeft
                    )

                    notificationHelper.showNotification(
                        title = "Left Queue",
                        message = "You left the line for $serviceName"
                    )
                    inspectorViewModel.logEvent(
                        eventName = "Queue_Left",
                        summary = "Cancelled ticket at $serviceName while at #$positionWhenLeft",
                        properties = mapOf(
                            "ticket_id" to ticketId,
                            "service_name" to serviceName,
                            "position_when_left" to positionWhenLeft,
                            "final_status" to "CANCELLED"
                        )
                    )
                    loadHomeDashboardData()
                    onLeftQueue()
                }
            } catch (e: Exception) {
                errorMessage = "Failed to leave queue"
                QueueCleverTapBridge.onError("Failed to leave queue", 500)
            }
            isLoading = false
        }
    }

    // Loads all past completed and cancelled queue tickets for QueueHistoryScreen.
    fun loadQueueHistory() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = ApiClient.apiService.getQueueHistory()
                if (response.isSuccessful) {
                    queueHistory = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load queue history"
            }
            isLoading = false
        }
    }

    // Cancels any running background ticker job when the ViewModel is destroyed.
    override fun onCleared() {
        super.onCleared()
        stopAutoQueuePositionPolling()
    }

    // Parses the error message string from a backend error JSON body.
    private fun parseError(rawJson: String?, fallback: String): String {
        if (rawJson.isNullOrBlank()) return fallback
        return try {
            JSONObject(rawJson).optString("error", fallback)
        } catch (e: Exception) {
            fallback
        }
    }
}
