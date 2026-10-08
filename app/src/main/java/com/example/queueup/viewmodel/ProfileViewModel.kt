// FILE TYPE: ViewModel
// PURPOSE: Holds UI state for Profile screen (user details, queue statistics, notifications & privacy toggles).
// USED BY: ProfileScreen
// CALLS: ProfileRepository, AuthRepository

package com.example.queueup.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.queueup.data.model.User
import com.example.queueup.data.model.UserStats
import com.example.queueup.data.repository.AuthRepository
import com.example.queueup.data.repository.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var user by mutableStateOf<User?>(null)
    var userStats by mutableStateOf<UserStats?>(null)

    var notificationsEnabled by mutableStateOf(true)
    var privacyConsentGiven by mutableStateOf(true)

    var editName by mutableStateOf("")
    var editOccupation by mutableStateOf("")
    var editInterests by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    fun loadProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            notificationsEnabled = profileRepository.isNotificationsEnabled()
            privacyConsentGiven = profileRepository.isPrivacyConsentGiven()

            val result = profileRepository.getProfile()
            isLoading = false
            result.onSuccess { profile ->
                user = profile.user
                userStats = profile.stats
                editName = profile.user.name
                editOccupation = profile.user.occupation ?: ""
                editInterests = profile.user.interests ?: ""
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Failed to load profile"
            }
        }
    }

    fun updateProfileInfo() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = profileRepository.updateProfile(
                name = editName.ifBlank { null },
                occupation = editOccupation.ifBlank { null },
                interests = editInterests.ifBlank { null }
            )
            isLoading = false
            result.onSuccess { updatedUser ->
                user = updatedUser
                successMessage = "Profile updated successfully!"
            }.onFailure { exception ->
                errorMessage = exception.message ?: "Failed to update profile"
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        notificationsEnabled = enabled
        profileRepository.setNotificationsEnabled(enabled)
    }

    fun togglePrivacyConsent(given: Boolean) {
        privacyConsentGiven = given
        profileRepository.setPrivacyConsentGiven(given)
    }

    fun logout(onLogoutDone: () -> Unit) {
        authRepository.logout()
        user = null
        userStats = null
        onLogoutDone()
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}
