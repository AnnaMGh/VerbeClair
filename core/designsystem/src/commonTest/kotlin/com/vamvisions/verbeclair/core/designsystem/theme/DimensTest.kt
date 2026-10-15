package com.vamvisions.verbeclair.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class DimensTest {

    @Test
    fun spacingMatchesFigma() {
        with(Spacing()) {
            assertEquals(
                listOf(2, 4, 8, 12, 16, 20, 24, 32, 48).map { it.dp },
                listOf(space2, space4, space8, space12, space16, space20, space24, space32, space48),
            )
        }
    }

    @Test
    fun radiusMatchesFigma() {
        with(Radius()) {
            assertEquals(listOf(4, 8, 12, 16, 28).map { it.dp }, listOf(xs, sm, md, lg, xl))
            assertEquals(RoundedCornerShape(percent = 50), full)
        }
    }

    @Test
    fun fullRadiusRoundsTheShortSide() {
        val density = Density(1f)
        val size = Size(width = 200f, height = 40f)
        assertEquals(20f, Radius().full.topStart.toPx(size, density))
    }

    @Test
    fun shapesMapToRadiusTokens() {
        with(verbeClairShapes()) {
            assertEquals(RoundedCornerShape(4.dp), extraSmall)
            assertEquals(RoundedCornerShape(8.dp), small)
            assertEquals(RoundedCornerShape(12.dp), medium)
            assertEquals(RoundedCornerShape(16.dp), large)
            assertEquals(RoundedCornerShape(28.dp), extraLarge)
        }
    }

    @Test
    fun elevationLevel2MatchesFigma() {
        val (near, far) = Elevation().level2
        assertEquals(0.dp, near.offset.x)
        assertEquals(2.dp, near.offset.y)
        assertEquals(6.dp, near.blur)
        assertEquals(0x1F0D0F33, near.color.toArgb())
        assertEquals(0.dp, far.offset.x)
        assertEquals(8.dp, far.offset.y)
        assertEquals(24.dp, far.blur)
        assertEquals(0x1A0D0F33, far.color.toArgb())
    }

    @Test
    fun flagOpacityIsTwelvePercent() {
        assertEquals(0.12f, FLAG_OPACITY)
    }
}
