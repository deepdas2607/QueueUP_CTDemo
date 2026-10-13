// BRIDGE LAYER: Connects QueueUp app actions and lifecycles to CleverTap analytics.

package com.example.queueup.clevertap

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
    fun onQueueServed(serviceId: String, serviceName: String) {
        CleverTapEvents.trackQueueServed(
            serviceName = serviceName,
            serviceId = serviceId
        )
        // Increment completed queues counter on profile
        CleverTapProfile.incrementQueuesCompleted()
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
        paymentMode: String = "Campus Card"
    ) {
        val item = mapOf<String, Any>(
            "Service name" to serviceName,
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
}
