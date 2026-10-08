// FILE TYPE: Material 3 Theme Configuration
// PURPOSE: Sets up light/dark MaterialTheme color schemes for QueueUp app.
// USED BY: MainActivity, Preview Composables

package com.example.queueup.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryIndigo,
    onPrimary = Color(0xFF0F172A),
    secondary = DarkSecondaryTeal,
    onSecondary = Color(0xFF0F172A),
    tertiary = DarkAccentOrange,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF475569),
    error = DarkErrorRed,
    errorContainer = Color(0xFF450A0A),
    onErrorContainer = Color(0xFFFECACA)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryIndigo,
    onPrimary = Color.White,
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    tertiary = AccentOrange,
    background = SurfaceLight,
    surface = CardBackground,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    outlineVariant = Color(0xFFE2E8F0),
    error = ErrorRed,
    errorContainer = Color(0xFFFEF2F2),
    onErrorContainer = Color(0xFF991B1B)
)

@Composable
fun QueueUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
