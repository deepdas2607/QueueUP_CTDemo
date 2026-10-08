// FILE TYPE: CleverTap Identity Manager
// PURPOSE: Manages identity lifecycle (Anonymous -> Identified User -> Session Restoration -> Logout).
// USED BY: Future QueueUp Auth Integration.
// CURRENT STATUS: Standalone module; not connected to QueueUp Core App.

package com.example.queueup.clevertap

object CleverTapIdentity {

    fun onUserLogin(userId: String, email: String, name: String) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null) {
            val profileData = HashMap<String, Any>().apply {
                put("Identity", userId)
                put("Email", email)
                put("Name", name)
            }
            // CleverTap onUserLogin binds anonymous actions to the identified user profile
            cleverTap.onUserLogin(profileData)
        }
    }

    fun onUserLogout() {
        // Log out or reset session state
        CleverTapEvents.logEvent(CleverTapEvents.Names.USER_LOGGED_OUT)
    }
}
