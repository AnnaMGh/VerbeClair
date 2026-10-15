package com.vamvisions.verbeclair.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

internal val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
internal val LocalExtendedTypography = staticCompositionLocalOf<ExtendedTypography> {
    error("No ExtendedTypography provided; wrap the content in VerbeClairTheme")
}
internal val LocalSpacing = staticCompositionLocalOf { Spacing() }
internal val LocalRadius = staticCompositionLocalOf { Radius() }
internal val LocalElevation = staticCompositionLocalOf { Elevation() }
internal val LocalFlagOpacity = staticCompositionLocalOf { FLAG_OPACITY }

/**
 * The VerbeClair theme: Material 3 colours, typography and shapes from the Figma Foundations,
 * plus the VerbeClair-only tokens read through [VerbeClairTheme].
 */
@Composable
fun VerbeClairTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val radius = Radius()
    CompositionLocalProvider(
        LocalExtendedColors provides if (darkTheme) DarkExtendedColors else LightExtendedColors,
        LocalExtendedTypography provides verbeClairExtendedTypography(),
        LocalSpacing provides Spacing(),
        LocalRadius provides radius,
        LocalElevation provides Elevation(),
        LocalFlagOpacity provides FLAG_OPACITY,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = verbeClairTypography(),
            shapes = verbeClairShapes(radius),
            content = content,
        )
    }
}

object VerbeClairTheme {
    val extendedColors: ExtendedColors
        @Composable @ReadOnlyComposable get() = LocalExtendedColors.current
    val typography: ExtendedTypography
        @Composable @ReadOnlyComposable get() = LocalExtendedTypography.current
    val spacing: Spacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current
    val radius: Radius
        @Composable @ReadOnlyComposable get() = LocalRadius.current
    val elevation: Elevation
        @Composable @ReadOnlyComposable get() = LocalElevation.current
    val flagOpacity: Float
        @Composable @ReadOnlyComposable get() = LocalFlagOpacity.current
}
