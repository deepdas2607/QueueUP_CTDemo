// FILE TYPE: Notification Helper
// PURPOSE: Initializes Android Notification Channels and shows system notifications for QueueUp events.
// USED BY: MainActivity, QueueViewModel
// DATA SOURCE: Android NotificationManager (Local System Notifications - NO CleverTap code)

package com.example.queueup.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.queueup.R
import com.example.queueup.utils.Constants

/**
 * NotificationService manages Android local system status bar notifications.
 *
 * It is completely decoupled from any external marketing SDKs:
 * - Creates an Android NotificationChannel required for Android 8.0+ (API 26+).
 * - Dispatches local notifications when the student enters a queue or their turn arrives.
 */
class NotificationService(context: Context) {

    // Retaining ApplicationContext prevents leaking Activity instances
    private val appContext = context.applicationContext
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    init {
        // Channels must be created before any notification can be displayed on Android 8.0+
        createNotificationChannel()
    }

    /**
     * Creates the system NotificationChannel for queue alerts.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_ID_QUEUE,
                Constants.CHANNEL_NAME_QUEUE,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for queue position and turn updates"
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Displays an immediate notification in the Android status bar tray.
     * @param title Header text (e.g., "Joined Queue").
     * @param message Body text showing ticket number and position.
     */
    fun showQueueUpdateNotification(title: String, message: String) {
        try {
            val notification = NotificationCompat.Builder(appContext, Constants.CHANNEL_ID_QUEUE)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true) // Dismisses the notification when tapped
                .build()

            // Using timestamp as unique ID ensures each status update is displayed
            notificationManager?.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
