// FILE TYPE: Utility Helper
// PURPOSE: Manages persistent user session, JWT token, and settings in SharedPreferences.
// USED BY: AuthRepository, ProfileRepository, ApiClient
// DATA SOURCE: Android SharedPreferences

package com.example.queueup.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit().putString(Constants.KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(Constants.KEY_AUTH_TOKEN, null)
    }

    fun saveUser(
        id: String,
        name: String,
        email: String,
        occupation: String?,
        interests: String?,
        role: String
    ) {
        prefs.edit().apply {
            putString(Constants.KEY_USER_ID, id)
            putString(Constants.KEY_USER_NAME, name)
            putString(Constants.KEY_USER_EMAIL, email)
            putString(Constants.KEY_USER_OCCUPATION, occupation)
            putString(Constants.KEY_USER_INTERESTS, interests)
            putString(Constants.KEY_USER_ROLE, role)
            apply()
        }
    }

    fun getUserId(): String? = prefs.getString(Constants.KEY_USER_ID, null)
    fun getUserName(): String? = prefs.getString(Constants.KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs.getString(Constants.KEY_USER_EMAIL, null)
    fun getUserOccupation(): String? = prefs.getString(Constants.KEY_USER_OCCUPATION, null)
    fun getUserInterests(): String? = prefs.getString(Constants.KEY_USER_INTERESTS, null)
    fun getUserRole(): String? = prefs.getString(Constants.KEY_USER_ROLE, "USER")

    fun isNotificationsEnabled(): Boolean =
        prefs.getBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, true)

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isPrivacyConsentGiven(): Boolean =
        prefs.getBoolean(Constants.KEY_PRIVACY_CONSENT, true)

    fun setPrivacyConsentGiven(given: Boolean) {
        prefs.edit().putBoolean(Constants.KEY_PRIVACY_CONSENT, given).apply()
    }

    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrBlank()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
