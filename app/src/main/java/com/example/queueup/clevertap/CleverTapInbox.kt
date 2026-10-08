// FILE TYPE: CleverTap App Inbox Wrapper
// PURPOSE: Prepares App Inbox initialization, message counts, and UI trigger handler.
// USED BY: Future QueueUp Profile / Inbox Integration.
// CURRENT STATUS: Standalone module; not connected to QueueUp Core App.

package com.example.queueup.clevertap

import com.clevertap.android.sdk.CTInboxListener

object CleverTapInbox {

    fun initializeInbox(listener: CTInboxListener? = null) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        cleverTap?.let { ct ->
            if (listener != null) {
                ct.ctNotificationInboxListener = listener
            }
            ct.initializeInbox()
        }
    }

    fun getUnreadCount(): Int {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        return cleverTap?.getInboxMessageUnreadCount() ?: 0
    }
}
