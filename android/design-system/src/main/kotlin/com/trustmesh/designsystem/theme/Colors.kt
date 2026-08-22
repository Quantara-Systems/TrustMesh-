package com.trustmesh.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class TrustMeshColors(
    val backgroundBase: Color,
    val surfaceElevated1: Color,
    val surfaceElevated2: Color,
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val danger: Color,
    val success: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val trustHigh: Color,
    val trustMedium: Color,
    val trustLow: Color,
    val escrowPending: Color,
    val driftDetected: Color,
    val isDark: Boolean
)

// Force white and blue classic color configuration across all screens
val DarkColorScheme = TrustMeshColors(
    backgroundBase = Color(0xFFFFFFFF), // Pure White Background Base
    surfaceElevated1 = Color(0xFFF8FAFC), // Soft Slate-Blue elevated container
    surfaceElevated2 = Color(0xFFF1F5F9),
    primary = Color(0xFF2563EB), // Classic Royal Blue highlights
    primaryVariant = Color(0xFF1D4ED8),
    secondary = Color(0xFF475569),
    danger = Color(0xFFEF4444),
    success = Color(0xFF10B981),
    textPrimary = Color(0xFF0F172A), // Premium high-contrast deep slate text
    textSecondary = Color(0xFF64748B),
    divider = Color(0xFFE2E8F0), // Thin slate hairline borders
    trustHigh = Color(0xFF10B981),
    trustMedium = Color(0xFFF59E0B),
    trustLow = Color(0xFFEF4444),
    escrowPending = Color(0xFFF59E0B),
    driftDetected = Color(0xFFEF4444),
    isDark = false
)

val LightColorScheme = TrustMeshColors(
    backgroundBase = Color(0xFFFFFFFF), // Pure White Background Base
    surfaceElevated1 = Color(0xFFF8FAFC), // Soft Slate-Blue elevated container
    surfaceElevated2 = Color(0xFFF1F5F9),
    primary = Color(0xFF2563EB), // Classic Royal Blue highlights
    primaryVariant = Color(0xFF1D4ED8),
    secondary = Color(0xFF475569),
    danger = Color(0xFFEF4444),
    success = Color(0xFF10B981),
    textPrimary = Color(0xFF0F172A), // Premium high-contrast deep slate text
    textSecondary = Color(0xFF64748B),
    divider = Color(0xFFE2E8F0), // Thin slate hairline borders
    trustHigh = Color(0xFF10B981),
    trustMedium = Color(0xFFF59E0B),
    trustLow = Color(0xFFEF4444),
    escrowPending = Color(0xFFF59E0B),
    driftDetected = Color(0xFFEF4444),
    isDark = false
)

val LocalTrustMeshColors = staticCompositionLocalOf { LightColorScheme }
