package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// ENGLISH MASTER AI — GAMIFIED BRAND PALETTE
// ============================================================================

// Primary Brand Green (#58CC02) — Learning, Success & Progression
val FeatherGreen = Color(0xFF58CC02)
val FeatherGreenDark = Color(0xFF46A302)
val FeatherGreenPressed = Color(0xFF3A8700)
val FeatherGreenContainerLight = Color(0xFFD7FFB8)
val OnFeatherGreenContainerLight = Color(0xFF153100)
val FeatherGreenContainerDark = Color(0xFF1B3B00)
val OnFeatherGreenContainerDark = Color(0xFFD7FFB8)

// Secondary Brand Blue (#1CB0F6) — Grammar, Listening, Info & Links
val SkyBlue = Color(0xFF1CB0F6)
val SkyBlueDark = Color(0xFF38BDF8)
val SkyBluePressed = Color(0xFF1490CC)
val SkyBlueContainerLight = Color(0xFFDDF4FF)
val OnSkyBlueContainerLight = Color(0xFF002B3D)
val SkyBlueContainerDark = Color(0xFF083952)
val OnSkyBlueContainerDark = Color(0xFFDDF4FF)

// Accent Brand Yellow (#FFC800) — XP, Stars, Gold League & Achievements
val SunnyYellow = Color(0xFFFFC800)
val SunnyYellowDark = Color(0xFFE5B400)
val SunnyYellowContainerLight = Color(0xFFFFF4CC)
val OnSunnyYellowContainerLight = Color(0xFF3D2E00)
val SunnyYellowContainerDark = Color(0xFF423200)
val OnSunnyYellowContainerDark = Color(0xFFFFE57F)

// Accent Brand Orange (#FF9600) — Daily Streak, Flame & Challenges
val VibrantOrange = Color(0xFFFF9600)
val VibrantOrangeDark = Color(0xFFE08200)
val VibrantOrangeContainerLight = Color(0xFFFFE7C2)
val OnVibrantOrangeContainerLight = Color(0xFF452400)
val VibrantOrangeContainerDark = Color(0xFF4A2800)
val OnVibrantOrangeContainerDark = Color(0xFFFFD199)

// Accent Brand Red (#FF4B4B) — Hearts/Lives, Mistakes & Alerts
val HeartRed = Color(0xFFFF4B4B)
val HeartRedDark = Color(0xFFD93838)
val HeartRedContainerLight = Color(0xFFFFDFE0)
val OnHeartRedContainerLight = Color(0xFF4D0F0F)
val HeartRedContainerDark = Color(0xFF4D0F0F)
val OnHeartRedContainerDark = Color(0xFFFFDFE0)

// Tertiary Brand Purple (#CE82FF) — AI Tutor, Royal Badges & Quests
val RoyalPurple = Color(0xFFCE82FF)
val RoyalPurpleDark = Color(0xFFB354F0)
val RoyalPurpleContainerLight = Color(0xFFF3E2FF)
val OnRoyalPurpleContainerLight = Color(0xFF340B52)
val RoyalPurpleContainerDark = Color(0xFF3A1457)
val OnRoyalPurpleContainerDark = Color(0xFFF3E2FF)

// Neutral Surfaces & Backgrounds (#F7F9FC Light / Cohesive Dark Mode)
val BackgroundLight = Color(0xFFF7F9FC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFE8EEF5)
val OnSurfaceLight = Color(0xFF1E293B)
val OnSurfaceVariantLight = Color(0xFF475569)
val OutlineLight = Color(0xFFCBD5E1)
val OutlineVariantLight = Color(0xFFE2E8F0)

val BackgroundDark = Color(0xFF0E1621)
val SurfaceDark = Color(0xFF172334)
val SurfaceVariantDark = Color(0xFF213147)
val OnSurfaceDark = Color(0xFFF1F5F9)
val OnSurfaceVariantDark = Color(0xFFCBD5E1)
val OutlineDark = Color(0xFF334155)
val OutlineVariantDark = Color(0xFF1E293B)

/**
 * Extended semantic brand tokens for gamified counters (XP, Streak, Hearts, Units, AI Tutor).
 */
@Immutable
data class EnglishMasterBrandColors(
    val primaryGreen: Color,
    val primaryGreenContainer: Color,
    val onPrimaryGreenContainer: Color,
    val skyBlue: Color,
    val skyBlueContainer: Color,
    val onSkyBlueContainer: Color,
    val xpYellow: Color,
    val xpYellowContainer: Color,
    val onXpYellowContainer: Color,
    val streakOrange: Color,
    val streakOrangeContainer: Color,
    val onStreakOrangeContainer: Color,
    val heartRed: Color,
    val heartRedContainer: Color,
    val onHeartRedContainer: Color,
    val royalPurple: Color,
    val royalPurpleContainer: Color,
    val onRoyalPurpleContainer: Color
)

val LightBrandColors = EnglishMasterBrandColors(
    primaryGreen = FeatherGreen,
    primaryGreenContainer = FeatherGreenContainerLight,
    onPrimaryGreenContainer = OnFeatherGreenContainerLight,
    skyBlue = SkyBlue,
    skyBlueContainer = SkyBlueContainerLight,
    onSkyBlueContainer = OnSkyBlueContainerLight,
    xpYellow = SunnyYellow,
    xpYellowContainer = SunnyYellowContainerLight,
    onXpYellowContainer = OnSunnyYellowContainerLight,
    streakOrange = VibrantOrange,
    streakOrangeContainer = VibrantOrangeContainerLight,
    onStreakOrangeContainer = OnVibrantOrangeContainerLight,
    heartRed = HeartRed,
    heartRedContainer = HeartRedContainerLight,
    onHeartRedContainer = OnHeartRedContainerLight,
    royalPurple = RoyalPurple,
    royalPurpleContainer = RoyalPurpleContainerLight,
    onRoyalPurpleContainer = OnRoyalPurpleContainerLight
)

val DarkBrandColors = EnglishMasterBrandColors(
    primaryGreen = FeatherGreen,
    primaryGreenContainer = FeatherGreenContainerDark,
    onPrimaryGreenContainer = OnFeatherGreenContainerDark,
    skyBlue = SkyBlueDark,
    skyBlueContainer = SkyBlueContainerDark,
    onSkyBlueContainer = OnSkyBlueContainerDark,
    xpYellow = SunnyYellow,
    xpYellowContainer = SunnyYellowContainerDark,
    onXpYellowContainer = OnSunnyYellowContainerDark,
    streakOrange = VibrantOrange,
    streakOrangeContainer = VibrantOrangeContainerDark,
    onStreakOrangeContainer = OnVibrantOrangeContainerDark,
    heartRed = HeartRed,
    heartRedContainer = HeartRedContainerDark,
    onHeartRedContainer = OnHeartRedContainerDark,
    royalPurple = RoyalPurple,
    royalPurpleContainer = RoyalPurpleContainerDark,
    onRoyalPurpleContainer = OnRoyalPurpleContainerDark
)

val LocalBrandColors = staticCompositionLocalOf { LightBrandColors }
