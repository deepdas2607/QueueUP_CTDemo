// FILE TYPE: CleverTap App Inbox Wrapper
// PURPOSE: Prepares App Inbox initialization, message counts, showing UI, and message handling.
// USED BY: Future QueueUp Profile / Inbox Integration.
// CURRENT STATUS: Standalone module; not connected to QueueUp Core App.

package com.example.queueup.clevertap

import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CTInboxStyleConfig

/**
 * APP INBOX ARCHITECTURE:
 * Dashboard Inbox Campaign -> CleverTap Sync -> Local CT Inbox Store -> Native Activity UI
 *
 * FUTURE CONNECTION POINT:
 * In HomeScreen / ProfileScreen TopAppBar:
 * Add an inbox bell icon showing `CleverTapInbox.getUnreadCount()`.
 * On tap: call `CleverTapInbox.showAppInbox()`.
 */
object CleverTapInbox {

    /**
     * Initializes the CleverTap App Inbox subsystem.
     */
    fun initializeInbox(listener: CTInboxListener? = null) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        cleverTap?.let { ct ->
            if (listener != null) {
                ct.ctNotificationInboxListener = listener
            }
            ct.initializeInbox()
        }
    }

    /**
     * Retrieves total count of unread messages in the user's inbox.
     */
    fun getUnreadCount(): Int {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        return cleverTap?.inboxMessageUnreadCount ?: 0
    }

    /**
     * Retrieves total count of all messages in the inbox.
     */
    fun getTotalCount(): Int {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        return cleverTap?.inboxMessageCount ?: 0
    }

    /**
     * Launches the default CleverTap App Inbox activity.
     */
    fun showAppInbox() {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        val styleConfig = CTInboxStyleConfig().apply {
            navBarTitle = "Campus Inbox"
            navBarColor = "#4338CA"
            navBarTitleColor = "#FFFFFF"
            inboxBackgroundColor = "#F8FAFC"
            backButtonColor = "#FFFFFF"
        }
        cleverTap?.showAppInbox(styleConfig)
    }

    /**
     * Marks an inbox message as read.
     */
    fun markMessageAsRead(messageId: String) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        cleverTap?.markReadInboxMessage(messageId)
    }

    /**
     * Deletes an inbox message.
     */
    fun deleteMessage(messageId: String) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        cleverTap?.deleteInboxMessage(messageId)
    }
}
