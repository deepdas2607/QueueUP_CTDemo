// NETWORK & DEVICE HELPER: Displays Android status bar notifications when a user enters a line or when their turn arrives.

package com.example.queueup.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.queueup.R

class NotificationHelper(context: Context) {

    private val appContext = context.applicationContext
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    companion object {
        const val CHANNEL_ID = "queue_updates_channel"
        const val CHANNEL_NAME = "Queue Updates & Turn Alerts"
    }

    init {
        createNotificationChannel()
    }

    // Creates the required Android Notification Channel on Android 8.0+ (API 26+).
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when your turn arrives at campus counters"
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    // Posts an immediate notification in the phone's status bar notification tray.
    fun showNotification(title: String, message: String) {
        try {
            val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            notificationManager?.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
