package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Playful, rounded Material 3 shape hierarchy for buttons, cards, badges, and dialogs.
 */
val EnglishMasterShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val DarkColorScheme = darkColorScheme(
    primary = FeatherGreen,
    onPrimary = Color(0xFF091A00),
    primaryContainer = FeatherGreenContainerDark,
    onPrimaryContainer = OnFeatherGreenContainerDark,
    inversePrimary = FeatherGreenDark,
    secondary = SkyBlueDark,
    onSecondary = Color(0xFF001E2C),
    secondaryContainer = SkyBlueContainerDark,
    onSecondaryContainer = OnSkyBlueContainerDark,
    tertiary = RoyalPurple,
    onTertiary = Color(0xFF25003F),
    tertiaryContainer = RoyalPurpleContainerDark,
    onTertiaryContainer = OnRoyalPurpleContainerDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceTint = FeatherGreen,
    inverseSurface = SurfaceLight,
    inverseOnSurface = OnSurfaceLight,
    error = HeartRed,
    onError = Color.White,
    errorContainer = HeartRedContainerDark,
    onErrorContainer = OnHeartRedContainerDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = Color(0xCC000000)
)

private val LightColorScheme = lightColorScheme(
    primary = FeatherGreen,
    onPrimary = Color.White,
    primaryContainer = FeatherGreenContainerLight,
    onPrimaryContainer = OnFeatherGreenContainerLight,
    inversePrimary = FeatherGreen,
    secondary = SkyBlue,
    onSecondary = Color.White,
    secondaryContainer = SkyBlueContainerLight,
    onSecondaryContainer = OnSkyBlueContainerLight,
    tertiary = RoyalPurple,
    onTertiary = Color.White,
    tertiaryContainer = RoyalPurpleContainerLight,
    onTertiaryContainer = OnRoyalPurpleContainerLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceTint = FeatherGreen,
    inverseSurface = SurfaceDark,
    inverseOnSurface = OnSurfaceDark,
    error = HeartRed,
    onError = Color.White,
    errorContainer = HeartRedContainerLight,
    onErrorContainer = OnHeartRedContainerLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = Color(0x99000000)
)

object EnglishMasterTheme {
    val brandColors: EnglishMasterBrandColors
        @Composable
        @ReadOnlyComposable
        get() = LocalBrandColors.current
}

@Composable
fun EnglishMasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val brandColors = if (darkTheme) DarkBrandColors else LightBrandColors

    CompositionLocalProvider(LocalBrandColors provides brandColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = EnglishMasterShapes,
            content = content
        )
    }
}
