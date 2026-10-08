// FILE TYPE: CleverTap Integration Bridge / Adapter
// PURPOSE: Plug-and-play event bridge connecting dynamic queue simulation lifecycles to CleverTap analytics.
// ARCHITECTURAL RULE: 100% Decoupled from QueueUp Core. Core code (UI, ViewModels, Repositories) does NOT import this.
// USAGE: When CleverTap is enabled in production (per the 30-Day Training Plan), this bridge can be invoked
//        from a dedicated analytics middleware or event bus.

package com.example.queueup.clevertap

/**
 * Bridge interface representing queue simulation lifecycle events
 * that map directly to CleverTap Analytics campaigns and user journeys.
 */
object QueueCleverTapBridge {

    /**
     * Called when a student joins a queue counter.
     * Maps to:
     * - Phase P1 Day 3 (Event Schema & User Properties)
     * - Phase P2 Day 7 (Wait Countdown Journey Initiation)
     * - Phase P2 Day 10 (Queue Conversion Funnel Step 2)
     */
    fun onQueueJoined(
        userId: String,
        serviceId: String,
        serviceName: String,
        initialPosition: Int,
        estimatedWaitMinutes: Int,
        userOccupation: String? = null
    ) {
        CleverTapEvents.trackQueueJoined(
            serviceName = serviceName,
            serviceId = serviceId,
            queuePosition = initialPosition,
            estimatedWaitMinutes = estimatedWaitMinutes,
            occupation = userOccupation
        )

        // Increment user's lifetime total joined queues attribute
        CleverTapProfile.incrementQueuesJoined()
    }

    /**
     * Called when queue position advances (via manual Refresh or background campus simulation ticker).
     * Maps to:
     * - Phase P2 Day 9 (Wait-time analytics, Drop-off velocity)
     * - Phase P2 Day 6 (In-App message when position <= 2)
     */
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

        // If user reaches Position 1 (Your Turn!), trigger celebratory turn-ready event
        if (newPosition == 1 && peopleAhead == 0) {
            onTurnReady(
                serviceId = serviceId,
                serviceName = serviceName,
                tokenNumber = 1
            )
        }
    }

    /**
     * Called when it is the student's turn to be served.
     * Maps to:
     * - Phase P1 Day 4 (High-priority Push Notification & App Inbox alert)
     * - Phase P4 Day 17 (SMS / WhatsApp transactional alert via webhook)
     */
    fun onTurnReady(
        serviceId: String,
        serviceName: String,
        tokenNumber: Int,
        totalWaitDurationMinutes: Int = 0
    ) {
        CleverTapEvents.trackQueueTurnReady(
            serviceName = serviceName,
            serviceId = serviceId,
            tokenNumber = tokenNumber,
            totalWaitDurationMinutes = totalWaitDurationMinutes
        )
    }

    /**
     * Called when a student is served at the counter by university staff.
     * Maps to:
     * - Phase P2 Day 8 (RFM Segment update: Recency, Frequency, Milestone)
     * - Phase P2 Day 10 (Funnel Step 4: Completion)
     */
    fun onQueueServed(
        serviceId: String,
        serviceName: String,
        totalWaitMinutes: Int
    ) {
        CleverTapEvents.trackQueueServed(
            serviceName = serviceName,
            serviceId = serviceId,
            totalWaitMinutes = totalWaitMinutes
        )

        CleverTapProfile.incrementQueuesCompleted()
    }

    /**
     * Called when a student leaves / cancels a queue.
     * Maps to:
     * - Phase P2 Day 9 (Abandonment Analysis & Churn Prevention Campaign)
     */
    fun onQueueLeft(
        serviceName: String,
        lastPosition: Int,
        elapsedMinutes: Int = 0
    ) {
        CleverTapEvents.trackQueueLeft(
            serviceName = serviceName,
            queuePosition = lastPosition,
            elapsedMinutes = elapsedMinutes
        )
    }
}
