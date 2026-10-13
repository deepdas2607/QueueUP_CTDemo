// CONSENT LAYER: Manages GDPR opt-in/opt-out status using the CleverTap setOptOut API.

package com.example.queueup.clevertap

object CleverTapConsent {

    // Updates user privacy preferences and syncs opt-out state with the CleverTap SDK.
    fun updateConsent(analyticsConsent: Boolean, personalizationConsent: Boolean = true) {
        CleverTapConfig.isAnalyticsConsentGiven = analyticsConsent

        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null) {
            // CleverTap SDK halts tracking and transmission when optOut is true
            cleverTap.setOptOut(!analyticsConsent)
        }

        CleverTapEvents.logEvent(
            "Consent Updated",
            mapOf(
                "analyticsConsent" to analyticsConsent,
                "personalizationConsent" to personalizationConsent
            )
        )
    }

    // Convenience function to explicitly opt in to analytics.
    fun optIn() {
        updateConsent(analyticsConsent = true, personalizationConsent = true)
    }

    // Convenience function to explicitly opt out from analytics (halts tracking).
    fun optOut() {
        updateConsent(analyticsConsent = false, personalizationConsent = false)
    }

    // Returns true if analytics data collection is currently allowed.
    fun isAnalyticsPermitted(): Boolean = CleverTapConfig.isAnalyticsConsentGiven
}
