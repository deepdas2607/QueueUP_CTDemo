// FILE TYPE: CleverTap Profile Wrapper
// PURPOSE: Manages pushing user profile attributes to CleverTap.
// USED BY: Future QueueUp integration point.
// CURRENT STATUS: Standalone module; not connected to QueueUp Core App.

package com.example.queueup.clevertap

/**
 * Conceptual profile data model for CleverTap.
 * Decoupled from QueueUp Core User model, ready to accept user data on integration.
 */
data class CleverTapUserProfile(
    val userId: String,
    val name: String,
    val email: String,
    val occupation: String? = null,
    val interests: String? = null,
    val role: String = "USER",
    val totalQueuesJoined: Int = 0,
    val completedQueues: Int = 0,
    val averageWaitMinutes: Int = 0,
    val currentQueueStatus: String? = null,
    val notificationsEnabled: Boolean = true,
    val analyticsConsent: Boolean = true
)

object CleverTapProfile {

    /**
     * Updates CleverTap profile with a structured profile object.
     */
    fun updateProfile(user: CleverTapUserProfile) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            val profileData = HashMap<String, Any>().apply {
                put("Identity", user.userId)
                put("Name", user.name)
                put("Email", user.email)
                put("Occupation", user.occupation ?: "Unspecified")
                put("Interests", user.interests ?: "None")
                put("Role", user.role)
                put("Total Queues Joined", user.totalQueuesJoined)
                put("Completed Queues", user.completedQueues)
                put("Average Waiting Time", user.averageWaitMinutes)
                user.currentQueueStatus?.let { put("Current Queue Status", it) }
                put("Notifications Enabled", user.notificationsEnabled)
                put("Analytics Consent", user.analyticsConsent)
                put("MSG-email", true)
                put("MSG-push", user.notificationsEnabled)
            }
            cleverTap.pushProfile(profileData)
        }
    }

    /**
     * Generic profile update accepting a map of arbitrary user properties.
     */
    fun updateProfile(properties: Map<String, Any>) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            cleverTap.pushProfile(HashMap(properties))
        }
    }

    /**
     * Legacy convenience function for pushing basic profile attributes.
     */
    fun pushUserProfile(
        userId: String,
        name: String,
        email: String,
        occupation: String?,
        interests: String?,
        role: String = "USER",
        notificationsEnabled: Boolean = true
    ) {
        updateProfile(
            CleverTapUserProfile(
                userId = userId,
                name = name,
                email = email,
                occupation = occupation,
                interests = interests,
                role = role,
                notificationsEnabled = notificationsEnabled,
                analyticsConsent = CleverTapConfig.isAnalyticsConsentGiven
            )
        )
    }

    /**
     * Updates queue statistics on the user's CleverTap profile.
     */
    fun updateQueueStats(totalJoined: Int, completed: Int, averageWaitMinutes: Int) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            val statsData = HashMap<String, Any>().apply {
                put("Total Queues Joined", totalJoined)
                put("Completed Queues", completed)
                put("Average Waiting Time", averageWaitMinutes)
            }
            cleverTap.pushProfile(statsData)
        }
    }

    /**
     * Updates active queue status on the user's CleverTap profile.
     */
    fun updateCurrentQueueStatus(status: String) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            val statusData = HashMap<String, Any>().apply {
                put("Current Queue Status", status)
            }
            cleverTap.pushProfile(statusData)
        }
    }
}
