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

class NotificationService(context: Context) {

    private val appContext = context.applicationContext
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    init {
        createNotificationChannel()
    }

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

    fun showQueueUpdateNotification(title: String, message: String) {
        try {
            val notification = NotificationCompat.Builder(appContext, Constants.CHANNEL_ID_QUEUE)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager?.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
