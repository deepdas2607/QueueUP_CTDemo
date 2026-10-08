// FILE TYPE: Data Model
// PURPOSE: Data representation of a Queue Entry and its status payloads.
// USED BY: QueueRepository, QueueViewModel, QueueScreen, QueueHistoryScreen
// DATA SOURCE: Express REST API / Database QueueEntry Table

package com.example.queueup.data.model

enum class QueueStatus {
    WAITING,
    SERVED,
    CANCELLED
}

data class QueueEntry(
    val id: String,
    val userId: String,
    val serviceId: String,
    val position: Int,
    val status: QueueStatus,
    val joinedAt: String,
    val servedAt: String? = null,
    val cancelledAt: String? = null,
    val service: Service
)

data class ActiveQueueData(
    val queueEntry: QueueEntry,
    val peopleAhead: Int,
    val estimatedWaitMinutes: Int
)

data class ActiveQueueResponse(
    val activeQueue: ActiveQueueData?
)

data class JoinQueueRequest(
    val serviceId: String
)

data class JoinQueueResponse(
    val queueEntry: QueueEntry,
    val estimatedWaitMinutes: Int,
    val peopleAhead: Int
)
