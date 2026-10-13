// VIEWMODEL LAYER (VM in MVVM): Holds real-time activity logs, JSON payloads, and announcements for live inspection.

package com.example.queueup.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.queueup.models.ActivityLogCategory
import com.example.queueup.models.ActivityLogEntry
import com.example.queueup.models.CampusAnnouncement
import com.example.queueup.network.SessionManager
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ActivityInspectorViewModel(private val sessionManager: SessionManager) : ViewModel() {

    // Observable list of tracked events, profile updates, and system payloads (newest first).
    val activityLogs = mutableStateListOf<ActivityLogEntry>()

    // Observable list of campus announcements shown in the bell icon sheet.
    val announcements = mutableStateListOf(
        CampusAnnouncement(
            id = "ann_1",
            title = "Bookstore Counter Express Lane Open",
            message = "Wait time is currently under 4 minutes for textbook & stationery pickup.",
            tag = "Counter Update",
            timeAgo = "Just now",
            isUnread = true
        ),
        CampusAnnouncement(
            id = "ann_2",
            title = "Digital Queue Tokens Enabled Across 12 Counters",
            message = "Join any campus service line remotely and get alerted when you reach #1.",
            tag = "Campus Notice",
            timeAgo = "15m ago",
            isUnread = true
        ),
        CampusAnnouncement(
            id = "ann_3",
            title = "Financial Aid Extended Hours",
            message = "Scholarship & grant verification desk remains open until 5:30 PM today.",
            tag = "Schedule",
            timeAgo = "1h ago",
            isUnread = false
        )
    )

    // Custom test event input fields on the Live Activity & Payload Inspector screen.
    var customEventName by mutableStateOf("Queue_Feedback_Submitted")
    var customPropertyKey by mutableStateOf("rating_score")
    var customPropertyValue by mutableStateOf("5")
    var selectedFilter by mutableStateOf<ActivityLogCategory?>(null)

    init {
        logSystemEvent(
            actionName = "App_Launched",
            summary = "QueueUp initialized with local session check",
            properties = mapOf(
                "platform" to "Android Native",
                "logged_in" to sessionManager.isLoggedIn(),
                "dark_mode_enabled" to sessionManager.isDarkModeEnabled()
            )
        )
    }

    // Records a user behavioral event (e.g., Queue_Joined, Service_Viewed) with its JSON payload.
    fun logEvent(eventName: String, summary: String, properties: Map<String, Any?>) {
        addLogEntry(ActivityLogCategory.EVENT, eventName, summary, properties)
    }

    // Records an identity resolution call (e.g., onUserLogin) when a user signs in or registers.
    fun logIdentityLogin(actionName: String, summary: String, profileMap: Map<String, Any?>) {
        addLogEntry(ActivityLogCategory.IDENTITY, actionName, summary, profileMap)
    }

    // Records a profile attribute update (e.g., pushProfile) when user details or settings change.
    fun logProfileUpdate(actionName: String, summary: String, profileMap: Map<String, Any?>) {
        addLogEntry(ActivityLogCategory.PROFILE, actionName, summary, profileMap)
    }

    // Records a system lifecycle or diagnostic event.
    fun logSystemEvent(actionName: String, summary: String, properties: Map<String, Any?>) {
        addLogEntry(ActivityLogCategory.SYSTEM, actionName, summary, properties)
    }

    // Fires a custom test event from the Inspector screen so you can test any event name & property live.
    fun triggerCustomTestEvent() {
        if (customEventName.isBlank()) return
        val props = mutableMapOf<String, Any?>(
            "triggered_from" to "Activity_Inspector",
            "timestamp_epoch" to System.currentTimeMillis() / 1000
        )
        if (customPropertyKey.isNotBlank()) {
            val trimmedVal = customPropertyValue.trim()
            val parsedVal: Any = trimmedVal.toIntOrNull()
                ?: trimmedVal.toDoubleOrNull()
                ?: (if (trimmedVal.equals("true", true) || trimmedVal.equals("false", true)) trimmedVal.toBoolean() else trimmedVal)
            props[customPropertyKey.trim()] = parsedVal
        }
        logEvent(
            eventName = customEventName.trim(),
            summary = "Custom test event triggered manually",
            properties = props
        )
    }

    // Returns the currently saved user profile formatted as a clean JSON string for inspection.
    fun getCurrentProfileJsonSnapshot(): String {
        val map = linkedMapOf<String, Any?>(
            "Identity" to (sessionManager.getUserId() ?: "anonymous"),
            "Name" to (sessionManager.getUserName() ?: "Guest"),
            "Email" to (sessionManager.getUserEmail() ?: ""),
            "Phone" to (sessionManager.getUserPhone() ?: ""),
            "Occupation" to (sessionManager.getUserOccupation() ?: "Student"),
            "Interests" to (sessionManager.getUserInterests() ?: "Campus Life"),
            "MSG-push" to sessionManager.isNotificationsEnabled(),
            "Consent_Analytics" to sessionManager.isPrivacyConsentGiven(),
            "Dark_Mode" to sessionManager.isDarkModeEnabled()
        )
        return formatMapAsPrettyJson(map)
    }

    // Marks all campus announcements as read when the user opens the announcements drawer.
    fun markAllAnnouncementsRead() {
        for (i in announcements.indices) {
            announcements[i] = announcements[i].copy(isUnread = false)
        }
    }

    // Clears all recorded logs from the Live Activity & Payload Inspector list.
    fun clearAllLogs() {
        activityLogs.clear()
    }

    // Helper that formats a Map into pretty-printed JSON and prepends it to the activity log list.
    private fun addLogEntry(
        category: ActivityLogCategory,
        actionName: String,
        summary: String,
        properties: Map<String, Any?>
    ) {
        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val entry = ActivityLogEntry(
            id = UUID.randomUUID().toString(),
            category = category,
            actionName = actionName,
            summary = summary,
            timestamp = timeFormat.format(Date()),
            payloadJson = formatMapAsPrettyJson(properties)
        )
        activityLogs.add(0, entry)
    }

    // Converts a Kotlin key-value Map into a 2-space indented JSON string.
    private fun formatMapAsPrettyJson(data: Map<String, Any?>): String {
        return try {
            val json = JSONObject()
            for ((k, v) in data) {
                json.put(k, v ?: JSONObject.NULL)
            }
            json.toString(2)
        } catch (e: Exception) {
            data.toString()
        }
    }
}
