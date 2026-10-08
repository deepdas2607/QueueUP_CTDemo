// FILE TYPE: ViewModel
// PURPOSE: Holds UI state for the Home Dashboard screen (services list, active queue snippet, tips).
// USED BY: HomeScreen
// CALLS: ServiceRepository, QueueRepository, ProfileRepository

package com.example.queueup.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.queueup.data.model.ActiveQueueData
import com.example.queueup.data.model.Service
import com.example.queueup.data.repository.ProfileRepository
import com.example.queueup.data.repository.QueueRepository
import com.example.queueup.data.repository.ServiceRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val serviceRepository: ServiceRepository,
    private val queueRepository: QueueRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    var services by mutableStateOf<List<Service>>(emptyList())
    var activeQueue by mutableStateOf<ActiveQueueData?>(null)
    var queueTips by mutableStateOf<List<String>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun loadHomeData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val servicesResult = serviceRepository.getServices()
            servicesResult.onSuccess { list ->
                services = list
            }.onFailure {
                errorMessage = "Failed to load services"
            }

            val activeQueueResult = queueRepository.getMyActiveQueue()
            activeQueueResult.onSuccess { data ->
                activeQueue = data
            }

            val tipsResult = profileRepository.getQueueTips()
            tipsResult.onSuccess { tips ->
                queueTips = tips
            }

            isLoading = false
        }
    }
}
