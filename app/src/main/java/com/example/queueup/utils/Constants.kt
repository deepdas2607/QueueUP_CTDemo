// FILE TYPE: Utility Constant File
// PURPOSE: Holds application-wide constants such as API endpoints and Preference keys.
// USED BY: ApiClient, SessionManager, NotificationService
// DATA SOURCE: Application Configuration

package com.example.queueup.utils

object Constants {
    // 10.0.2.2 points to localhost when running inside Android Emulator
    const val BASE_URL = "http://10.0.2.2:3000/api/"

    const val PREF_NAME = "queueup_prefs"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_OCCUPATION = "user_occupation"
    const val KEY_USER_INTERESTS = "user_interests"
    const val KEY_USER_ROLE = "user_role"
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val KEY_PRIVACY_CONSENT = "privacy_consent"

    const val CHANNEL_ID_QUEUE = "queue_status_channel"
    const val CHANNEL_NAME_QUEUE = "Queue Status Updates"
}
