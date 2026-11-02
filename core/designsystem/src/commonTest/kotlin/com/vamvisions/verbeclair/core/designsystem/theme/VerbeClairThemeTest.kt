package com.vamvisions.verbeclair.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.font.FontFamily
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

@OptIn(ExperimentalTestApi::class)
class VerbeClairThemeTest {

    private class Read(
        val colorScheme: ColorScheme,
        val typography: Typography,
        val shapes: Shapes,
        val extendedColors: ExtendedColors,
        val extendedTypography: ExtendedTypography,
        val spacing: Spacing,
        val radius: Radius,
        val elevation: Elevation,
        val flagOpacity: Float,
    )

    private fun readTheme(darkTheme: Boolean): Read {
        var read: Read? = null
        runComposeUiTest {
            setContent {
                VerbeClairTheme(darkTheme = darkTheme) {
                    read =
                        Read(
                            colorScheme = MaterialTheme.colorScheme,
                            typography = MaterialTheme.typography,
                            shapes = MaterialTheme.shapes,
                            extendedColors = VerbeClairTheme.extendedColors,
                            extendedTypography = VerbeClairTheme.typography,
                            spacing = VerbeClairTheme.spacing,
                            radius = VerbeClairTheme.radius,
                            elevation = VerbeClairTheme.elevation,
                            flagOpacity = VerbeClairTheme.flagOpacity,
                        )
                }
            }
            waitForIdle()
        }
        return assertNotNull(read)
    }

    @Test
    fun lightThemeProvidesLightTokens() =
        assertTheme(readTheme(darkTheme = false), LightColorScheme, LightExtendedColors)

    @Test
    fun darkThemeProvidesDarkTokens() = assertTheme(readTheme(darkTheme = true), DarkColorScheme, DarkExtendedColors)

    private fun assertTheme(
        read: Read,
        colorScheme: ColorScheme,
        extendedColors: ExtendedColors,
    ) {
        assertSame(colorScheme, read.colorScheme)
        assertEquals(extendedColors, read.extendedColors)
        assertEquals(verbeClairShapes(), read.shapes)
        assertEquals(Spacing(), read.spacing)
        assertEquals(Radius(), read.radius)
        assertEquals(Elevation(), read.elevation)
        assertEquals(FLAG_OPACITY, read.flagOpacity)

        with(read.typography) {
            assertEquals(TypeTokens.DisplayVerbLarge.fontSize, displaySmall.fontSize)
            assertEquals(TypeTokens.BodyLarge.lineHeight, bodyLarge.lineHeight)
            // Every M3 slot, including the ones Figma does not define, uses a bundled font.
            listOf(
                displayLarge,
                displayMedium,
                displaySmall,
                headlineLarge,
                headlineMedium,
                headlineSmall,
                titleLarge,
                titleMedium,
                titleSmall,
                bodyLarge,
                bodyMedium,
                bodySmall,
                labelLarge,
                labelMedium,
                labelSmall,
            ).forEach { style ->
                assertNotNull(style.fontFamily)
                assertNotEquals(FontFamily.Default, style.fontFamily)
            }
            assertNotEquals(displaySmall.fontFamily, bodyLarge.fontFamily, "Fraunces and DM Sans differ")
        }
        with(read.extendedTypography) {
            assertEquals(TypeTokens.TitleVerb.fontSize, titleVerb.fontSize)
            assertEquals(TypeTokens.LabelOverline.letterSpacing, labelOverline.letterSpacing)
            assertEquals(TypeTokens.ConjugationForm.fontWeight, conjugationForm.fontWeight)
            assertEquals(TypeTokens.ConjugationEnding.fontWeight, conjugationEnding.fontWeight)
            assertEquals(read.typography.bodyLarge.fontFamily, conjugationForm.fontFamily)
        }
    }
}
