// FILE TYPE: CleverTap Profile Wrapper
// PURPOSE: Manages pushing user profile attributes to CleverTap.
// USED BY: Future QueueUp integration point.
// CURRENT STATUS: Standalone module; not connected to QueueUp Core App.

package com.example.queueup.clevertap

object CleverTapProfile {

    fun pushUserProfile(
        userId: String,
        name: String,
        email: String,
        occupation: String?,
        interests: String?,
        role: String = "USER",
        notificationsEnabled: Boolean = true
    ) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            val profileData = HashMap<String, Any>().apply {
                put("Name", name)
                put("Email", email)
                put("Identity", userId)
                put("Occupation", occupation ?: "Unspecified")
                put("Interests", interests ?: "None")
                put("Role", role)
                put("Notifications Enabled", notificationsEnabled)
                put("MSG-email", true)
                put("MSG-push", notificationsEnabled)
            }
            cleverTap.pushProfile(profileData)
        }
    }

    fun updateQueueStats(totalJoined: Int, completed: Int, averageWaitMinutes: Int) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            val statsData = HashMap<String, Any>().apply {
                put("Total Queues Joined", totalJoined)
                put("Completed Queues", completed)
                put("Average Wait Minutes", averageWaitMinutes)
            }
            cleverTap.pushProfile(statsData)
        }
    }
}
