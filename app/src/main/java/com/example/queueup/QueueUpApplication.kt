// APPLICATION ROOT: Registers lifecycle callbacks and initializes the CleverTap SDK on app startup.

package com.example.queueup

import android.app.Application
import android.util.Log
import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler
import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.interfaces.NotificationHandler
import com.example.queueup.clevertap.CleverTapManager
import com.google.firebase.messaging.FirebaseMessaging

class QueueUpApplication : Application() {

    // Called when the application process is starting before any activities or services run.
    override fun onCreate() {
        // Must call ActivityLifecycleCallback.register(this) before super.onCreate() per CleverTap docs
        ActivityLifecycleCallback.register(this)
        super.onCreate()

        // Configure CleverTap Custom Code In-App Templates before SDK initialization
        com.example.queueup.clevertap.CleverTapCustomTemplates.registerTemplates()

        // Configure CleverTap Push Templates Handler for rich push notifications (Carousels, Timers, 5-icon)
        CleverTapAPI.setNotificationHandler(PushTemplateNotificationHandler() as NotificationHandler)

        // Initialize CleverTap SDK instance
        CleverTapManager.getInstance().initialize(this)

        // Initialize CleverTap App Inbox
        CleverTapAPI.getDefaultInstance(this)?.initializeInbox()

        // Fetch active FCM registration token and sync with CleverTap
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("QueueUpApplication", "FCM token retrieved: $token")
                CleverTapAPI.getDefaultInstance(this)?.pushFcmRegistrationId(token, true)
            } else {
                Log.w("QueueUpApplication", "Fetching FCM token failed", task.exception)
            }
        }
    }
}

