// FILE TYPE: CleverTap In-App Messaging Wrapper
// PURPOSE: Manages In-App notification triggers, lifecycle control, and button action listeners.
// USED BY: Future QueueUp In-App Campaign Integration.
// CURRENT STATUS: Standalone module; not connected to QueueUp Core App.

package com.example.queueup.clevertap

import com.clevertap.android.sdk.InAppNotificationButtonListener

/**
 * IN-APP ARCHITECTURE:
 * Dashboard In-App Campaign -> SDK Event Trigger (e.g. "Service Viewed") -> SDK Renders Modal/Cover/Header/Footer natively
 *
 * LIFECYCLE REQUIREMENTS:
 * - ActivityLifecycleCallback is handled automatically by CleverTap SDK when Application class initializes SDK.
 * - During delicate flows (such as during checkout, or active token transitions), call `suspendInAppNotifications()`.
 * - Call `resumeInAppNotifications()` once the user returns to an idle screen (Home/Profile).
 */
object CleverTapInApp {

    /**
     * Registers listener to intercept button click actions from In-App campaigns.
     * Use to handle custom deep links or internal routing.
     */
    fun registerInAppButtonListener(listener: InAppNotificationButtonListener) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        cleverTap?.setInAppNotificationButtonListener(listener)
    }

    /**
     * Suspends in-app notification display (e.g., during critical queue wait countdown).
     */
    fun suspendInAppNotifications() {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        cleverTap?.suspendInAppNotifications()
    }

    /**
     * Resumes in-app notification display once screen transitions back to normal.
     */
    fun resumeInAppNotifications() {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        cleverTap?.resumeInAppNotifications()
    }

    /**
     * Discards any pending in-app notifications in queue.
     */
    fun discardInAppNotifications() {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        cleverTap?.discardInAppNotifications()
    }
}
