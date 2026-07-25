package com.jarvis.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E3A5F),           // Dark Blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001B3D),
    secondary = Color(0xFF3A5F8A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E4FF),
    onSecondaryContainer = Color(0xFF001B3D),
    tertiary = Color(0xFF4A90D9),
    tertiaryContainer = Color(0xFFE8F0FE),
    onTertiaryContainer = Color(0xFF001B3D),
    background = Color.White,
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFF0F4FA),
    onSurfaceVariant = Color(0xFF44474E),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    outline = Color(0xFFD0D5DD)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA1C4FD),           // Light blue on dark
    onPrimary = Color(0xFF00315C),
    primaryContainer = Color(0xFF1E3A5F),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFFA3C8FF),
    onSecondary = Color(0xFF00315C),
    secondaryContainer = Color(0xFF3A5F8A),
    onSecondaryContainer = Color(0xFFD6E4FF),
    tertiary = Color(0xFF81B4FF),
    background = Color(0xFF0A1628),        // Deep navy
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF101D33),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF25324A),
    onSurfaceVariant = Color(0xFFC1C7D0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    outline = Color(0xFF5A6270)
)

@Composable
fun JarvisTheme(
    darkTheme: Boolean = false,  // Force light — white bg always
    content: @Composable () -> Unit
) {
    val colorScheme = LightColors  // Always white background

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
