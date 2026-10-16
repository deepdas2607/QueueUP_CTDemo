// CUSTOM IN-APP TEMPLATES: Defines, registers, and presents custom code in-app templates and functions.

package com.example.queueup.clevertap

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.inapp.customtemplates.CustomTemplate
import com.clevertap.android.sdk.inapp.customtemplates.CustomTemplateContext
import com.clevertap.android.sdk.inapp.customtemplates.FunctionPresenter
import com.clevertap.android.sdk.inapp.customtemplates.TemplatePresenter

// Data representation for the custom in-app template UI
data class CustomInAppUiModel(
    val templateName: String,
    val title: String,
    val message: String,
    val ctaLabel: String,
    val badgeTag: String,
    val contextRef: CustomTemplateContext.TemplateContext? = null
)

object CleverTapCustomTemplates {

    private const val TAG = "CustomInAppTemplates"

    // Observable state holding the currently visible custom template for Compose UI rendering
    var activeCustomTemplate by mutableStateOf<CustomInAppUiModel?>(null)

    // Callback invoked when a template or function executes (for logging into Activity Inspector)
    var onTemplateEventLogged: ((eventName: String, summary: String, props: Map<String, Any?>) -> Unit)? = null

    // Presenter for the visual custom banner template
    private val alertBannerPresenter = object : TemplatePresenter {
        override fun onPresent(context: CustomTemplateContext.TemplateContext) {
            val title = context.getString("title") ?: "Campus Queue Alert"
            val message = context.getString("message") ?: "A counter queue update is ready."
            val ctaLabel = context.getString("cta_label") ?: "View Line"
            val badgeTag = context.getString("badge_tag") ?: "Counter Alert"

            Log.d(TAG, "Custom In-App Template onPresent: $title - $message")
            context.setPresented()

            activeCustomTemplate = CustomInAppUiModel(
                templateName = context.templateName,
                title = title,
                message = message,
                ctaLabel = ctaLabel,
                badgeTag = badgeTag,
                contextRef = context
            )

            onTemplateEventLogged?.invoke(
                "Custom_Template_Presented",
                "Custom In-App Banner displayed: $title",
                mapOf(
                    "template" to context.templateName,
                    "title" to title,
                    "message" to message,
                    "cta_label" to ctaLabel
                )
            )
        }

        override fun onClose(context: CustomTemplateContext.TemplateContext) {
            Log.d(TAG, "Custom In-App Template onClose: ${context.templateName}")
            context.setDismissed()
            if (activeCustomTemplate?.templateName == context.templateName) {
                activeCustomTemplate = null
            }
        }
    }

    // Presenter for the non-visual haptic/chime function
    private val hapticFunctionPresenter = object : FunctionPresenter {
        override fun onPresent(context: CustomTemplateContext.FunctionContext) {
            val durationMs = context.getInt("vibrate_duration_ms") ?: 400
            Log.d(TAG, "Custom Function onPresent: Haptic Turn Alert ($durationMs ms)")
            context.setPresented()

            onTemplateEventLogged?.invoke(
                "Custom_Function_Executed",
                "Non-visual turn alert executed ($durationMs ms)",
                mapOf("vibrate_duration_ms" to durationMs)
            )

            context.setDismissed()
        }
    }

    // Registers all custom code templates and functions with CleverTap BEFORE CleverTapAPI initialization
    fun registerTemplates() {
        CleverTapAPI.registerCustomInAppTemplates { _ ->
            setOf(
                // 1. Visual Custom Code Template: Campus Queue Alert Banner
                CustomTemplate.TemplateBuilder()
                    .name("Campus_Queue_Alert_Banner")
                    .presenter(alertBannerPresenter)
                    .stringArgument("title", "Campus Queue Alert")
                    .stringArgument("message", "A counter queue status update is ready.")
                    .stringArgument("cta_label", "Check Status")
                    .stringArgument("badge_tag", "Counter Notice")
                    .actionArgument("on_click_action")
                    .build(),

                // 2. Non-Visual Function: Turn Alert Haptic
                CustomTemplate.FunctionBuilder(false)
                    .name("Campus_Turn_Haptic_Alert")
                    .presenter(hapticFunctionPresenter)
                    .intArgument("vibrate_duration_ms", 400)
                    .build()
            )
        }
        Log.d(TAG, "Custom In-App Templates registered with CleverTap SDK")
    }

    // Dismisses the active template and releases the CleverTap in-app queue slot
    fun dismissActiveTemplate() {
        val model = activeCustomTemplate ?: return
        model.contextRef?.setDismissed()
        activeCustomTemplate = null
    }

    // Triggers the template's action argument and dismisses the template
    fun triggerAction(context: Context) {
        val model = activeCustomTemplate ?: return
        model.contextRef?.let { ctx ->
            try {
                ctx.triggerActionArgument("on_click_action", context)
            } catch (e: Exception) {
                Log.w(TAG, "Error triggering on_click_action argument", e)
            }
            ctx.setDismissed()
        }
        activeCustomTemplate = null
    }

    // Uploads registered custom code templates to CleverTap dashboard for campaign creation
    fun syncTemplatesWithDashboard(cleverTapAPI: CleverTapAPI?) {
        try {
            cleverTapAPI?.syncRegisteredInAppTemplates()
            Log.d(TAG, "syncRegisteredInAppTemplates triggered successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync custom templates to dashboard", e)
        }
    }

    // Simulates presenting the custom template locally for instant verification
    fun simulateLocalTemplate(title: String = "Express Lane Ready", message: String = "Counter #4 is now accepting digital tokens.") {
        activeCustomTemplate = CustomInAppUiModel(
            templateName = "Campus_Queue_Alert_Banner",
            title = title,
            message = message,
            ctaLabel = "Join Queue",
            badgeTag = "Live Notice",
            contextRef = null
        )
        onTemplateEventLogged?.invoke(
            "Custom_Template_Simulated",
            "Simulated local presentation of Custom In-App Banner",
            mapOf("title" to title, "message" to message)
        )
    }
}

