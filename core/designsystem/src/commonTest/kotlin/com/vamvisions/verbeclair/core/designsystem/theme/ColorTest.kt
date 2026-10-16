package com.vamvisions.verbeclair.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.test.Test
import kotlin.test.assertEquals

/** Each row is design.md D3: Figma variable, theme role, Light hex, Dark hex. */
class ColorTest {

    private class Role(val figma: String, val light: String, val dark: String, val read: (ColorScheme) -> Color)

    private val roles = listOf(
        Role("color/primary", "#2E4BCB", "#B8C4FF") { it.primary },
        Role("color/on-primary", "#FFFFFF", "#08218A") { it.onPrimary },
        Role("color/primary-container", "#DDE1FF", "#1A35AD") { it.primaryContainer },
        Role("color/on-primary-container", "#000E5E", "#DDE1FF") { it.onPrimaryContainer },
        Role("color/secondary-container", "#E1E1F9", "#444559") { it.secondaryContainer },
        Role("color/on-secondary-container", "#191A2C", "#E1E1F9") { it.onSecondaryContainer },
        Role("color/secondary", "#5B5D72", "#C4C5DD") { it.secondary },
        Role("color/on-secondary", "#FFFFFF", "#2D2F42") { it.onSecondary },
        Role("color/tertiary", "#C0391F", "#FFB4A1") { it.tertiary },
        Role("color/on-tertiary", "#FFFFFF", "#5F1409") { it.onTertiary },
        Role("color/tertiary-container", "#FFDAD2", "#5F1409") { it.tertiaryContainer },
        Role("color/on-tertiary-container", "#3E0500", "#FFDAD2") { it.onTertiaryContainer },
        Role("color/inverse-primary", "#B8C4FF", "#2E4BCB") { it.inversePrimary },
        Role("color/background", "#FBFAFF", "#12131A") { it.background },
        Role("color/on-background", "#1A1B23", "#E3E1EC") { it.onBackground },
        Role("color/surface", "#FFFFFF", "#12131A") { it.surface },
        Role("color/surface-variant", "#E3E1EC", "#464754") { it.surfaceVariant },
        Role("color/surface-tint", "#2E4BCB", "#B8C4FF") { it.surfaceTint },
        Role("color/surface-dim", "#DBD9E4", "#12131A") { it.surfaceDim },
        Role("color/surface-bright", "#FFFFFF", "#383841") { it.surfaceBright },
        Role("color/surface-container-lowest", "#FFFFFF", "#0D0E15") { it.surfaceContainerLowest },
        Role("color/surface-container-low", "#FBFAFF", "#1A1B23") { it.surfaceContainerLow },
        Role("color/surface-container", "#F1F1F9", "#1E1F27") { it.surfaceContainer },
        Role("color/surface-container-high", "#E9E9F3", "#292A32") { it.surfaceContainerHigh },
        Role("color/surface-container-highest", "#E3E1EC", "#33343D") { it.surfaceContainerHighest },
        Role("color/inverse-surface", "#292A32", "#E3E1EC") { it.inverseSurface },
        Role("color/inverse-on-surface", "#F1F1F9", "#292A32") { it.inverseOnSurface },
        Role("color/scrim", "#000000", "#000000") { it.scrim },
        Role("color/on-surface", "#1A1B23", "#E3E1EC") { it.onSurface },
        Role("color/on-surface-variant", "#464754", "#C6C5D6") { it.onSurfaceVariant },
        Role("color/outline", "#767686", "#908FA0") { it.outline },
        Role("color/outline-variant", "#C6C5D6", "#464754") { it.outlineVariant },
        Role("color/error", "#BA1A1A", "#FFB4AB") { it.error },
        Role("color/on-error", "#FFFFFF", "#690005") { it.onError },
        Role("color/error-container", "#FFDAD6", "#93000A") { it.errorContainer },
        Role("color/on-error-container", "#410002", "#FFDAD6") { it.onErrorContainer },
        Role("color/primary-fixed", "#DDE1FF", "#DDE1FF") { it.primaryFixed },
        Role("color/primary-fixed-dim", "#B8C4FF", "#B8C4FF") { it.primaryFixedDim },
        Role("color/on-primary-fixed", "#000E5E", "#000E5E") { it.onPrimaryFixed },
        Role("color/on-primary-fixed-variant", "#1A35AD", "#1A35AD") { it.onPrimaryFixedVariant },
        Role("color/secondary-fixed", "#E1E1F9", "#E1E1F9") { it.secondaryFixed },
        Role("color/secondary-fixed-dim", "#C4C5DD", "#C4C5DD") { it.secondaryFixedDim },
        Role("color/on-secondary-fixed", "#191A2C", "#191A2C") { it.onSecondaryFixed },
        Role("color/on-secondary-fixed-variant", "#444559", "#444559") { it.onSecondaryFixedVariant },
        Role("color/tertiary-fixed", "#FFDAD2", "#FFDAD2") { it.tertiaryFixed },
        Role("color/tertiary-fixed-dim", "#FFB4A1", "#FFB4A1") { it.tertiaryFixedDim },
        Role("color/on-tertiary-fixed", "#3E0500", "#3E0500") { it.onTertiaryFixed },
        Role("color/on-tertiary-fixed-variant", "#8D1500", "#8D1500") { it.onTertiaryFixedVariant },
    )

    @Test
    fun everyM3RoleIsInTheTable() {
        assertEquals(48, roles.size)
        assertEquals(roles.size, roles.map { it.figma }.toSet().size)
    }

    @Test
    fun lightSchemeMatchesFigma() {
        roles.forEach { assertEquals(it.light, LightColorScheme.let(it.read).hex(), it.figma) }
    }

    @Test
    fun darkSchemeMatchesFigma() {
        roles.forEach { assertEquals(it.dark, DarkColorScheme.let(it.read).hex(), it.figma) }
    }

    @Test
    fun extendedColorsLightMatchFigma() {
        with(LightExtendedColors) {
            assertEquals("#C0391F", ending.hex())
            assertEquals("#FFDAD2", endingContainer.hex())
            assertEquals("#2E7D4F", success.hex())
            assertEquals("#FFFFFF", onSuccess.hex())
            assertEquals("#A4F4BC", successContainer.hex())
            assertEquals("#00210F", onSuccessContainer.hex())
            assertEquals("#C4C5DD", navigationIndicator.hex())
        }
    }

    @Test
    fun extendedColorsDarkMatchFigma() {
        with(DarkExtendedColors) {
            assertEquals("#FFB4A1", ending.hex())
            assertEquals("#5F1409", endingContainer.hex())
            assertEquals("#8FD8A8", success.hex())
            assertEquals("#00391D", onSuccess.hex())
            assertEquals("#00522C", successContainer.hex())
            assertEquals("#A4F4BC", onSuccessContainer.hex())
            assertEquals("#444559", navigationIndicator.hex())
        }
    }
}

internal fun Color.hex(): String {
    val argb = toArgb()
    require(argb ushr 24 == 0xFF) { "expected an opaque colour, got alpha ${argb ushr 24}" }
    return "#" + (argb and 0xFFFFFF).toString(16).uppercase().padStart(6, '0')
}
