// INITIALIZATION LAYER: Manages CleverTap SDK lifecycle and provides singleton access.

package com.example.queueup.clevertap

import android.app.Application
import android.util.Log
import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CleverTapAPI

class CleverTapManager private constructor() {

    private var cleverTapInstance: CleverTapAPI? = null

    // Initializes the CleverTap SDK with the custom application instance.
    fun initialize(application: Application) {
        if (cleverTapInstance == null) {
            // Register lifecycle callback before super.onCreate in Application class
            ActivityLifecycleCallback.register(application)
            // Pass CleverTap Project credentials and region as explicit strings
            CleverTapAPI.changeCredentials(CleverTapConfig.ACCOUNT_ID, CleverTapConfig.TOKEN, CleverTapConfig.REGION)
            cleverTapInstance = CleverTapAPI.getDefaultInstance(application.applicationContext)
            // Enable debug logging for testing and troubleshooting
            CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG.intValue())

            // Register CTInboxListener to monitor App Inbox sync events
            cleverTapInstance?.ctNotificationInboxListener = object : CTInboxListener {
                override fun inboxDidInitialize() {
                    Log.d("CleverTapManager", "CTInboxListener: inboxDidInitialize() - total inbox messages: ${cleverTapInstance?.inboxMessageCount}")
                }

                override fun inboxMessagesDidUpdate() {
                    Log.d("CleverTapManager", "CTInboxListener: inboxMessagesDidUpdate() - total inbox messages: ${cleverTapInstance?.inboxMessageCount}")
                }
            }
        }
    }

    // Provides the active CleverTapAPI instance.
    fun getInstance(): CleverTapAPI? = cleverTapInstance

    // Helper getter for active CleverTapAPI instance.
    fun getCleverTapInstance(): CleverTapAPI? = cleverTapInstance

    // Registers the Firebase Cloud Messaging registration token with CleverTap.
    fun pushFcmToken(token: String) {
        cleverTapInstance?.pushFcmRegistrationId(token, true)
    }

    // Prompts user for notification permission using CleverTap Local In-App Push Primer.
    fun promptPushPrimer(activity: androidx.fragment.app.FragmentActivity) {
        try {
            val primer = com.clevertap.android.sdk.inapp.CTLocalInApp.builder()
                .setInAppType(com.clevertap.android.sdk.inapp.CTLocalInApp.InAppType.ALERT)
                .setTitleText("Campus Queue Updates")
                .setMessageText("Enable notifications to receive real-time turn alerts, queue reminders, and campus notices.")
                .followDeviceOrientation(true)
                .setPositiveBtnText("Allow")
                .setNegativeBtnText("Not Now")
                .setFallbackToSettings(true)
                .build()
            cleverTapInstance?.promptPushPrimer(primer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Opens CleverTap native App Inbox styled to match the QueueUp campus theme.
    fun showAppInbox() {
        try {
            val styleConfig = com.clevertap.android.sdk.CTInboxStyleConfig().apply {
                val tabs = arrayListOf("Announcements")
                setTabs(tabs)
                setTabBackgroundColor("#FFFFFF")
                setSelectedTabIndicatorColor("#2563EB")
                setSelectedTabColor("#2563EB")
                setUnselectedTabColor("#64748B")
                setBackButtonColor("#2563EB")
                setNavBarTitleColor("#1E293B")
                setNavBarTitle("Campus Inbox")
                setNavBarColor("#FFFFFF")
                setInboxBackgroundColor("#F8FAFC")
            }
            cleverTapInstance?.showAppInbox(styleConfig)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Suspends in-app notification popups during critical flows (e.g. active counter turn checkout).
    fun suspendInAppNotifications() {
        cleverTapInstance?.suspendInAppNotifications()
    }

    // Resumes in-app notification popups once critical flow finishes.
    fun resumeInAppNotifications() {
        cleverTapInstance?.resumeInAppNotifications()
    }

    companion object {
        @Volatile
        private var instance: CleverTapManager? = null

        // Singleton getter for CleverTapManager.
        fun getInstance(): CleverTapManager {
            return instance ?: synchronized(this) {
                instance ?: CleverTapManager().also { instance = it }
            }
        }
    }
}

