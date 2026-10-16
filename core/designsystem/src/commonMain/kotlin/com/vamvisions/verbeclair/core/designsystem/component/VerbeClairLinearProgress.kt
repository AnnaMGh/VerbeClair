package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

// Figma `Progress/Linear` (7:151) height.
private val ProgressHeight = 4.dp

/**
 * Linear progress (Figma 7:151): an indeterminate 4 dp bar, a `primary` indicator on a `primary-container` track,
 * used for the first download when the database is empty (Notion L-10).
 */
@Composable
fun VerbeClairLinearProgress(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    LinearProgressIndicator(
        modifier = modifier.fillMaxWidth().height(ProgressHeight),
        color = colors.primary,
        trackColor = colors.primaryContainer,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
    )
}
