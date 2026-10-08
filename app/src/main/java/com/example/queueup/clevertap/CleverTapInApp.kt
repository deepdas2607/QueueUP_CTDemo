// FILE TYPE: CleverTap In-App Messaging Wrapper
// PURPOSE: Manages In-App notification triggers and callback listeners.
// USED BY: Future QueueUp In-App Campaign Integration.
// CURRENT STATUS: Standalone module; not connected to QueueUp Core App.

package com.example.queueup.clevertap

import com.clevertap.android.sdk.InAppNotificationButtonListener

object CleverTapInApp {

    fun registerInAppButtonListener(listener: InAppNotificationButtonListener) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        cleverTap?.setInAppNotificationButtonListener(listener)
    }

    fun suspendInAppNotifications() {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        cleverTap?.suspendInAppNotifications()
    }

    fun resumeInAppNotifications() {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        cleverTap?.resumeInAppNotifications()
    }
}
