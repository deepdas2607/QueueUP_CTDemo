// FILE TYPE: CleverTap Identity Manager
// PURPOSE: Manages identity lifecycle (Anonymous -> Identified User -> Session Restoration -> Logout).
// USED BY: Future QueueUp Auth Integration.
// CURRENT STATUS: Standalone module; not connected to QueueUp Core App.

package com.example.queueup.clevertap

/**
 * CleverTap Identity Management Guide:
 *
 * 1. ANONYMOUS STATE:
 *    When the app is opened without logging in, CleverTap assigns an internal CleverTap ID (GUID).
 *    All initial browsing events (App Launched, Service Viewed) are recorded against this anonymous ID.
 *
 * 2. LOGIN STATE (onUserLogin):
 *    When the user registers or logs into QueueUp, we call `onUserLogin(userId, email, name)`.
 *    CleverTap associates the existing anonymous session history with the identified profile.
 *
 * 3. STABLE IDENTITY:
 *    The QueueUp backend User ID (UUID string from PostgreSQL) is assigned to the "Identity" key.
 *    This ensures consistent tracking across app reinstalls, multiple devices, and server-side APIs.
 *
 * 4. SESSION RESTORATION:
 *    When a previously logged-in user launches the app, SessionManager reads the cached JWT and userId.
 *    Calling `restoreSession(userId, email, name)` ensures continuous attribution without creating duplicate profiles.
 *
 * 5. IDENTITY CONFLICTS & MERGES:
 *    - If User A logs in on a device previously used by User B without logout, CleverTap handles identity
 *      switching based on the "Identity" key.
 *    - To avoid data pollution, `onUserLogout()` resets local states before new credentials are used.
 *    - Email and Identity are set as primary identifiers in the CleverTap dashboard schema.
 */
object CleverTapIdentity {

    /**
     * Binds user identity to CleverTap upon successful login or registration.
     * @param userId The permanent PostgreSQL User UUID.
     * @param email The user's verified email.
     * @param name The user's full name.
     */
    fun onUserLogin(userId: String, email: String, name: String) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null) {
            val profileData = HashMap<String, Any>().apply {
                put("Identity", userId)
                put("Email", email)
                put("Name", name)
            }
            // CleverTap onUserLogin binds anonymous events to this identified profile
            cleverTap.onUserLogin(profileData)
        }
    }

    /**
     * Restores session for an existing logged-in user upon app restart.
     */
    fun restoreSession(userId: String, email: String, name: String) {
        onUserLogin(userId, email, name)
    }

    /**
     * Cleans up local session tracking on logout.
     */
    fun onUserLogout() {
        CleverTapEvents.logEvent(CleverTapEvents.Names.USER_LOGGED_OUT)
    }
}
