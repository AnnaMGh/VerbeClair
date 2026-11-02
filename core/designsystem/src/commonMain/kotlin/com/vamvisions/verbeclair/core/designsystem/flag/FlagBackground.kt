package com.vamvisions.verbeclair.core.designsystem.flag

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import com.vamvisions.verbeclair.core.designsystem.generated.resources.Res
import com.vamvisions.verbeclair.core.designsystem.generated.resources.flag_stripe_blue
import com.vamvisions.verbeclair.core.designsystem.generated.resources.flag_stripe_white
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import org.jetbrains.compose.resources.painterResource
import kotlin.math.max

/**
 * Screen background (Notion N-14, Figma `Background/Flag stripe` White 95:213): the background colour, then the
 * White stripe scaled to cover the node, centred, at [VerbeClairTheme.flagOpacity].
 *
 * Apply it once at the screen root, outside scrolling content, so the flag stays fixed. It only draws, so screen
 * readers never see it.
 */
@Composable
fun Modifier.flagScreenBackground(): Modifier =
    flagBackground(
        stripe = painterResource(Res.drawable.flag_stripe_white),
        background = MaterialTheme.colorScheme.background,
        alpha = VerbeClairTheme.flagOpacity,
        placement = FlagPlacement.Screen,
    )

/**
 * Bottom-bar background (Figma `Background/Flag stripe` Blue 95:211): the background colour, so the bar is opaque,
 * then the Blue stripe rotated 90° and scaled to cover, at [VerbeClairTheme.flagOpacity].
 */
@Composable
fun Modifier.flagBarBackground(): Modifier =
    flagBackground(
        stripe = painterResource(Res.drawable.flag_stripe_blue),
        background = MaterialTheme.colorScheme.background,
        alpha = VerbeClairTheme.flagOpacity,
        placement = FlagPlacement.Bar,
    )

/**
 * Navigation-rail background (Notion N-14, Figma `Background/Flag stripe` Blue 95:211 behind the rail): the Blue
 * stripe, unrotated, scaled to cover the rail and centred, at [VerbeClairTheme.flagOpacity]. It has no background
 * colour of its own, so the screen background shows through around the stripe.
 */
@Composable
fun Modifier.flagRailBackground(): Modifier =
    flagBackground(
        stripe = painterResource(Res.drawable.flag_stripe_blue),
        background = null,
        alpha = VerbeClairTheme.flagOpacity,
        placement = FlagPlacement.Rail,
    )

/** How each flag background places its stripe: the bar's stripe is rotated by 90°, the others are not. */
internal enum class FlagPlacement(val rotated: Boolean) {
    Screen(rotated = false),
    Bar(rotated = true),
    Rail(rotated = false),
}

private fun Modifier.flagBackground(
    stripe: Painter,
    background: Color?,
    alpha: Float,
    placement: FlagPlacement,
): Modifier =
    drawBehind {
        if (background != null) drawRect(background)
        clipRect { drawCover(stripe, alpha, placement.rotated) }
    }

/**
 * The size, before rotation, to draw an image of [intrinsic] size so that it covers [area], keeping its aspect ratio.
 * A 90° rotation swaps the image's width and height on screen.
 */
internal fun coverSize(
    intrinsic: Size,
    area: Size,
    rotated: Boolean,
): Size {
    val coverWidth = if (rotated) intrinsic.height else intrinsic.width
    val coverHeight = if (rotated) intrinsic.width else intrinsic.height
    val scale = max(area.width / coverWidth, area.height / coverHeight)
    return Size(intrinsic.width * scale, intrinsic.height * scale)
}

/** Draws [painter] centred and scaled to cover the whole draw area, optionally rotated by 90°. */
private fun DrawScope.drawCover(
    painter: Painter,
    alpha: Float,
    rotated: Boolean,
) {
    val intrinsic = painter.intrinsicSize
    if (intrinsic.isUnspecified() || intrinsic.width <= 0f || intrinsic.height <= 0f) return
    val drawn = coverSize(intrinsic, size, rotated)
    rotate(degrees = if (rotated) 90f else 0f, pivot = center) {
        translate(left = center.x - drawn.width / 2f, top = center.y - drawn.height / 2f) {
            with(painter) { draw(size = drawn, alpha = alpha) }
        }
    }
}

private fun Size.isUnspecified() = this == Size.Unspecified
