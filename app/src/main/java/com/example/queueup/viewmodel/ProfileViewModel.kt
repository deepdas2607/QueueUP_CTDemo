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

/**
 * ProfileViewModel manages user profile information, queuing statistics, and student preferences.
 *
 * It powers the ProfileScreen where students can:
 * - View their personal details (Name, Email, Role, Occupation).
 * - Inspect queuing statistics (Total Queues Joined, Total Wait Time, Active Tickets).
 * - Edit their display name and academic interests.
 * - Manage device push notification preferences and data privacy consent settings.
 * - Log out securely.
 */
class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // User model and queue statistics
    var user by mutableStateOf<User?>(null)
    var userStats by mutableStateOf<UserStats?>(null)

    // Device notification and privacy preferences
    var notificationsEnabled by mutableStateOf(true)
    var privacyConsentGiven by mutableStateOf(true)

    // Editable form fields
    var editName by mutableStateOf("")
    var editOccupation by mutableStateOf("")
    var editInterests by mutableStateOf("")

    // UI feedback state
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    /**
     * Loads student profile and historical statistics from the backend REST API.
     * Also reads locally persisted notification and privacy preferences from SharedPreferences.
     */
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

    /**
     * Sends updated name, occupation, or interests to the PostgreSQL database via REST API.
     */
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

    /**
     * Toggles whether the student wants to receive system notifications.
     */
    fun toggleNotifications(enabled: Boolean) {
        notificationsEnabled = enabled
        profileRepository.setNotificationsEnabled(enabled)
    }

    /**
     * Toggles student privacy consent for analytical logging.
     */
    fun togglePrivacyConsent(given: Boolean) {
        privacyConsentGiven = given
        profileRepository.setPrivacyConsentGiven(given)
    }

    /**
     * Logs the student out and clears in-memory state.
     */
    fun logout(onLogoutDone: () -> Unit) {
        authRepository.logout()
        user = null
        userStats = null
        onLogoutDone()
    }

    /**
     * Resets banner messages.
     */
    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}
