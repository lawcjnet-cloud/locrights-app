package com.locrights.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.locrights.app.data.local.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = BrandEmerald,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7F4E8),
    onPrimaryContainer = Color(0xFF0A3D2C),
    secondary = BrandNavy,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6E9F0),
    onSecondaryContainer = BrandNavy,
    tertiary = BrandMint,
    onTertiary = BrandNavy,
    background = BrandAlabaster,
    onBackground = BrandNavy,
    surface = BrandPaper,
    onSurface = BrandNavy,
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = BrandMutedTextLight,
    outline = BrandOutlineLight,
    outlineVariant = Color(0xFFEBEEF3)
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandMint,
    onPrimary = Color(0xFF082D1F),
    primaryContainer = Color(0xFF124D39),
    onPrimaryContainer = Color(0xFFD7F4E8),
    secondary = Color(0xFFBAC4D6),
    onSecondary = DarkCanvas,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = BrandEmerald,
    onTertiary = Color.White,
    background = DarkCanvas,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkOutline,
    outlineVariant = Color(0xFF232B3B)
)

@Composable
fun LocRightsTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme,
        typography = LocRightsTypography,
        content = content
    )
}
