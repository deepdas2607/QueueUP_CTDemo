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

class QueueViewModel(
    private val serviceRepository: ServiceRepository,
    private val queueRepository: QueueRepository,
    private val notificationService: NotificationService? = null
) : ViewModel() {

    var selectedService by mutableStateOf<Service?>(null)
    var activeQueueData by mutableStateOf<ActiveQueueData?>(null)
    var queueHistory by mutableStateOf<List<QueueEntry>>(emptyList())

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var actionSuccessMessage by mutableStateOf<String?>(null)

    fun loadServiceDetail(serviceId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = serviceRepository.getServiceById(serviceId)
            isLoading = false
            result.onSuccess { service ->
                selectedService = service
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Failed to load service detail"
            }
        }
    }

    fun joinQueue(serviceId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = queueRepository.joinQueue(serviceId)
            isLoading = false
            result.onSuccess { activeData ->
                activeQueueData = activeData
                actionSuccessMessage = "Joined queue successfully!"
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

    fun clearMessages() {
        errorMessage = null
        actionSuccessMessage = null
    }
}
