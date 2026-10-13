// MODEL LAYER (M in MVVM): Data classes for Campus Service Counters and helpful campus tips.

package com.example.queueup.models

// Represents one of the 12 campus service counters (e.g., Library Help Desk, Canteen Counter).
data class Service(
    val id: String,
    val name: String,
    val category: String = "General",
    val description: String,
    val averageServiceTime: Int,
    val currentWaitingCount: Int,
    val isOpen: Boolean = true
)

// Response returned by GET /api/content/queue-tips for the home screen tip banner.
data class QueueTipsResponse(
    val tips: List<String> = emptyList()
)
