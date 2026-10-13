// MODEL LAYER (M in MVVM): Data classes for the Live Activity & Payload Inspector and Campus Announcements.

package com.example.queueup.models

// Categories of logs displayed inside the Live Activity & Payload Inspector.
enum class ActivityLogCategory {
    EVENT,
    PROFILE,
    IDENTITY,
    SYSTEM
}

// Represents one tracked action or JSON payload inside the Live Activity & Payload Inspector.
data class ActivityLogEntry(
    val id: String,
    val category: ActivityLogCategory,
    val actionName: String,
    val summary: String,
    val timestamp: String,
    val payloadJson: String
)

// Represents an announcement card shown inside the Campus Announcements drawer.
data class CampusAnnouncement(
    val id: String,
    val title: String,
    val message: String,
    val tag: String,
    val timeAgo: String,
    val isUnread: Boolean = true
)
