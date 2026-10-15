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
fun Modifier.flagScreenBackground(): Modifier = flagBackground(
    stripe = painterResource(Res.drawable.flag_stripe_white),
    background = MaterialTheme.colorScheme.background,
    alpha = VerbeClairTheme.flagOpacity,
    rotated = false,
)

/**
 * Bottom-bar and navigation-rail background (Figma `Background/Flag stripe` Blue 95:211): the background colour,
 * so the bar is opaque, then the Blue stripe rotated 90° and scaled to cover, at [VerbeClairTheme.flagOpacity].
 */
@Composable
fun Modifier.flagBarBackground(): Modifier = flagBackground(
    stripe = painterResource(Res.drawable.flag_stripe_blue),
    background = MaterialTheme.colorScheme.background,
    alpha = VerbeClairTheme.flagOpacity,
    rotated = true,
)

private fun Modifier.flagBackground(stripe: Painter, background: Color, alpha: Float, rotated: Boolean): Modifier =
    drawBehind {
        drawRect(background)
        clipRect { drawCover(stripe, alpha, rotated) }
    }

/** Draws [painter] centred and scaled to cover the whole draw area, optionally rotated by 90°. */
private fun DrawScope.drawCover(painter: Painter, alpha: Float, rotated: Boolean) {
    val intrinsic = painter.intrinsicSize
    if (intrinsic.isUnspecified() || intrinsic.width <= 0f || intrinsic.height <= 0f) return
    // A 90° rotation swaps the image's width and height on screen.
    val coverWidth = if (rotated) intrinsic.height else intrinsic.width
    val coverHeight = if (rotated) intrinsic.width else intrinsic.height
    val scale = max(size.width / coverWidth, size.height / coverHeight)
    val drawn = Size(intrinsic.width * scale, intrinsic.height * scale)
    rotate(degrees = if (rotated) 90f else 0f, pivot = center) {
        translate(left = center.x - drawn.width / 2f, top = center.y - drawn.height / 2f) {
            with(painter) { draw(size = drawn, alpha = alpha) }
        }
    }
}

private fun Size.isUnspecified() = this == Size.Unspecified
