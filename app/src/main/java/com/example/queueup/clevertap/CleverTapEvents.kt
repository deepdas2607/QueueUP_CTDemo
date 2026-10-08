// FILE TYPE: CleverTap Events Wrapper
// PURPOSE: Centralizes event names and property tracking for QueueUp user journeys.
// USED BY: Future QueueUp integration point.
// CURRENT STATUS: Standalone module; not called by QueueUp screens or business logic.

package com.example.queueup.clevertap

import java.util.Date

object CleverTapEvents {

    // Event Names
    object Names {
        // Authentication
        const val USER_REGISTERED = "User Registered"
        const val USER_LOGGED_IN = "User Logged In"
        const val USER_LOGGED_OUT = "User Logged Out"

        // App Navigation
        const val APP_LAUNCHED = "App Launched"
        const val HOME_VIEWED = "Home Viewed"
        const val SERVICE_VIEWED = "Service Viewed"
        const val PROFILE_VIEWED = "Profile Viewed"

        // Queue Actions
        const val QUEUE_JOINED = "Queue Joined"
        const val QUEUE_POSITION_VIEWED = "Queue Position Viewed"
        const val QUEUE_REFRESHED = "Queue Refreshed"
        const val QUEUE_LEFT = "Queue Left"
        const val QUEUE_COMPLETED = "Queue Completed"

        // Engagement & Consent
        const val NOTIFICATION_PERMISSION_PROMPT_SHOWN = "Notification Permission Prompt Shown"
        const val NOTIFICATION_ENABLED = "Notification Enabled"
        const val NOTIFICATION_DISABLED = "Notification Disabled"
        const val CONSENT_UPDATED = "Consent Updated"

        // Milestones
        const val FIRST_QUEUE_JOINED = "First Queue Joined"
        const val FIRST_QUEUE_COMPLETED = "First Queue Completed"
        const val MULTIPLE_QUEUES_JOINED = "Multiple Queues Joined"
    }

    fun logEvent(eventName: String, properties: Map<String, Any> = emptyMap()) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            val eventProps = HashMap<String, Any>(properties).apply {
                put("timestamp", Date())
            }
            cleverTap.pushEvent(eventName, eventProps)
        }
    }

    // Convenience functions for future integration

    fun trackUserRegistered(userId: String, name: String, email: String, occupation: String?, interests: String?) {
        logEvent(
            Names.USER_REGISTERED,
            mapOf(
                "userId" to userId,
                "name" to name,
                "email" to email,
                "occupation" to (occupation ?: "Unspecified"),
                "interests" to (interests ?: "None")
            )
        )
    }

    fun trackQueueJoined(serviceName: String, serviceId: String, queuePosition: Int, estimatedWaitMinutes: Int) {
        logEvent(
            Names.QUEUE_JOINED,
            mapOf(
                "serviceName" to serviceName,
                "serviceId" to serviceId,
                "queuePosition" to queuePosition,
                "estimatedWaitMinutes" to estimatedWaitMinutes
            )
        )
    }

    fun trackQueueLeft(serviceName: String, queuePosition: Int) {
        logEvent(
            Names.QUEUE_LEFT,
            mapOf(
                "serviceName" to serviceName,
                "queuePosition" to queuePosition
            )
        )
    }

    fun trackQueueCompleted(serviceName: String, waitDurationMinutes: Int) {
        logEvent(
            Names.QUEUE_COMPLETED,
            mapOf(
                "serviceName" to serviceName,
                "waitDurationMinutes" to waitDurationMinutes
            )
        )
    }
}
