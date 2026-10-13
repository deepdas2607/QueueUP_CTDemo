// VIEWMODEL LAYER (VM in MVVM): Manages User Profile details, Queue statistics, and App Preferences.

package com.example.queueup.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.queueup.clevertap.CleverTapConsent
import com.example.queueup.clevertap.CleverTapIdentity
import com.example.queueup.clevertap.CleverTapProfile
import com.example.queueup.clevertap.CleverTapUserProfile
import com.example.queueup.models.UpdateProfileRequest
import com.example.queueup.models.User
import com.example.queueup.models.UserQueueStats
import com.example.queueup.network.ApiClient
import com.example.queueup.network.SessionManager
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val sessionManager: SessionManager,
    private val inspectorViewModel: ActivityInspectorViewModel
) : ViewModel() {

    // User profile object and queue statistics (Total Joined, Completed, Cancelled).
    var user by mutableStateOf<User?>(null)
    var userStats by mutableStateOf<UserQueueStats?>(null)

    // Editable text fields on ProfileScreen.
    var editName by mutableStateOf("")
    var editPhone by mutableStateOf("")
    var editOccupation by mutableStateOf("")
    var editInterests by mutableStateOf("")

    // Toggle switches on ProfileScreen.
    var notificationsEnabled by mutableStateOf(true)
    var privacyConsentGiven by mutableStateOf(true)

    // UI loading and feedback banners.
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var statusMessage by mutableStateOf<String?>(null)

    // Fetches the user's profile and queue stats from GET /api/profile.
    fun loadUserProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            statusMessage = null

            notificationsEnabled = sessionManager.isNotificationsEnabled()
            privacyConsentGiven = sessionManager.isPrivacyConsentGiven()

            try {
                val response = ApiClient.apiService.getUserProfile()
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    user = data.user
                    userStats = data.stats
                    editName = data.user.name
                    editPhone = data.user.phone ?: ""
                    editOccupation = data.user.occupation ?: ""
                    editInterests = data.user.interests ?: ""
                } else {
                    errorMessage = "Could not load user profile"
                }
            } catch (e: Exception) {
                errorMessage = "Network error while loading profile"
            }
            isLoading = false
        }
    }

    // Saves edited profile details (Name, Phone, Occupation, Interests) to PUT /api/profile.
    fun saveUpdatedProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            statusMessage = null
            try {
                val response = ApiClient.apiService.updateUserProfile(
                    UpdateProfileRequest(
                        name = editName.trim().ifBlank { null },
                        phone = editPhone.trim(),
                        occupation = editOccupation.trim().ifBlank { null },
                        interests = editInterests.trim().ifBlank { null }
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val updated = response.body()!!
                    user = updated
                    sessionManager.saveUserProfile(
                        id = updated.id,
                        name = updated.name,
                        email = updated.email,
                        phone = updated.phone,
                        occupation = updated.occupation,
                        interests = updated.interests
                    )
                    statusMessage = "Profile updated and synced!"

                    // Push enriched profile attributes to CleverTap SDK per documentation
                    CleverTapProfile.pushProfile(
                        CleverTapUserProfile(
                            userId = updated.id,
                            name = updated.name,
                            email = updated.email,
                            phone = updated.phone,
                            occupation = updated.occupation ?: "Student",
                            interests = listOf(updated.interests ?: "Campus Life"),
                            totalQueuesJoined = userStats?.totalJoined ?: 0,
                            completedQueues = userStats?.completed ?: 0,
                            averageWaitMinutes = 0,
                            notificationsEnabled = notificationsEnabled,
                            analyticsConsent = privacyConsentGiven
                        )
                    )

                    inspectorViewModel.logProfileUpdate(
                        actionName = "pushProfile",
                        summary = "Updated profile attributes for ${updated.name}",
                        profileMap = mapOf(
                            "Identity" to updated.id,
                            "Name" to updated.name,
                            "Email" to updated.email,
                            "Phone" to (updated.phone ?: ""),
                            "Occupation" to (updated.occupation ?: "Student"),
                            "Interests" to (updated.interests ?: "Campus Life"),
                            "Total_Queues_Joined" to (userStats?.totalJoined ?: 0)
                        )
                    )
                } else {
                    errorMessage = "Failed to update profile"
                }
            } catch (e: Exception) {
                errorMessage = "Network error while saving profile"
            }
            isLoading = false
        }
    }

    // Updates push notification preference and logs the profile property change.
    fun togglePushNotifications(enabled: Boolean) {
        notificationsEnabled = enabled
        sessionManager.setNotificationsEnabled(enabled)
        // Sync communication flag with CleverTap
        CleverTapProfile.updateCommunicationPreferences(
            pushEnabled = enabled,
            emailEnabled = true
        )
        inspectorViewModel.logProfileUpdate(
            actionName = "pushProfile (MSG-push)",
            summary = if (enabled) "Subscribed to push notifications" else "Muted push notifications",
            profileMap = mapOf("MSG-push" to enabled)
        )
    }

    // Updates analytics privacy consent and logs the opt-in/opt-out state.
    fun togglePrivacyConsent(given: Boolean) {
        privacyConsentGiven = given
        sessionManager.setPrivacyConsentGiven(given)
        // Sync optOut status with CleverTap SDK
        CleverTapConsent.updateConsent(analyticsConsent = given)
        inspectorViewModel.logProfileUpdate(
            actionName = "setOptOut / Privacy Consent",
            summary = if (given) "Analytics tracking enabled (Opt-In)" else "Analytics tracking paused (Opt-Out)",
            profileMap = mapOf(
                "Consent_Analytics" to given,
                "optOut" to !given
            )
        )
    }

    // Logs out the user, clears saved tokens, and navigates back to LoginScreen.
    fun logoutUser(onLogoutDone: () -> Unit) {
        val currentDark = sessionManager.isDarkModeEnabled()
        // Record logout event on CleverTap
        CleverTapIdentity.onUserLogout()
        inspectorViewModel.logSystemEvent(
            actionName = "User_Logged_Out",
            summary = "Session cleared for ${user?.email ?: "user"}",
            properties = mapOf("user_id" to (user?.id ?: ""))
        )
        sessionManager.clearSession()
        sessionManager.setDarkModeEnabled(currentDark)
        user = null
        userStats = null
        onLogoutDone()
    }
}
