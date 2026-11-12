package com.rinconsur.probadores.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = RetailPurpleLight,
    onPrimary = RetailDarkNavy,
    primaryContainer = RetailPurpleDark,
    onPrimaryContainer = RetailPurpleContainer,
    secondary = RetailCyan,
    onSecondary = RetailDarkNavy,
    secondaryContainer = RetailCyanDark,
    onSecondaryContainer = RetailCyanContainer,
    tertiary = RetailCyan,
    background = RetailDarkNavy,
    onBackground = RetailBackgroundLight,
    surface = RetailDarkNavySurface,
    onSurface = Color.White,
    surfaceVariant = RetailDarkNavySurfaceVariant,
    onSurfaceVariant = Color(0xFFC7C5D8),
    error = Color(0xFFFF6B6B),
)

private val LightColorScheme = lightColorScheme(
    primary = RetailPurple,
    onPrimary = Color.White,
    primaryContainer = RetailPurpleContainer,
    onPrimaryContainer = RetailOnPurpleContainer,
    secondary = RetailCyanDark,
    onSecondary = Color.White,
    secondaryContainer = RetailCyanContainer,
    onSecondaryContainer = RetailOnCyanContainer,
    tertiary = RetailCyan,
    background = RetailBackgroundLight,
    onBackground = RetailOnSurfaceLight,
    surface = RetailSurfaceLight,
    onSurface = RetailOnSurfaceLight,
    surfaceVariant = Color(0xFFF1F3F9),
    onSurfaceVariant = RetailOnSurfaceVariantLight,
    outline = RetailOutlineLight,
    error = RetailRed,
    errorContainer = RetailRedContainer,
)

@Composable
fun RinconSurTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = RetailDarkNavy.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
