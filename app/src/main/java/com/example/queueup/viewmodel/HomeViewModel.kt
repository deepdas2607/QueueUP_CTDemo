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

/**
 * HomeViewModel coordinates data aggregation for the main dashboard (HomeScreen).
 *
 * In a production Android app, dashboards frequently aggregate data from multiple independent services:
 * - ServiceRepository: retrieves the list of available campus counters (e.g. Administration, Library, IT Support).
 * - QueueRepository: checks if the logged-in student has an active ticket running right now.
 * - ProfileRepository: retrieves dynamic campus tips (e.g., peak queue hours, quiet study zones).
 *
 * All three requests are performed asynchronously in a single coroutine lifecycle to provide a unified loading state.
 */
class HomeViewModel(
    private val serviceRepository: ServiceRepository,
    private val queueRepository: QueueRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // Observable Compose state for the 12 campus counters
    var services by mutableStateOf<List<Service>>(emptyList())

    // Observable Compose state for active queue banner (null if student isn't in any queue)
    var activeQueue by mutableStateOf<ActiveQueueData?>(null)

    // Observable Compose state for helpful queue guidance tips
    var queueTips by mutableStateOf<List<String>>(emptyList())

    // UI Loading and Error status
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    /**
     * loadHomeData() fetches all dashboard components in a single background coroutine.
     * Called automatically by HomeScreen's `LaunchedEffect(Unit)` when the user opens the app.
     */
    fun loadHomeData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            // 1. Fetch campus counters
            val servicesResult = serviceRepository.getServices()
            servicesResult.onSuccess { list ->
                services = list
            }.onFailure {
                errorMessage = "Failed to load services"
            }

            // 2. Fetch active queue status
            val activeQueueResult = queueRepository.getMyActiveQueue()
            activeQueueResult.onSuccess { data ->
                activeQueue = data
            }

            // 3. Fetch campus tips
            val tipsResult = profileRepository.getQueueTips()
            tipsResult.onSuccess { tips ->
                queueTips = tips
            }

            isLoading = false
        }
    }
}
