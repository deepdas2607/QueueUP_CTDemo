// FILE TYPE: CleverTap Push Notification Handler
// PURPOSE: Handles FCM Push Token registration, notification payload processing, and permission flow.
// USED BY: Future Firebase/CleverTap Push Integration.
// CURRENT STATUS: Standalone module; not connected to QueueUp Core App.

package com.example.queueup.clevertap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import com.clevertap.android.sdk.CleverTapAPI

/**
 * PUSH ARCHITECTURE:
 * FCM Registration Token -> CleverTap.pushFcmRegistrationId -> CleverTap Dashboard Campaign -> FCM Dispatch -> Device
 *
 * STATUS BREAKDOWN:
 * [CODE PREPARED]:
 * - FCM registration token forwarder
 * - Payload inspector (CleverTapAPI.getNotificationInfo)
 * - System notification dispatcher
 * - Dedicated CleverTap notification channel creator
 *
 * [REQUIRES DASHBOARD / FIREBASE CONFIGURATION]:
 * 1. Google Services JSON (google-services.json) from Firebase Console.
 * 2. Upload FCM Server Key / Service Account JSON into CleverTap Dashboard -> Settings -> Channels -> Push Notifications.
 * 3. Add Firebase Messaging Service in AndroidManifest.xml when integration is activated.
 */
object CleverTapPush {

    const val CLEVERTAP_CHANNEL_ID = "queueup_clevertap_channel"
    const val CLEVERTAP_CHANNEL_NAME = "QueueUp Campus Announcements"

    /**
     * Registers FCM device registration token with CleverTap SDK.
     */
    fun registerFcmToken(fcmToken: String) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        cleverTap?.pushFcmRegistrationId(fcmToken, true)
    }

    /**
     * Creates notification channel for CleverTap push campaigns (Android O+).
     */
    fun createPushNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CLEVERTAP_CHANNEL_ID,
                CLEVERTAP_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Campus queue alerts, turn notifications, and announcements"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * Inspects incoming FCM push payload and delegates to CleverTap if originated from CleverTap.
     * @return true if payload belonged to CleverTap and was processed, false otherwise.
     */
    fun handlePushNotification(context: Context, payload: Bundle): Boolean {
        val info = CleverTapAPI.getNotificationInfo(payload)
        if (info.fromCleverTap) {
            CleverTapAPI.createNotification(context, payload)
            return true
        }
        return false
    }

    /**
     * Future Integration Point:
     * When ready, wire this inside FirebaseMessagingService:
     *
     * override fun onNewToken(token: String) {
     *     CleverTapPush.registerFcmToken(token)
     * }
     *
     * override fun onMessageReceived(remoteMessage: RemoteMessage) {
     *     val extras = Bundle().apply {
     *         remoteMessage.data.forEach { (k, v) -> putString(k, v) }
     *     }
     *     if (!CleverTapPush.handlePushNotification(applicationContext, extras)) {
     *         // Fallback to normal app notification handler
     *     }
     * }
     */
}
