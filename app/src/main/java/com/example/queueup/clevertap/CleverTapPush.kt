// FILE TYPE: CleverTap Push Notification Handler
// PURPOSE: Handles FCM Push Token registration and push notification payload processing.
// USED BY: Future Firebase/CleverTap Push Integration.
// CURRENT STATUS: Standalone module; not connected to QueueUp Core App.

package com.example.queueup.clevertap

import android.content.Context
import android.os.Bundle
import com.clevertap.android.sdk.CleverTapAPI

object CleverTapPush {

    fun registerFcmToken(fcmToken: String) {
        val cleverTap = CleverTapManager.getInstance().getInstance()
        cleverTap?.pushFcmRegistrationId(fcmToken, true)
    }

    fun handlePushNotification(context: Context, payload: Bundle): Boolean {
        val info = CleverTapAPI.getNotificationInfo(payload)
        if (info.fromCleverTap) {
            CleverTapAPI.createNotification(context, payload)
            return true
        }
        return false
    }
}
