// BRIDGE LAYER: Connects QueueUp app actions and lifecycles to CleverTap analytics.

package com.example.queueup.clevertap

import com.clevertap.android.sdk.inapp.callbacks.FetchInAppsCallback

object QueueCleverTapBridge {

    // Dispatches Queue Joined event and increments the student's lifetime joined counter in CleverTap.
    fun onQueueJoined(
        serviceId: String,
        serviceName: String,
        initialPosition: Int,
        estimatedWaitMinutes: Int,
        occupation: String? = null
    ) {
        CleverTapEvents.trackQueueJoined(
            serviceName = serviceName,
            serviceId = serviceId,
            queuePosition = initialPosition,
            estimatedWaitMinutes = estimatedWaitMinutes,
            occupation = occupation
        )
        // Increment lifetime counter on user's profile
        CleverTapProfile.incrementQueuesJoined()
    }

    // Dispatches Queue Position Updated event when line advances.
    fun onQueuePositionUpdated(
        serviceId: String,
        serviceName: String,
        previousPosition: Int,
        newPosition: Int,
        peopleAhead: Int,
        estimatedWaitMinutes: Int
    ) {
        CleverTapEvents.trackQueuePositionUpdated(
            serviceName = serviceName,
            serviceId = serviceId,
            previousPosition = previousPosition,
            newPosition = newPosition,
            peopleAhead = peopleAhead,
            estimatedWaitMinutes = estimatedWaitMinutes
        )

        // Trigger Turn Ready event when position reaches #1
        if (newPosition == 1 && peopleAhead == 0) {
            onTurnReady(serviceId, serviceName, tokenNumber = 1)
        }
    }

    // Dispatches Queue Turn Ready event when student reaches the head of the line (#1).
    fun onTurnReady(serviceId: String, serviceName: String, tokenNumber: Int = 1) {
        CleverTapEvents.trackQueueTurnReady(
            serviceName = serviceName,
            serviceId = serviceId,
            tokenNumber = tokenNumber
        )
    }

    // Dispatches Queue Served event and increments lifetime completed counter on profile.
    // Also automatically fires CleverTap's special Charged event for campus counter checkout.
    fun onQueueServed(serviceId: String, serviceName: String, ticketId: String = "") {
        CleverTapEvents.trackQueueServed(
            serviceName = serviceName,
            serviceId = serviceId
        )
        // Increment completed queues counter on profile
        CleverTapProfile.incrementQueuesCompleted()

        // Automatically fire CleverTap Charged event with contextual campus pricing & items
        val (amount, itemName, category) = when {
            serviceName.contains("Canteen", ignoreCase = true) ->
                Triple(8.50, "Pre-order Campus Meal", "Food & Dining")
            serviceName.contains("Bookstore", ignoreCase = true) || serviceName.contains("Stationery", ignoreCase = true) ->
                Triple(15.00, "Textbook & Printing Tokens", "Stationery")
            serviceName.contains("Administration", ignoreCase = true) ->
                Triple(20.00, "Official Transcript & Certificate Fee", "Administration")
            serviceName.contains("Sports", ignoreCase = true) ->
                Triple(5.00, "Sports Complex Day Pass", "Campus Life")
            serviceName.contains("IT Support", ignoreCase = true) ->
                Triple(12.00, "Hardware Diagnostic Fee", "Tech Support")
            else ->
                Triple(10.00, "$serviceName Token Fee", "Campus Services")
        }

        val txnId = if (ticketId.isNotBlank()) "TXN-${ticketId.take(8).uppercase()}" else "TXN-${System.currentTimeMillis() % 100000}"
        onCampusPurchase(
            amount = amount,
            serviceName = serviceName,
            transactionId = txnId,
            paymentMode = "Campus Digital Card",
            itemName = itemName,
            itemCategory = category
        )
    }

    // Dispatches Queue Left event when student cancels or steps out of line.
    fun onQueueLeft(serviceName: String, queuePosition: Int) {
        CleverTapEvents.trackQueueLeft(
            serviceName = serviceName,
            queuePosition = queuePosition
        )
    }

    // Dispatches Service Viewed event when browsing a counter.
    fun onServiceViewed(serviceName: String, serviceId: String, waitingCount: Int) {
        CleverTapEvents.trackServiceViewed(
            serviceName = serviceName,
            serviceId = serviceId,
            waitingCount = waitingCount
        )
    }

    // Records a screen transition in CleverTap.
    fun onScreenViewed(screenName: String) {
        CleverTapEvents.recordScreen(screenName)
    }

    // Records an error in CleverTap.
    fun onError(message: String, code: Int = 500) {
        CleverTapEvents.recordError(message, code)
    }

    // Records a campus purchase/transaction using the special Charged event.
    fun onCampusPurchase(
        amount: Double,
        serviceName: String,
        transactionId: String,
        paymentMode: String = "Campus Digital Card",
        itemName: String = "Campus Service Token",
        itemCategory: String = "Campus Services"
    ) {
        val item = mapOf<String, Any>(
            "Item" to itemName,
            "Product category" to itemCategory,
            "Quantity" to 1,
            "Price" to amount
        )
        CleverTapEvents.recordChargedEvent(
            amount = amount,
            paymentMode = paymentMode,
            transactionId = transactionId,
            items = listOf(item)
        )
    }

    // Forces an immediate real-time sync with CleverTap EU1 server:
    // Pushes Dashboard Refreshed event, flushes network queue immediately, and refreshes In-Apps & Native Display.
    fun onDashboardRefreshed() {
        val cleverTap = CleverTapManager.getInstance().getCleverTapInstance() ?: return

        // 1. Log real-time event to trigger server-side evaluation
        CleverTapEvents.logEvent(
            CleverTapEvents.Names.DASHBOARD_REFRESHED,
            mapOf(
                "action" to "manual_refresh",
                "source" to "home_dashboard"
            )
        )

        // 2. Immediately flush the network queue (bypasses SDK batch timers)
        try {
            cleverTap.flush()
            android.util.Log.d("QueueCleverTapBridge", "cleverTap.flush() executed successfully")
        } catch (e: Exception) {
            android.util.Log.e("QueueCleverTapBridge", "Error executing cleverTap.flush()", e)
        }

        // 3. Request fresh In-App campaigns directly from CleverTap server
        try {
            cleverTap.fetchInApps(object : FetchInAppsCallback {
                override fun onInAppsFetched(isSuccess: Boolean) {
                    android.util.Log.d("QueueCleverTapBridge", "fetchInApps completed: success=$isSuccess")
                }
            })
        } catch (e: Exception) {
            android.util.Log.e("QueueCleverTapBridge", "Error executing fetchInApps", e)
        }

        // 4. Pull any active Native Display units from SDK cache
        try {
            CleverTapNativeDisplay.refreshDisplayUnits(cleverTap)
        } catch (e: Exception) {
            android.util.Log.e("QueueCleverTapBridge", "Error refreshing display units", e)
        }
    }
}

