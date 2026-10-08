// FILE TYPE: CleverTap Integration Configuration
// PURPOSE: Centralizes CleverTap Account Credentials, Region, and Feature Flags.
// USED BY: CleverTapManager
// CURRENT STATUS: Standalone module; not connected to QueueUp Core App.

package com.example.queueup.clevertap

object CleverTapConfig {
    const val ACCOUNT_ID = "TEST-CLEVERTAP-ACCOUNT-ID"
    const val TOKEN = "TEST-CLEVERTAP-TOKEN"
    const val REGION = "in1" // Example region: in1, us1, eu1, sg1

    // Feature toggles for future integration
    var isPushEnabled = true
    var isInAppEnabled = true
    var isAppInboxEnabled = true
    var isAnalyticsConsentGiven = true
}
