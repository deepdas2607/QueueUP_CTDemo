// INITIALIZATION LAYER: Manages CleverTap SDK lifecycle and provides singleton access.

package com.example.queueup.clevertap

import android.app.Application
import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.clevertap.android.sdk.CleverTapAPI

class CleverTapManager private constructor() {

    private var cleverTapInstance: CleverTapAPI? = null

    // Initializes the CleverTap SDK with the custom application instance.
    fun initialize(application: Application) {
        if (cleverTapInstance == null) {
            // Register lifecycle callback before super.onCreate in Application class
            ActivityLifecycleCallback.register(application)
            cleverTapInstance = CleverTapAPI.getDefaultInstance(application.applicationContext)
            // Enable debug logging for testing and troubleshooting
            CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG.intValue())
        }
    }

    // Provides the active CleverTapAPI instance.
    fun getInstance(): CleverTapAPI? = cleverTapInstance

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
