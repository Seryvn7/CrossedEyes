package com.crosseyes.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light green / blue accent palette over a deep, near-black cyan-green base.
val MintPrimary = Color(0xFF5BF0B0)
val SkySecondary = Color(0xFF4FC3F7)
val AccentGlow = Color(0xFF7DF9C0)
val BaseDark = Color(0xFF0B1210)
val SurfaceDark = Color(0xFF12201C)
val SurfaceLight = Color(0xFFEAF6F0)
val OnDark = Color(0xFFE8FFF6)

private val DarkColors = darkColorScheme(
    primary = MintPrimary,
    onPrimary = Color(0xFF052B1C),
    primaryContainer = Color(0xFF0F3A2B),
    onPrimaryContainer = Color(0xFFB9FFE0),
    secondary = SkySecondary,
    onSecondary = Color(0xFF06283A),
    secondaryContainer = Color(0xFF0E3448),
    onSecondaryContainer = Color(0xFFC7EDFF),
    tertiary = AccentGlow,
    background = BaseDark,
    onBackground = OnDark,
    surface = SurfaceDark,
    onSurface = OnDark,
    surfaceVariant = Color(0xFF163029),
    onSurfaceVariant = Color(0xFFC9E8DE),
    surfaceTint = MintPrimary,
    outline = Color(0xFF3F6B5D),
    outlineVariant = Color(0xFF24423A)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0C6E4E),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF0E5C7E),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF2E8A5F),
    background = Color(0xFFF3FBF6),
    onBackground = Color(0xFF0A1512),
    surface = SurfaceLight,
    onSurface = Color(0xFF0A1512),
    surfaceVariant = Color(0xFFD9EDE3),
    onSurfaceVariant = Color(0xFF2E4A3F),
    outline = Color(0xFF7BA192)
)

@Composable
fun CrossEyesTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
