// FILE TYPE: CleverTap Events Wrapper
// PURPOSE: Centralizes event names and property tracking for QueueUp user journeys.
// USED BY: Future QueueUp integration point.
// CURRENT STATUS: Standalone module; not called by QueueUp screens or business logic.

package com.example.queueup.clevertap

import java.util.Calendar
import java.util.Date

object CleverTapEvents {

    // Event Names Catalog (P1 - P3)
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

        // Queue Lifecycle
        const val QUEUE_JOINED = "Queue Joined"
        const val QUEUE_POSITION_VIEWED = "Queue Position Viewed"
        const val QUEUE_REFRESHED = "Queue Refreshed"
        const val QUEUE_POSITION_UPDATED = "Queue Position Updated"
        const val QUEUE_TURN_READY = "Queue Turn Ready"
        const val QUEUE_SERVED = "Queue Served"
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

    /**
     * Core event dispatcher to CleverTap SDK.
     * Enforces privacy consent checks before dispatching.
     */
    fun logEvent(eventName: String, properties: Map<String, Any> = emptyMap()) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            val eventProps = HashMap<String, Any>(properties).apply {
                put("timestamp", Date())
            }
            cleverTap.pushEvent(eventName, eventProps)
        }
    }

    // --- Authentication Events ---

    fun trackUserRegistered(
        userId: String,
        name: String,
        email: String,
        occupation: String?,
        interests: String?
    ) {
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

    fun trackUserLoggedIn(userId: String, email: String, method: String = "Email/Password") {
        logEvent(
            Names.USER_LOGGED_IN,
            mapOf(
                "userId" to userId,
                "email" to email,
                "loginMethod" to method
            )
        )
    }

    fun trackUserLoggedOut(userId: String? = null) {
        logEvent(
            Names.USER_LOGGED_OUT,
            userId?.let { mapOf("userId" to it) } ?: emptyMap()
        )
    }

    // --- App Navigation Events ---

    fun trackAppLaunched() {
        logEvent(Names.APP_LAUNCHED)
    }

    fun trackHomeViewed(hasActiveQueue: Boolean = false) {
        logEvent(
            Names.HOME_VIEWED,
            mapOf("hasActiveQueue" to hasActiveQueue)
        )
    }

    fun trackServiceViewed(serviceName: String, serviceId: String, currentWaitingCount: Int = 0) {
        logEvent(
            Names.SERVICE_VIEWED,
            mapOf(
                "serviceName" to serviceName,
                "serviceId" to serviceId,
                "currentWaitingCount" to currentWaitingCount
            )
        )
    }

    fun trackProfileViewed() {
        logEvent(Names.PROFILE_VIEWED)
    }

    // --- Queue Lifecycle Events ---

    fun trackQueueJoined(
        serviceName: String,
        serviceId: String,
        queuePosition: Int,
        estimatedWaitMinutes: Int,
        occupation: String? = null,
        hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    ) {
        val props = mutableMapOf<String, Any>(
            "serviceName" to serviceName,
            "serviceId" to serviceId,
            "queuePosition" to queuePosition,
            "estimatedWaitMinutes" to estimatedWaitMinutes,
            "hourOfDay" to hourOfDay
        )
        occupation?.let { props["occupation"] = it }
        logEvent(Names.QUEUE_JOINED, props)
    }

    fun trackQueuePositionViewed(serviceName: String, position: Int, peopleAhead: Int, estimatedWaitMinutes: Int) {
        logEvent(
            Names.QUEUE_POSITION_VIEWED,
            mapOf(
                "serviceName" to serviceName,
                "queuePosition" to position,
                "peopleAhead" to peopleAhead,
                "estimatedWaitMinutes" to estimatedWaitMinutes
            )
        )
    }

    fun trackQueueRefreshed(serviceName: String, queuePosition: Int, peopleAhead: Int) {
        logEvent(
            Names.QUEUE_REFRESHED,
            mapOf(
                "serviceName" to serviceName,
                "queuePosition" to queuePosition,
                "peopleAhead" to peopleAhead
            )
        )
    }

    fun trackQueuePositionUpdated(
        serviceName: String,
        serviceId: String,
        previousPosition: Int,
        newPosition: Int,
        peopleAhead: Int,
        estimatedWaitMinutes: Int
    ) {
        logEvent(
            Names.QUEUE_POSITION_UPDATED,
            mapOf(
                "serviceName" to serviceName,
                "serviceId" to serviceId,
                "previousPosition" to previousPosition,
                "newPosition" to newPosition,
                "peopleAhead" to peopleAhead,
                "estimatedWaitMinutes" to estimatedWaitMinutes
            )
        )
    }

    fun trackQueueTurnReady(
        serviceName: String,
        serviceId: String,
        tokenNumber: Int,
        totalWaitDurationMinutes: Int = 0
    ) {
        logEvent(
            Names.QUEUE_TURN_READY,
            mapOf(
                "serviceName" to serviceName,
                "serviceId" to serviceId,
                "tokenNumber" to tokenNumber,
                "totalWaitDurationMinutes" to totalWaitDurationMinutes
            )
        )
    }

    fun trackQueueServed(
        serviceName: String,
        serviceId: String,
        totalWaitMinutes: Int
    ) {
        logEvent(
            Names.QUEUE_SERVED,
            mapOf(
                "serviceName" to serviceName,
                "serviceId" to serviceId,
                "totalWaitMinutes" to totalWaitMinutes
            )
        )
    }

    fun trackQueueLeft(serviceName: String, queuePosition: Int, elapsedMinutes: Int = 0) {
        logEvent(
            Names.QUEUE_LEFT,
            mapOf(
                "serviceName" to serviceName,
                "queuePosition" to queuePosition,
                "elapsedMinutes" to elapsedMinutes
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

    // --- Engagement & Consent Events ---

    fun trackNotificationPermissionPromptShown() {
        logEvent(Names.NOTIFICATION_PERMISSION_PROMPT_SHOWN)
    }

    fun trackNotificationEnabled() {
        logEvent(Names.NOTIFICATION_ENABLED)
    }

    fun trackNotificationDisabled() {
        logEvent(Names.NOTIFICATION_DISABLED)
    }

    fun trackConsentUpdated(analyticsConsent: Boolean, personalizationConsent: Boolean) {
        logEvent(
            Names.CONSENT_UPDATED,
            mapOf(
                "analyticsConsent" to analyticsConsent,
                "personalizationConsent" to personalizationConsent
            )
        )
    }

    // --- Milestone Events ---

    fun trackFirstQueueJoined(serviceName: String) {
        logEvent(Names.FIRST_QUEUE_JOINED, mapOf("serviceName" to serviceName))
    }

    fun trackFirstQueueCompleted(serviceName: String) {
        logEvent(Names.FIRST_QUEUE_COMPLETED, mapOf("serviceName" to serviceName))
    }

    fun trackMultipleQueuesJoined(count: Int) {
        logEvent(Names.MULTIPLE_QUEUES_JOINED, mapOf("totalCount" to count))
    }
}
