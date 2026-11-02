package com.vamvisions.verbeclair.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/** Figma `Spacing` collection: `space/2` … `space/48`. */
@Immutable
data class Spacing(
    val space2: Dp = 2.dp,
    val space4: Dp = 4.dp,
    val space8: Dp = 8.dp,
    val space12: Dp = 12.dp,
    val space16: Dp = 16.dp,
    val space20: Dp = 20.dp,
    val space24: Dp = 24.dp,
    val space32: Dp = 32.dp,
    val space48: Dp = 48.dp,
)

/** Figma `Radius` collection. `full` (999 in Figma) is a fully rounded shape. */
@Immutable
data class Radius(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 28.dp,
    val full: RoundedCornerShape = RoundedCornerShape(percent = 50),
)

internal fun verbeClairShapes(radius: Radius = Radius()) =
    Shapes(
        extraSmall = RoundedCornerShape(radius.xs),
        small = RoundedCornerShape(radius.sm),
        medium = RoundedCornerShape(radius.md),
        large = RoundedCornerShape(radius.lg),
        extraLarge = RoundedCornerShape(radius.xl),
    )

/** One drop shadow of a Figma effect style. */
@Immutable
data class ShadowSpec(
    val offset: DpOffset,
    val blur: Dp,
    val color: Color,
)

/** Figma effect style `Elevation/Level 2`: two drop shadows on #0D0F33. */
@Immutable
data class Elevation(
    val level2: List<ShadowSpec> =
        listOf(
            ShadowSpec(offset = DpOffset(0.dp, 2.dp), blur = 6.dp, color = Color(0x1F0D0F33)),
            ShadowSpec(offset = DpOffset(0.dp, 8.dp), blur = 24.dp, color = Color(0x1A0D0F33)),
        ),
)

/** Figma variable `opacity/flag` (12) and Notion N-14: opacity of the flag background. */
internal const val FLAG_OPACITY = 0.12f
