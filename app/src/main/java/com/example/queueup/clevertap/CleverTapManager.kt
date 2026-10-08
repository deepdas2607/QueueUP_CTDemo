// FILE TYPE: CleverTap Integration Manager
// PURPOSE: Central manager providing SDK initialization and access to CleverTap instance.
// USED BY: CleverTap standalone module components.
// CURRENT STATUS: Standalone module; not connected to QueueUp Core App.

package com.example.queueup.clevertap

import android.content.Context
import com.clevertap.android.sdk.CleverTapAPI

class CleverTapManager private constructor() {

    private var cleverTapInstance: CleverTapAPI? = null

    fun initialize(context: Context) {
        if (cleverTapInstance == null) {
            // Set CleverTap Credentials dynamically or let SDK pick from Manifest
            CleverTapAPI.changeCredentials(CleverTapConfig.ACCOUNT_ID, CleverTapConfig.TOKEN, CleverTapConfig.REGION)
            cleverTapInstance = CleverTapAPI.getDefaultInstance(context.applicationContext)
            
            // Enable Debug logging during development
            CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG)
        }
    }

    fun getInstance(): CleverTapAPI? = cleverTapInstance

    companion object {
        @Volatile
        private var instance: CleverTapManager? = null

        fun getInstance(): CleverTapManager {
            return instance ?: synchronized(this) {
                instance ?: CleverTapManager().also { instance = it }
            }
        }
    }
}
