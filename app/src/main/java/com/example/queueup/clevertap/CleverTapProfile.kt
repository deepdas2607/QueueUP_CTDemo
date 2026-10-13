// PROFILE LAYER: Manages user demographic attributes, communication flags (DND), and counters.

package com.example.queueup.clevertap

import java.util.Date

// Strongly-typed model representing a student user profile for CleverTap.
data class CleverTapUserProfile(
    val userId: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val gender: String? = null, // "M" or "F"
    val dateOfBirth: Date? = null,
    val occupation: String? = "Student",
    val interests: List<String> = listOf("Campus Life"),
    val role: String = "USER",
    val totalQueuesJoined: Int = 0,
    val completedQueues: Int = 0,
    val averageWaitMinutes: Int = 0,
    val currentQueueStatus: String? = null,
    val notificationsEnabled: Boolean = true,
    val analyticsConsent: Boolean = true
)

object CleverTapProfile {

    // Sends rich profile attributes to CleverTap via pushProfile().
    fun pushProfile(user: CleverTapUserProfile) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            val profileData = HashMap<String, Any>().apply {
                put("Identity", user.userId)
                put("Name", user.name)
                put("Email", user.email)
                user.phone?.let { put("Phone", it) }
                user.gender?.let { put("Gender", it) }
                user.dateOfBirth?.let { put("DOB", it) }
                user.occupation?.let { put("Occupation", it) }
                put("Interests", ArrayList(user.interests))
                put("Role", user.role)
                put("Total Queues Joined", user.totalQueuesJoined)
                put("Completed Queues", user.completedQueues)
                put("Average Wait Time", user.averageWaitMinutes)
                user.currentQueueStatus?.let { put("Current Queue Status", it) }

                // Communication flags & DND channels per CleverTap schema
                put("MSG-push", user.notificationsEnabled)
                put("MSG-email", true)
                put("MSG-sms", false)
                put("MSG-whatsapp", false)
                put("MSG-dndPhone", false)
                put("MSG-dndEmail", false)
            }
            cleverTap.pushProfile(profileData)
        }
    }

    // Sends arbitrary key-value profile properties map to CleverTap via pushProfile().
    fun pushProfile(properties: Map<String, Any>) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            cleverTap.pushProfile(HashMap(properties))
        }
    }

    // Increments a numerical profile attribute (e.g., Total Queues Joined).
    fun incrementProfileValue(key: String, value: Number = 1) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            when (value) {
                is Double -> cleverTap.incrementValue(key, value)
                is Float -> cleverTap.incrementValue(key, value.toDouble())
                else -> cleverTap.incrementValue(key, value.toInt())
            }
        }
    }

    // Decrements a numerical profile attribute (e.g., Available Queue Credits).
    fun decrementProfileValue(key: String, value: Number = 1) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            when (value) {
                is Double -> cleverTap.decrementValue(key, value)
                is Float -> cleverTap.decrementValue(key, value.toDouble())
                else -> cleverTap.decrementValue(key, value.toInt())
            }
        }
    }

    // Updates DND (Do Not Disturb) and communication preferences.
    fun updateCommunicationPreferences(
        pushEnabled: Boolean,
        emailEnabled: Boolean,
        dndPhone: Boolean = false,
        dndEmail: Boolean = false
    ) {
        pushProfile(
            mapOf(
                "MSG-push" to pushEnabled,
                "MSG-email" to emailEnabled,
                "MSG-dndPhone" to dndPhone,
                "MSG-dndEmail" to dndEmail
            )
        )
    }

    // Convenience function to increment total joined queues count.
    fun incrementQueuesJoined() {
        incrementProfileValue("Total Queues Joined", 1)
    }

    // Convenience function to increment completed queues count.
    fun incrementQueuesCompleted() {
        incrementProfileValue("Completed Queues", 1)
    }
}
