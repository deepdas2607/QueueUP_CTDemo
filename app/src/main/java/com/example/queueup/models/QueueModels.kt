// MODEL LAYER (M in MVVM): Data classes for Queue tickets, live queue position, and join requests.

package com.example.queueup.models

// Possible states of a user's queue ticket in the database.
enum class QueueStatus {
    WAITING,
    SERVED,
    CANCELLED
}

// Represents a single queue ticket linking a user to a campus service counter.
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

// Holds the user's active queue ticket along with how many people are ahead and estimated wait minutes.
data class ActiveQueueData(
    val queueEntry: QueueEntry,
    val peopleAhead: Int,
    val estimatedWaitMinutes: Int
)

// Wrapper response returned by GET /api/queues/my-active and POST /api/queues/{id}/refresh.
data class ActiveQueueResponse(
    val activeQueue: ActiveQueueData?
)

// Request body sent to POST /api/queues/join when joining a counter line.
data class JoinQueueRequest(
    val serviceId: String
)
