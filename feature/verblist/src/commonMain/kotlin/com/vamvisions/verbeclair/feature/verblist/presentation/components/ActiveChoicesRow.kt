package com.vamvisions.verbeclair.feature.verblist.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vamvisions.verbeclair.core.designsystem.component.VerbeClairFilterChip
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.feature.verblist.presentation.ActiveChoice
import com.vamvisions.verbeclair.feature.verblist.presentation.label
import org.jetbrains.compose.resources.stringResource

/**
 * Active choices row (L-6, Figma 10:320): one selected chip per active choice, scrolling horizontally
 * when it does not fit. Tapping a chip removes that choice. Shown only when [choices] is not empty.
 */
@Composable
fun ActiveChoicesRow(
    choices: List<ActiveChoice>,
    onRemove: (ActiveChoice) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = VerbeClairTheme.spacing
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                // Figma has 8 dp under the 32 dp chips; the chip's 48 dp touch target already adds it (and 8 dp above).
                .padding(horizontal = spacing.space16),
        horizontalArrangement = Arrangement.spacedBy(spacing.space8),
    ) {
        choices.forEach { choice ->
            VerbeClairFilterChip(
                label = stringResource(choice.label()),
                selected = true,
                onClick = { onRemove(choice) },
            )
        }
    }
}
