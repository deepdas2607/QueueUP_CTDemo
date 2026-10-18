// NATIVE DISPLAY LAYER: Manages CleverTap Native Display Units (DisplayUnitListener, impression, click, and simulation).

package com.example.queueup.clevertap

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

object CleverTapNativeDisplay : DisplayUnitListener {

    private const val TAG = "NativeDisplay"

    // Observable list of active Native Display units rendered natively inside Jetpack Compose screens
    val activeDisplayUnits = mutableStateListOf<CleverTapDisplayUnit>()

    // Set of unitIDs that have already recorded a viewed impression to prevent duplicate events on recomposition
    private val viewedUnitIds = mutableSetOf<String>()

    // Callback stream to log Native Display lifecycle events in the in-app Activity Inspector
    var onDisplayUnitEventLogged: ((eventName: String, summary: String, props: Map<String, Any?>) -> Unit)? = null

    // Registers the DisplayUnitListener with the CleverTap SDK and pulls cached units
    fun initialize(cleverTapAPI: CleverTapAPI?) {
        try {
            cleverTapAPI?.setDisplayUnitListener(this)
            Log.d(TAG, "DisplayUnitListener successfully registered with CleverTap SDK")

            // Load any pre-cached display units from previous sessions/syncs
            val cachedUnits = cleverTapAPI?.allDisplayUnits
            if (!cachedUnits.isNullOrEmpty()) {
                Log.d(TAG, "Loaded ${cachedUnits.size} pre-cached display units from CleverTap")
                activeDisplayUnits.clear()
                activeDisplayUnits.addAll(cachedUnits)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering DisplayUnitListener with CleverTap", e)
        }
    }

    // Callback triggered by CleverTap SDK when Native Display campaign payloads arrive
    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        Log.d(TAG, "onDisplayUnitsLoaded invoked with ${units?.size ?: 0} units")
        if (units == null) return

        activeDisplayUnits.clear()
        activeDisplayUnits.addAll(units)

        for (unit in units) {
            val content = unit.contents?.firstOrNull()
            val title = content?.title ?: "Campus Update"
            val message = content?.message ?: ""

            onDisplayUnitEventLogged?.invoke(
                "Display_Unit_Loaded",
                "Native Display Loaded: $title",
                mapOf(
                    "unit_id" to (unit.unitID ?: "unknown"),
                    "type" to (unit.type?.name ?: "BANNER"),
                    "bg_color" to (unit.bgColor ?: "#FFFFFF"),
                    "title" to title,
                    "message" to message,
                    "action_url" to (content?.actionUrl ?: "None"),
                    "custom_extras" to (unit.customExtras ?: emptyMap<String, String>())
                )
            )
        }
    }

    // Records the Notification Viewed event when a Native Display unit appears in the viewport
    fun recordDisplayUnitViewed(unit: CleverTapDisplayUnit, cleverTapAPI: CleverTapAPI?) {
        val unitId = unit.unitID ?: return
        if (viewedUnitIds.contains(unitId)) return
        viewedUnitIds.add(unitId)

        try {
            cleverTapAPI?.pushDisplayUnitViewedEventForID(unitId)
            Log.d(TAG, "pushDisplayUnitViewedEventForID sent for: $unitId")

            val title = unit.contents?.firstOrNull()?.title ?: "Native Display"
            onDisplayUnitEventLogged?.invoke(
                "Display_Unit_Viewed",
                "Native Display impression recorded: $title",
                mapOf(
                    "unit_id" to unitId,
                    "type" to (unit.type?.name ?: "BANNER")
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error recording Display Unit viewed impression", e)
        }
    }

    // Records the Notification Clicked event when a student interacts with the Native Display card
    fun recordDisplayUnitClicked(unit: CleverTapDisplayUnit, context: Context, cleverTapAPI: CleverTapAPI?) {
        val unitId = unit.unitID ?: return

        try {
            cleverTapAPI?.pushDisplayUnitClickedEventForID(unitId)
            Log.d(TAG, "pushDisplayUnitClickedEventForID sent for: $unitId")

            val content = unit.contents?.firstOrNull()
            val actionUrl = content?.actionUrl
            val title = content?.title ?: "Native Display"

            onDisplayUnitEventLogged?.invoke(
                "Display_Unit_Clicked",
                "Native Display CTA tapped: $title",
                mapOf(
                    "unit_id" to unitId,
                    "action_url" to (actionUrl ?: "None")
                )
            )

            // Open target URL if configured
            if (!actionUrl.isNullOrBlank()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(actionUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling Display Unit click action", e)
        }
    }

    // Dismisses the unit from the active screen
    fun dismissDisplayUnit(unit: CleverTapDisplayUnit) {
        activeDisplayUnits.remove(unit)
    }

    // Pulls all active Display Units from the CleverTap SDK memory cache
    fun refreshDisplayUnits(cleverTapAPI: CleverTapAPI?) {
        try {
            val units = cleverTapAPI?.allDisplayUnits
            activeDisplayUnits.clear()
            if (!units.isNullOrEmpty()) {
                activeDisplayUnits.addAll(units)
            }
            Log.d(TAG, "Manual refresh complete. Active units: ${activeDisplayUnits.size}")
            onDisplayUnitEventLogged?.invoke(
                "Display_Units_Fetched",
                "Fetched ${activeDisplayUnits.size} Display Units from SDK cache",
                mapOf("count" to activeDisplayUnits.size)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching display units from SDK", e)
        }
    }

    // Simulates an official CleverTapDisplayUnit locally for testing without waiting for a dashboard campaign
    fun simulateLocalDisplayUnit(
        title: String = "Campus Bookstore Flash Voucher",
        message: String = "Show your active queue ticket today to get 20% off all textbook & stationery pickups!",
        actionUrl: String = "https://clevertap.com",
        badge: String = "Campus Special"
    ) {
        try {
            val simId = "sim_unit_${System.currentTimeMillis() % 100000}"
            val json = JSONObject().apply {
                put("wzrk_id", simId)
                put("type", "banner")
                put("bg", "#F0FDF4")
                put("custom_kv", JSONObject().apply {
                    put("badge", badge)
                    put("category", "Campus Rewards")
                })
                put("content", JSONArray().apply {
                    put(JSONObject().apply {
                        put("title", JSONObject().apply {
                            put("text", title)
                            put("color", "#15803D")
                        })
                        put("message", JSONObject().apply {
                            put("text", message)
                            put("color", "#166534")
                        })
                        put("action", JSONObject().apply {
                            put("url", JSONObject().apply {
                                put("android", JSONObject().apply {
                                    put("text", actionUrl)
                                })
                            })
                        })
                    })
                })
            }

            val simulatedUnit = CleverTapDisplayUnit.toDisplayUnit(json)
            activeDisplayUnits.removeAll { it.unitID == simulatedUnit.unitID }
            activeDisplayUnits.add(0, simulatedUnit)

            onDisplayUnitEventLogged?.invoke(
                "Display_Unit_Simulated",
                "Simulated Native Display unit loaded on Home screen: $title",
                mapOf(
                    "unit_id" to simulatedUnit.unitID,
                    "title" to title,
                    "badge" to badge,
                    "action_url" to actionUrl
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error simulating local display unit", e)
        }
    }
}
