// APPLICATION ROOT: Registers lifecycle callbacks and initializes the CleverTap SDK on app startup.

package com.example.queueup

import android.app.Application
import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.example.queueup.clevertap.CleverTapManager

class QueueUpApplication : Application() {

    // Called when the application process is starting before any activities or services run.
    override fun onCreate() {
        // Must call ActivityLifecycleCallback.register(this) before super.onCreate() per CleverTap docs
        ActivityLifecycleCallback.register(this)
        super.onCreate()

        // Initialize CleverTap SDK instance
        CleverTapManager.getInstance().initialize(this)
    }
}

