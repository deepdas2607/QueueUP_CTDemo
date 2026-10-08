// FILE TYPE: Utility
// PURPOSE: Parses server response errors or raw JSON strings into clean user-friendly messages.
// USED BY: Repositories, ViewModels, UI Screens
// DATA SOURCE: HTTP Error Bodies and Exceptions

package com.example.queueup.utils

import org.json.JSONObject

object ErrorParser {

    fun parse(rawError: String?): String {
        if (rawError.isNullOrBlank()) return "An unexpected error occurred. Please try again."

        val trimmed = rawError.trim()

        // Check if it's a JSON object
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                val json = JSONObject(trimmed)
                if (json.has("error")) {
                    return json.getString("error")
                }
                if (json.has("message")) {
                    return json.getString("message")
                }
            } catch (_: Exception) {
                // Ignore parse errors and fall through
            }
        }

        // Clean up common exception prefixes
        var cleaned = trimmed
        if (cleaned.startsWith("Exception:")) {
            cleaned = cleaned.removePrefix("Exception:").trim()
        }
        if (cleaned.startsWith("Registration failed:")) {
            val inner = cleaned.removePrefix("Registration failed:").trim()
            return parse(inner)
        }

        return cleaned
    }
}
