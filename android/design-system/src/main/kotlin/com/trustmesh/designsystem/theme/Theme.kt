package com.trustmesh.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

object TrustMeshTheme {
    val colors: TrustMeshColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTrustMeshColors.current

    val typography: TrustMeshTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTrustMeshTypography.current
}

@Composable
fun TrustMeshTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    val materialColorScheme = if (darkTheme) {
        darkColorScheme(
            background = colors.backgroundBase,
            surface = colors.surfaceElevated1,
            primary = colors.primary,
            secondary = colors.secondary,
            error = colors.danger,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary
        )
    } else {
        lightColorScheme(
            background = colors.backgroundBase,
            surface = colors.surfaceElevated1,
            primary = colors.primary,
            secondary = colors.secondary,
            error = colors.danger,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary
        )
    }

    CompositionLocalProvider(
        LocalTrustMeshColors provides colors,
        LocalTrustMeshTypography provides TrustMeshTypeScale
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}
