package com.vamvisions.verbeclair.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vamvisions.verbeclair.core.designsystem.generated.resources.Res
import com.vamvisions.verbeclair.core.designsystem.generated.resources.dmsans_variable
import com.vamvisions.verbeclair.core.designsystem.generated.resources.fraunces_variable
import org.jetbrains.compose.resources.Font

/** VerbeClair text styles that have no Material 3 slot. */
@Immutable
data class ExtendedTypography(
    val titleVerb: TextStyle,
    val labelOverline: TextStyle,
    val conjugationForm: TextStyle,
    val conjugationEnding: TextStyle,
)

// Figma text styles (Foundations, Typography 8:244): size, line height, letter spacing and weight.
// Font families are attached in [verbeClairTypography], because loading a font resource needs composition.
internal object TypeTokens {
    val DisplayVerbLarge = style(FontWeight.SemiBold, 34, 40, -0.5f)
    val HeadlineSmall = style(FontWeight.SemiBold, 24, 32, 0f)
    val TitleVerb = style(FontWeight.SemiBold, 20, 26, 0f)
    val TitleLarge = style(FontWeight.SemiBold, 22, 28, 0f)
    val TitleMedium = style(FontWeight.SemiBold, 16, 24, 0.1f)
    val TitleSmall = style(FontWeight.SemiBold, 14, 20, 0.1f)
    val LabelOverline = style(FontWeight.Bold, 12, 16, 1.2f)
    val LabelLarge = style(FontWeight.Medium, 14, 20, 0.1f)
    val LabelMedium = style(FontWeight.Medium, 12, 16, 0.4f)
    val BodyLarge = style(FontWeight.Normal, 16, 24, 0.2f)
    val BodyMedium = style(FontWeight.Normal, 14, 20, 0.2f)
    val BodySmall = style(FontWeight.Normal, 12, 16, 0.3f)
    val ConjugationForm = style(FontWeight.Normal, 16, 24, 0f)
    val ConjugationEnding = style(FontWeight.Bold, 16, 24, 0f)

    // Figma sets Fraunces with WONK 1, SOFT 0 and automatic optical size (opsz = font size),
    // and DM Sans with opsz 14.
    const val FRAUNCES_WONK = 1f
    const val FRAUNCES_SOFT = 0f
    const val DM_SANS_OPTICAL_SIZE = 14f

    private fun style(weight: FontWeight, size: Int, lineHeight: Int, letterSpacing: Float) = TextStyle(
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = lineHeight.sp,
        letterSpacing = letterSpacing.sp,
    )
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun frauncesFamily(style: TextStyle): FontFamily {
    val weight = style.fontWeight ?: FontWeight.SemiBold
    return FontFamily(
        Font(
            resource = Res.font.fraunces_variable,
            weight = weight,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(weight.weight),
                FontVariation.Setting("opsz", style.fontSize.value),
                FontVariation.Setting("WONK", TypeTokens.FRAUNCES_WONK),
                FontVariation.Setting("SOFT", TypeTokens.FRAUNCES_SOFT),
            ),
        ),
    )
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun dmSansFamily(): FontFamily = FontFamily(
    listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold).map { weight ->
        Font(
            resource = Res.font.dmsans_variable,
            weight = weight,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(weight.weight),
                FontVariation.Setting("opsz", TypeTokens.DM_SANS_OPTICAL_SIZE),
            ),
        )
    },
)

/** The M3 [Typography]; slots Figma does not define keep the M3 defaults in DM Sans. */
@Composable
internal fun verbeClairTypography(): Typography {
    val dmSans = dmSansFamily()
    val defaults = Typography()
    return Typography(
        displayLarge = defaults.displayLarge.copy(fontFamily = dmSans),
        displayMedium = defaults.displayMedium.copy(fontFamily = dmSans),
        headlineLarge = defaults.headlineLarge.copy(fontFamily = dmSans),
        headlineMedium = defaults.headlineMedium.copy(fontFamily = dmSans),
        labelSmall = defaults.labelSmall.copy(fontFamily = dmSans),
        displaySmall = TypeTokens.DisplayVerbLarge.copy(fontFamily = frauncesFamily(TypeTokens.DisplayVerbLarge)),
        headlineSmall = TypeTokens.HeadlineSmall.copy(fontFamily = frauncesFamily(TypeTokens.HeadlineSmall)),
        titleLarge = TypeTokens.TitleLarge.copy(fontFamily = dmSans),
        titleMedium = TypeTokens.TitleMedium.copy(fontFamily = dmSans),
        titleSmall = TypeTokens.TitleSmall.copy(fontFamily = dmSans),
        labelLarge = TypeTokens.LabelLarge.copy(fontFamily = dmSans),
        labelMedium = TypeTokens.LabelMedium.copy(fontFamily = dmSans),
        bodyLarge = TypeTokens.BodyLarge.copy(fontFamily = dmSans),
        bodyMedium = TypeTokens.BodyMedium.copy(fontFamily = dmSans),
        bodySmall = TypeTokens.BodySmall.copy(fontFamily = dmSans),
    )
}

@Composable
internal fun verbeClairExtendedTypography(): ExtendedTypography {
    val dmSans = dmSansFamily()
    return ExtendedTypography(
        titleVerb = TypeTokens.TitleVerb.copy(fontFamily = frauncesFamily(TypeTokens.TitleVerb)),
        labelOverline = TypeTokens.LabelOverline.copy(fontFamily = dmSans),
        conjugationForm = TypeTokens.ConjugationForm.copy(fontFamily = dmSans),
        conjugationEnding = TypeTokens.ConjugationEnding.copy(fontFamily = dmSans),
    )
}
