package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

// Figma row and bar lines (`border` 1 px on `outline-variant`).
private val LineWidth = 1.dp

/** Draws the 1 dp line at the bottom of a list row (verb cell, skeleton), over any background before it. */
internal fun Modifier.bottomLine(color: Color): Modifier =
    drawBehind {
        val stroke = LineWidth.toPx()
        drawRect(color, topLeft = Offset(0f, size.height - stroke), size = Size(size.width, stroke))
    }

/** Draws the 1 dp line at the top of the bottom bar. */
internal fun Modifier.topLine(color: Color): Modifier =
    drawBehind {
        drawRect(color, size = Size(size.width, LineWidth.toPx()))
    }

/** Draws the 1 dp line on the end side of the navigation rail (the right in left-to-right layouts). */
internal fun Modifier.endLine(color: Color): Modifier =
    drawBehind {
        val stroke = LineWidth.toPx()
        val x = if (layoutDirection == LayoutDirection.Ltr) size.width - stroke else 0f
        drawRect(color, topLeft = Offset(x, 0f), size = Size(stroke, size.height))
    }
