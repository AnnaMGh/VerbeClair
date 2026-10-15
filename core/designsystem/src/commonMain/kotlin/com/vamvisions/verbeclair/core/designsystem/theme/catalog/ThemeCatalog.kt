package com.vamvisions.verbeclair.core.designsystem.theme.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.designsystem.theme.elevation

// Theme catalogue: mirrors the Figma Foundations frames so the theme can be checked against them.
// Sizes below are the catalogue's own Figma frame geometry, not design tokens.
private val CatalogPadding = 32.dp
private val CatalogCorner = 24.dp
private val SwatchWidth = 136.dp
private val SwatchHeight = 64.dp
private val SwatchGap = 6.dp
private val TypeLabelWidth = 240.dp
private val RadiusSampleSize = 72.dp
private val RadiusSampleBorder = 1.5.dp
private val SpacingSampleCorner = 2.dp
private val ElevationSampleWidth = 200.dp
private val ElevationSampleHeight = 96.dp

private class Swatch(
    val role: String,
    val lightPrimitive: String,
    val darkPrimitive: String,
    val color: @Composable () -> Color,
)

private class SwatchGroup(val title: String, val swatches: List<Swatch>)

private val swatchGroups = listOf(
    SwatchGroup(
        "Brand",
        listOf(
            Swatch("primary", "blue/40", "blue/80") { MaterialTheme.colorScheme.primary },
            Swatch("on-primary", "neutral/100", "blue/20") { MaterialTheme.colorScheme.onPrimary },
            Swatch("primary-container", "blue/90", "blue/30") { MaterialTheme.colorScheme.primaryContainer },
            Swatch("on-primary-container", "blue/10", "blue/90") { MaterialTheme.colorScheme.onPrimaryContainer },
            Swatch("secondary-container", "indigo-gray/90", "indigo-gray/30") { MaterialTheme.colorScheme.secondaryContainer },
            Swatch("on-secondary-container", "indigo-gray/10", "indigo-gray/90") { MaterialTheme.colorScheme.onSecondaryContainer },
            Swatch("inverse-primary", "blue/80", "blue/40") { MaterialTheme.colorScheme.inversePrimary },
        ),
    ),
    SwatchGroup(
        "Verb ending",
        listOf(
            Swatch("ending", "coral/40", "coral/80") { VerbeClairTheme.extendedColors.ending },
            Swatch("ending-container", "coral/90", "coral/20") { VerbeClairTheme.extendedColors.endingContainer },
        ),
    ),
    SwatchGroup(
        "Surfaces",
        listOf(
            Swatch("background", "neutral/98", "neutral/6") { MaterialTheme.colorScheme.background },
            Swatch("surface", "neutral/100", "neutral/6") { MaterialTheme.colorScheme.surface },
            Swatch("surface-container-low", "neutral/98", "neutral/10") { MaterialTheme.colorScheme.surfaceContainerLow },
            Swatch("surface-container", "neutral/95", "neutral/12") { MaterialTheme.colorScheme.surfaceContainer },
            Swatch("surface-container-high", "neutral/92", "neutral/17") { MaterialTheme.colorScheme.surfaceContainerHigh },
            Swatch("surface-container-highest", "neutral/90", "neutral/22") { MaterialTheme.colorScheme.surfaceContainerHighest },
            Swatch("inverse-surface", "neutral/17", "neutral/90") { MaterialTheme.colorScheme.inverseSurface },
            Swatch("inverse-on-surface", "neutral/95", "neutral/17") { MaterialTheme.colorScheme.inverseOnSurface },
            Swatch("scrim", "neutral/0", "neutral/0") { MaterialTheme.colorScheme.scrim },
        ),
    ),
    SwatchGroup(
        "Content & lines",
        listOf(
            Swatch("on-surface", "neutral/10", "neutral/90") { MaterialTheme.colorScheme.onSurface },
            Swatch("on-surface-variant", "neutral/30", "neutral/80") { MaterialTheme.colorScheme.onSurfaceVariant },
            Swatch("outline", "neutral/50", "neutral/60") { MaterialTheme.colorScheme.outline },
            Swatch("outline-variant", "neutral/80", "neutral/30") { MaterialTheme.colorScheme.outlineVariant },
        ),
    ),
    SwatchGroup(
        "Feedback",
        listOf(
            Swatch("error", "red/40", "red/80") { MaterialTheme.colorScheme.error },
            Swatch("on-error", "neutral/100", "red/20") { MaterialTheme.colorScheme.onError },
            Swatch("error-container", "red/90", "red/30") { MaterialTheme.colorScheme.errorContainer },
            Swatch("on-error-container", "red/10", "red/90") { MaterialTheme.colorScheme.onErrorContainer },
            Swatch("success", "green/40", "green/80") { VerbeClairTheme.extendedColors.success },
        ),
    ),
)

private fun Color.hexLabel(): String =
    "#" + (toArgb() and 0xFFFFFF).toString(16).uppercase().padStart(6, '0')

@Composable
private fun CatalogFrame(
    background: Color,
    bordered: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(CatalogCorner)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(background, shape)
            .then(if (bordered) Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape) else Modifier)
            .padding(CatalogPadding),
        verticalArrangement = Arrangement.spacedBy(VerbeClairTheme.spacing.space20),
    ) { content() }
}

@Composable
private fun FrameTitle(text: String) {
    Text(text, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        style = VerbeClairTheme.typography.labelOverline,
        color = MaterialTheme.colorScheme.primary,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ColourCatalog(darkTheme: Boolean, modifier: Modifier = Modifier) {
    CatalogFrame(background = MaterialTheme.colorScheme.background, bordered = false, modifier = modifier) {
        FrameTitle(if (darkTheme) "Colour · Dark" else "Colour · Light")
        swatchGroups.forEach { group ->
            SectionLabel(group.title)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(VerbeClairTheme.spacing.space16),
                verticalArrangement = Arrangement.spacedBy(VerbeClairTheme.spacing.space16),
            ) {
                group.swatches.forEach { swatch ->
                    SwatchItem(swatch, if (darkTheme) swatch.darkPrimitive else swatch.lightPrimitive)
                }
            }
        }
    }
}

@Composable
private fun SwatchItem(swatch: Swatch, primitive: String) {
    val color = swatch.color()
    val shape = MaterialTheme.shapes.medium
    Column(verticalArrangement = Arrangement.spacedBy(SwatchGap)) {
        Box(
            Modifier
                .size(SwatchWidth, SwatchHeight)
                .background(color, shape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
        )
        Text(swatch.role, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(
            "${color.hexLabel()} · $primitive",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun TypographyCatalog(modifier: Modifier = Modifier) {
    val type = MaterialTheme.typography
    val extended = VerbeClairTheme.typography
    val samples: List<Triple<String, TextStyle, String>> = listOf(
        Triple("Display/Verb Large\nFraunces SemiBold · 34/40", type.displaySmall, "s'appeler"),
        Triple("Headline/Small\nFraunces SemiBold · 24/32", type.headlineSmall, "Indicatif"),
        Triple("Title/Verb\nFraunces SemiBold · 20/26", extended.titleVerb, "être"),
        Triple("Title/Large\nDM Sans SemiBold · 22/28", type.titleLarge, "No verbs match your search"),
        Triple("Title/Medium\nDM Sans SemiBold · 16/24", type.titleMedium, "Group by"),
        Triple("Title/Small\nDM Sans SemiBold · 14/20", type.titleSmall, "Passé composé"),
        Triple("Label/Overline\nDM Sans Bold · 12/16", extended.labelOverline, "1st group".uppercase()),
        Triple("Label/Large\nDM Sans Medium · 14/20", type.labelLarge, "Clear filters"),
        Triple("Label/Medium\nDM Sans Medium · 12/16", type.labelMedium, "5,684 verbs"),
        Triple("Body/Large\nDM Sans Regular · 16/24", type.bodyLarge, "to end; to finish; to terminate"),
        Triple("Body/Medium\nDM Sans Regular · 14/20", type.bodyMedium, "1st group · avoir · non-reflexive"),
        Triple("Body/Small\nDM Sans Regular · 12/16", type.bodySmall, "Last updated 30 Sept 2026"),
        Triple("Conjugation/Form\nDM Sans Regular · 16/24", extended.conjugationForm, "que je termine"),
    )
    CatalogFrame(background = MaterialTheme.colorScheme.surface, bordered = true, modifier = modifier) {
        FrameTitle("Typography · Fraunces (verbs, moods) + DM Sans (UI)")
        samples.forEach { (label, style, sample) ->
            TypeRow(label) { Text(sample, style = style, color = MaterialTheme.colorScheme.onSurface, maxLines = 1) }
        }
        TypeRow("Conjugation/Ending\nDM Sans Bold · 16/24") {
            Text("e", style = extended.conjugationEnding, color = VerbeClairTheme.extendedColors.ending)
        }
    }
}

@Composable
private fun TypeRow(label: String, sample: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(VerbeClairTheme.spacing.space24),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            modifier = Modifier.width(TypeLabelWidth),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        sample()
    }
}

@Composable
internal fun SpacingRadiusElevationCatalog(modifier: Modifier = Modifier) {
    val spacing = VerbeClairTheme.spacing
    val radius = VerbeClairTheme.radius
    CatalogFrame(background = MaterialTheme.colorScheme.surface, bordered = true, modifier = modifier) {
        FrameTitle("Spacing")
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space24), verticalAlignment = Alignment.Bottom) {
            listOf(
                2 to spacing.space2, 4 to spacing.space4, 8 to spacing.space8, 12 to spacing.space12,
                16 to spacing.space16, 20 to spacing.space20, 24 to spacing.space24, 32 to spacing.space32,
                48 to spacing.space48,
            ).forEach { (name, size) ->
                ScaleSample(name.toString()) {
                    Box(
                        Modifier
                            .size(size)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(SpacingSampleCorner)),
                    )
                }
            }
        }
        FrameTitle("Radius")
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.space24)) {
            listOf(
                "xs · 4" to RoundedCornerShape(radius.xs),
                "sm · 8" to RoundedCornerShape(radius.sm),
                "md · 12" to RoundedCornerShape(radius.md),
                "lg · 16" to RoundedCornerShape(radius.lg),
                "xl · 28" to RoundedCornerShape(radius.xl),
                "full · full" to radius.full,
            ).forEach { (name, shape) ->
                ScaleSample(name) {
                    Box(
                        Modifier
                            .size(RadiusSampleSize)
                            .background(MaterialTheme.colorScheme.primaryContainer, shape)
                            .border(RadiusSampleBorder, MaterialTheme.colorScheme.primary, shape),
                    )
                }
            }
        }
        FrameTitle("Elevation")
        val sheetShape = MaterialTheme.shapes.large
        Box(
            Modifier
                .size(ElevationSampleWidth, ElevationSampleHeight)
                .elevation(VerbeClairTheme.elevation.level2, sheetShape)
                .background(MaterialTheme.colorScheme.surface, sheetShape),
        )
        Text(
            "Elevation/Level 2 — bottom sheet, snackbar",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ScaleSample(label: String, sample: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VerbeClairTheme.spacing.space8),
    ) {
        sample()
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=8-4
@Preview(widthDp = 824, heightDp = 1164)
@Composable
private fun ColourLightPreview() {
    VerbeClairTheme(darkTheme = false) { ColourCatalog(darkTheme = false) }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=8-124
@Preview(widthDp = 824, heightDp = 1164)
@Composable
private fun ColourDarkPreview() {
    VerbeClairTheme(darkTheme = true) { ColourCatalog(darkTheme = true) }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=8-244
@Preview(widthDp = 680, heightDp = 834)
@Composable
private fun TypographyLightPreview() {
    VerbeClairTheme(darkTheme = false) { TypographyCatalog() }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=8-244
@Preview(widthDp = 680, heightDp = 834)
@Composable
private fun TypographyDarkPreview() {
    VerbeClairTheme(darkTheme = true) { TypographyCatalog() }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=8-288
@Preview(widthDp = 618, heightDp = 562)
@Composable
private fun SpacingRadiusElevationLightPreview() {
    VerbeClairTheme(darkTheme = false) { SpacingRadiusElevationCatalog() }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=8-288
@Preview(widthDp = 618, heightDp = 562)
@Composable
private fun SpacingRadiusElevationDarkPreview() {
    VerbeClairTheme(darkTheme = true) { SpacingRadiusElevationCatalog() }
}
