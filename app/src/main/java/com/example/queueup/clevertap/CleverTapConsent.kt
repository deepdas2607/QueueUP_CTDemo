// FILE TYPE: CleverTap Consent Manager
// PURPOSE: Manages user consent for analytics, tracking, opt-in/opt-out per privacy regulations.
// USED BY: Future QueueUp Settings Integration.
// CURRENT STATUS: Standalone module; not connected to QueueUp Core App.

package com.example.queueup.clevertap

object CleverTapConsent {

    fun updateConsent(analyticsConsent: Boolean, personalizationConsent: Boolean) {
        CleverTapConfig.isAnalyticsConsentGiven = analyticsConsent

        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null) {
            // CleverTap SDK Privacy Consent API
            cleverTap.setOptOut(!analyticsConsent)
        }

        CleverTapEvents.logEvent(
            CleverTapEvents.Names.CONSENT_UPDATED,
            mapOf(
                "analyticsConsent" to analyticsConsent,
                "personalizationConsent" to personalizationConsent
            )
        )
    }
}
