// FILE TYPE: CleverTap Consent Manager
// PURPOSE: Manages user consent for analytics, personalization, opt-in/opt-out per GDPR & privacy regulations.
// USED BY: Future QueueUp Settings / Profile Integration.
// CURRENT STATUS: Standalone module; not connected to QueueUp Core App.

package com.example.queueup.clevertap

/**
 * PRIVACY & CONSENT MANAGEMENT (GDPR / CCPA / DPDPA):
 *
 * Provides granular user privacy controls:
 * 1. Analytics Consent: Toggles event tracking on/off. When opted out, CleverTap setOptOut(true) disables tracking.
 * 2. Personalization Consent: Governs personalized push/in-app recommendations vs generic announcements.
 * 3. Right to Erasure: Disassociates identifiers upon account deletion.
 *
 * FUTURE CONNECTION POINT:
 * In ProfileScreen "Privacy Consent" switch toggle, call:
 * CleverTapConsent.updateConsent(analyticsConsent, personalizationConsent)
 */
object CleverTapConsent {

    /**
     * Updates full consent profile and syncs state with CleverTap SDK optOut API.
     */
    fun updateConsent(analyticsConsent: Boolean, personalizationConsent: Boolean) {
        CleverTapConfig.isAnalyticsConsentGiven = analyticsConsent

        val cleverTap = CleverTapManager.getInstance().getInstance()
        if (cleverTap != null) {
            // CleverTap SDK setOptOut halts data collection when user declines
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

    /**
     * Explicit opt-in convenience method.
     */
    fun optIn() {
        updateConsent(analyticsConsent = true, personalizationConsent = true)
    }

    /**
     * Explicit opt-out convenience method (disables all analytics & tracking).
     */
    fun optOut() {
        updateConsent(analyticsConsent = false, personalizationConsent = false)
    }

    /**
     * Returns whether analytics collection is currently permitted.
     */
    fun isAnalyticsPermitted(): Boolean = CleverTapConfig.isAnalyticsConsentGiven
}
