// EVENTS LAYER: Tracks custom behavioral events, screen transitions, charged purchases, and errors.

package com.example.queueup.clevertap

import java.util.Calendar
import java.util.Date

object CleverTapEvents {

    // Standardized event name taxonomy for campus queue workflows
    object Names {
        const val USER_REGISTERED = "User Registered"
        const val USER_LOGGED_IN = "User Logged In"
        const val USER_LOGGED_OUT = "User Logged Out"
        const val APP_LAUNCHED = "App Launched"
        const val HOME_VIEWED = "Home Viewed"
        const val SERVICE_VIEWED = "Service Viewed"
        const val PROFILE_VIEWED = "Profile Viewed"
        const val QUEUE_JOINED = "Queue Joined"
        const val QUEUE_POSITION_UPDATED = "Queue Position Updated"
        const val QUEUE_REFRESHED = "Queue Refreshed"
        const val QUEUE_TURN_READY = "Queue Turn Ready"
        const val QUEUE_SERVED = "Queue Served"
        const val QUEUE_LEFT = "Queue Left"
        const val QUEUE_COMPLETED = "Queue Completed"
        const val CHARGED = "Charged"
    }

    // Dispatches a custom event with key-value properties to CleverTap SDK.
    fun logEvent(eventName: String, properties: Map<String, Any> = emptyMap()) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            val eventProps = HashMap<String, Any>(properties).apply {
                if (!containsKey("timestamp")) {
                    put("timestamp", Date())
                }
            }
            cleverTap.pushEvent(eventName, eventProps)
        }
    }

    // Records a screen change in the app (e.g., Home, Service Detail, Queue Status).
    fun recordScreen(screenName: String) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            cleverTap.recordScreen(screenName)
        }
    }

    // Records an error event in CleverTap for monitoring failures and API issues.
    fun recordError(message: String, code: Int = 500) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null) {
            cleverTap.pushError(message, code)
        }
    }

    // Records a purchase or campus transaction using CleverTap's special Charged event.
    fun recordChargedEvent(
        amount: Double,
        paymentMode: String,
        transactionId: String,
        items: List<Map<String, Any>>
    ) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            val chargeDetails = HashMap<String, Any>().apply {
                put("Amount", amount)
                put("Payment Mode", paymentMode)
                put("Charged ID", transactionId)
                put("Date", Date())
            }

            val itemsList = ArrayList<HashMap<String, Any>>()
            for (item in items) {
                itemsList.add(HashMap(item))
            }

            try {
                cleverTap.pushChargedEvent(chargeDetails, itemsList)
            } catch (e: Exception) {
                // Safe non-blocking fallback
            }
        }
    }

    // Tracks when a student views a specific campus counter.
    fun trackServiceViewed(serviceName: String, serviceId: String, waitingCount: Int) {
        logEvent(
            Names.SERVICE_VIEWED,
            mapOf(
                "serviceName" to serviceName,
                "serviceId" to serviceId,
                "currentWaitingCount" to waitingCount
            )
        )
    }

    // Tracks when a student enters a queue line.
    fun trackQueueJoined(
        serviceName: String,
        serviceId: String,
        queuePosition: Int,
        estimatedWaitMinutes: Int,
        occupation: String? = null
    ) {
        val props = mutableMapOf<String, Any>(
            "serviceName" to serviceName,
            "serviceId" to serviceId,
            "queuePosition" to queuePosition,
            "estimatedWaitMinutes" to estimatedWaitMinutes,
            "hourOfDay" to Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        )
        occupation?.let { props["occupation"] = it }
        logEvent(Names.QUEUE_JOINED, props)
    }

    // Tracks when queue position updates automatically via the 30s ticker or manual refresh.
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

    // Tracks when a student reaches position #1 and their turn is ready at the counter.
    fun trackQueueTurnReady(serviceName: String, serviceId: String, tokenNumber: Int) {
        logEvent(
            Names.QUEUE_TURN_READY,
            mapOf(
                "serviceName" to serviceName,
                "serviceId" to serviceId,
                "tokenNumber" to tokenNumber
            )
        )
    }

    // Tracks when a student is served and checked out at the counter.
    fun trackQueueServed(serviceName: String, serviceId: String) {
        logEvent(
            Names.QUEUE_SERVED,
            mapOf(
                "serviceName" to serviceName,
                "serviceId" to serviceId
            )
        )
    }

    // Tracks when a student voluntarily leaves or cancels their place in line.
    fun trackQueueLeft(serviceName: String, queuePosition: Int) {
        logEvent(
            Names.QUEUE_LEFT,
            mapOf(
                "serviceName" to serviceName,
                "queuePosition" to queuePosition
            )
        )
    }
}
