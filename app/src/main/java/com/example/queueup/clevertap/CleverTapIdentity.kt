// IDENTITY LAYER: Manages CleverTap onUserLogin, anonymous-to-known user resolution, and session restore.

package com.example.queueup.clevertap

object CleverTapIdentity {

    // Resolves identity on CleverTap SDK when a user registers or logs in with their credentials.
    fun onUserLogin(
        userId: String,
        email: String,
        name: String,
        phone: String? = null,
        occupation: String? = "Student",
        interests: String? = "Campus Life"
    ) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null && CleverTapConfig.isAnalyticsConsentGiven) {
            val profileData = HashMap<String, Any>().apply {
                put("Identity", userId)
                put("Email", email)
                put("Name", name)
                phone?.let { put("Phone", it) }
                occupation?.let { put("Occupation", it) }
                interests?.let { put("Interests", it) }
                put("MSG-push", true)
                put("MSG-email", true)
            }
            // Merges anonymous browsing events with this permanent user profile
            cleverTap.onUserLogin(profileData)
        }
    }

    // Re-authenticates and restores the user's profile on CleverTap when the app restarts from cache.
    fun restoreSession(
        userId: String,
        email: String,
        name: String,
        phone: String? = null,
        occupation: String? = null,
        interests: String? = null
    ) {
        onUserLogin(
            userId = userId,
            email = email,
            name = name,
            phone = phone,
            occupation = occupation,
            interests = interests
        )
    }

    // Records the user logout event in CleverTap.
    fun onUserLogout() {
        CleverTapEvents.logEvent(CleverTapEvents.Names.USER_LOGGED_OUT)
    }
}
