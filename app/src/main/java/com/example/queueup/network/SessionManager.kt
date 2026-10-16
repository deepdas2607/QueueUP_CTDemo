// NETWORK LAYER: Manages saving and reading login tokens and user settings in Android SharedPreferences.

package com.example.queueup.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("queueup_user_session", Context.MODE_PRIVATE)

    // Saves the JWT token string when the user logs in.
    fun saveAuthToken(token: String) {
        prefs.edit().putString("auth_token", token).apply()
    }

    // Reads the saved JWT token string, or null if the user is logged out.
    fun getAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }

    // Saves the basic user profile fields into local phone storage.
    fun saveUserProfile(id: String, name: String, email: String, phone: String?, occupation: String?, interests: String?) {
        prefs.edit().apply {
            putString("user_id", id)
            putString("user_name", name)
            putString("user_email", email)
            putString("user_phone", phone ?: "")
            putString("user_occupation", occupation ?: "Student")
            putString("user_interests", interests ?: "Campus Life")
            apply()
        }
    }

    // Reads saved user fields for instant UI display before network responses arrive.
    fun getUserId(): String? = prefs.getString("user_id", null)
    fun getUserName(): String? = prefs.getString("user_name", "Student")
    fun getUserEmail(): String? = prefs.getString("user_email", "")
    fun getUserPhone(): String? = prefs.getString("user_phone", "")
    fun getUserOccupation(): String? = prefs.getString("user_occupation", "Student")
    fun getUserInterests(): String? = prefs.getString("user_interests", "Campus Life")

    // Returns true if a valid auth token is currently saved.
    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrBlank()
    }

    // Reads and writes whether Dark Mode is enabled in the app.
    fun isDarkModeEnabled(): Boolean = prefs.getBoolean("dark_mode", true)
    fun setDarkModeEnabled(enabled: Boolean) = prefs.edit().putBoolean("dark_mode", enabled).apply()

    // Reads and writes whether queue notifications are enabled.
    fun isNotificationsEnabled(): Boolean = prefs.getBoolean("notifications_enabled", true)
    fun setNotificationsEnabled(enabled: Boolean) = prefs.edit().putBoolean("notifications_enabled", enabled).apply()

    // Reads and writes whether analytics privacy consent is given.
    fun isPrivacyConsentGiven(): Boolean = prefs.getBoolean("privacy_consent", true)
    fun setPrivacyConsentGiven(given: Boolean) = prefs.edit().putBoolean("privacy_consent", given).apply()

    // Saves and reads the device Firebase Cloud Messaging (FCM) registration token for push notifications.
    fun saveFcmToken(token: String) = prefs.edit().putString("fcm_token", token).apply()
    fun getFcmToken(): String? = prefs.getString("fcm_token", null)

    // Wipes all saved user tokens and fields when logging out.
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
