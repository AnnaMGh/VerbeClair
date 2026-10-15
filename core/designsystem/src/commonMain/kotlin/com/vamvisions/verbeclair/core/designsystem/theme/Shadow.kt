package com.vamvisions.verbeclair.core.designsystem.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow

/** Draws every [ShadowSpec] of an elevation level (e.g. [Elevation.level2]) behind [shape]. */
fun Modifier.elevation(shadows: List<ShadowSpec>, shape: Shape): Modifier =
    shadows.fold(this) { modifier, spec ->
        modifier.dropShadow(
            shape = shape,
            shadow = Shadow(radius = spec.blur, color = spec.color, offset = spec.offset),
        )
    }
