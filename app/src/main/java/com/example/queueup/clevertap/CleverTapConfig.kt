// CONFIGURATION LAYER: Holds CleverTap project credentials, region, and privacy consent state.

package com.example.queueup.clevertap

object CleverTapConfig {
    // Project ID & Token from CleverTap Dashboard (Settings > Project)
    const val ACCOUNT_ID = "TEST-CLEVERTAP-ACCOUNT-ID"
    const val TOKEN = "TEST-CLEVERTAP-TOKEN"
    const val REGION = "in1" // Supported regions: in1, us1, eu1, sg1

    // Controls whether analytics tracking is permitted under GDPR / privacy preferences
    var isAnalyticsConsentGiven: Boolean = true
}
