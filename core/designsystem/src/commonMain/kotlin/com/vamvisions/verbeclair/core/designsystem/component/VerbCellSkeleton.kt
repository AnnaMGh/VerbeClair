package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme

// Figma `Verb cell skeleton` (7:49) placeholder bars.
private val InfinitiveBarWidth = 120.dp
private val InfinitiveBarHeight = 18.dp
private val MetaBarWidth = 210.dp
private val MetaBarHeight = 12.dp

/**
 * Verb cell skeleton (Figma 7:49, Notion N-2): two rounded `surface-container-highest` bars with the verb cell's
 * bottom line, shown while a page of the list loads. It has no semantics, so screen readers skip it.
 */
@Composable
fun VerbCellSkeleton(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val barShape = MaterialTheme.shapes.extraSmall
    Column(
        modifier = modifier
            .fillMaxWidth()
            .bottomLine(colors.outlineVariant)
            .padding(VerbeClairTheme.spacing.space16),
        verticalArrangement = Arrangement.spacedBy(VerbeClairTheme.spacing.space8),
    ) {
        Box(Modifier.size(InfinitiveBarWidth, InfinitiveBarHeight).background(colors.surfaceContainerHighest, barShape))
        Box(Modifier.size(MetaBarWidth, MetaBarHeight).background(colors.surfaceContainerHighest, barShape))
    }
}
