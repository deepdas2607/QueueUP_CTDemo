// FILE TYPE: ViewModel
// PURPOSE: Holds UI state for Service Detail, Active Queue Status, and Queue History screens.
// USED BY: ServiceDetailScreen, QueueStatusScreen, QueueHistoryScreen
// CALLS: ServiceRepository, QueueRepository, NotificationService

package com.example.queueup.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.queueup.data.model.ActiveQueueData
import com.example.queueup.data.model.QueueEntry
import com.example.queueup.data.model.Service
import com.example.queueup.data.repository.QueueRepository
import com.example.queueup.data.repository.ServiceRepository
import com.example.queueup.notifications.NotificationService
import kotlinx.coroutines.launch

/**
 * QueueViewModel is responsible for preparing and managing data for:
 * 1. ServiceDetailScreen (displays counter info and allows joining queue)
 * 2. QueueStatusScreen (displays live ticket number, position #, and dynamic countdown)
 * 3. QueueHistoryScreen (shows past completed or abandoned queue tickets)
 *
 * In Android MVVM architecture, the ViewModel acts as the intermediary between UI (Composable functions)
 * and Data Repositories. It survives screen orientation rotations and prevents network requests from being restarted.
 */
class QueueViewModel(
    private val serviceRepository: ServiceRepository,
    private val queueRepository: QueueRepository,
    private val notificationService: NotificationService? = null
) : ViewModel() {

    // ---------------------------------------------------------------------------------------------
    // Compose State Variables:
    // By using `by mutableStateOf(...)`, any change to these variables triggers automatic
    // recomposition (re-rendering) of only the Composable screens observing them.
    // ---------------------------------------------------------------------------------------------
    var selectedService by mutableStateOf<Service?>(null)
    var activeQueueData by mutableStateOf<ActiveQueueData?>(null)
    var queueHistory by mutableStateOf<List<QueueEntry>>(emptyList())

    // UI Feedback State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var actionSuccessMessage by mutableStateOf<String?>(null)

    /**
     * Loads details for a specific campus service (e.g., description, average wait time).
     * @param serviceId UUID or ID string representing the service in the PostgreSQL database.
     * Called by ServiceDetailScreen on initial launch.
     */
    fun loadServiceDetail(serviceId: String) {
        // viewModelScope.launch starts a Kotlin Coroutine tied to this ViewModel's lifecycle.
        // If the user navigates away, this coroutine is automatically cancelled to prevent memory leaks.
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = serviceRepository.getServiceById(serviceId)
            isLoading = false
            // Kotlin's Result.onSuccess executes only when the HTTP call returned a 200 OK.
            result.onSuccess { service ->
                selectedService = service
            }.onFailure { exception ->
                // Null-coalescing operator `?:` provides a default message if exception.message is null.
                errorMessage = exception.message ?: "Failed to load service detail"
            }
        }
    }

    /**
     * Enters the authenticated student into the queue for the specified campus service.
     * @param serviceId Target service identifier.
     * @param onSuccess Callback lambda invoked only after successful API confirmation, navigating user to QueueStatusScreen.
     */
    fun joinQueue(serviceId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = queueRepository.joinQueue(serviceId)
            isLoading = false
            result.onSuccess { activeData ->
                activeQueueData = activeData
                actionSuccessMessage = "Joined queue successfully!"
                // Safe-call operator `?.` triggers Android status bar notification only if notificationService is non-null.
                notificationService?.showQueueUpdateNotification(
                    title = "Joined Queue",
                    message = "Your position is #${activeData.queueEntry.position} in ${activeData.queueEntry.service.name}"
                )
                onSuccess()
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Failed to join queue"
            }
        }
    }

    /**
     * Fetches the user's current active queue ticket from the backend database.
     * Called by QueueStatusScreen on launch or pull-to-refresh.
     */
    fun fetchActiveQueue() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = queueRepository.getMyActiveQueue()
            isLoading = false
            result.onSuccess { activeData ->
                activeQueueData = activeData
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Failed to fetch active queue"
            }
        }
    }

    /**
     * Refreshes the active queue status to get latest simulated queue position and wait time.
     * Uses Elvis operator `?: return` to exit early if no active queue exists.
     */
    fun refreshActiveQueue() {
        val currentQueueId = activeQueueData?.queueEntry?.id ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = queueRepository.refreshQueue(currentQueueId)
            isLoading = false
            result.onSuccess { activeData ->
                activeQueueData = activeData
                actionSuccessMessage = "Queue status refreshed"
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Failed to refresh queue"
            }
        }
    }

    /**
     * Allows the student to leave/cancel their spot in line.
     * @param onLeftSuccess Callback lambda navigating the student back to the previous screen.
     */
    fun leaveQueue(onLeftSuccess: () -> Unit) {
        val currentQueueId = activeQueueData?.queueEntry?.id ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = queueRepository.leaveQueue(currentQueueId)
            isLoading = false
            result.onSuccess {
                activeQueueData = null
                actionSuccessMessage = "Left the queue"
                notificationService?.showQueueUpdateNotification(
                    title = "Left Queue",
                    message = "You have left the queue"
                )
                onLeftSuccess()
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Failed to leave queue"
            }
        }
    }

    /**
     * Loads the history of past completed and cancelled queues for the current student.
     */
    fun loadQueueHistory() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = queueRepository.getQueueHistory()
            isLoading = false
            result.onSuccess { historyList ->
                queueHistory = historyList
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Failed to load queue history"
            }
        }
    }

    /**
     * Resets error and success banners so they don't linger on the UI after user dismisses them.
     */
    fun clearMessages() {
        errorMessage = null
        actionSuccessMessage = null
    }
}
