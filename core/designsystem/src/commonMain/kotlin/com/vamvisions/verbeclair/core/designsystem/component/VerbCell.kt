package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.generated.resources.Res
import com.vamvisions.verbeclair.core.designsystem.generated.resources.favourite_add
import com.vamvisions.verbeclair.core.designsystem.generated.resources.favourite_remove
import com.vamvisions.verbeclair.core.designsystem.generated.resources.test_start
import com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import org.jetbrains.compose.resources.stringResource

// Figma `Verb cell` (7:41) test and favourite buttons: 10 dp around a 24 dp icon.
private val CellButtonSize = 44.dp

/**
 * Verb cell (Figma 7:41, Notion L-8): the [infinitive] in Title/Verb over the [meta] line, then on the right the
 * last test [score] when there is one, the test button (L-14) and the favourite button (L-13), with a 1 dp
 * `outline-variant` line at the bottom.
 *
 * - [matchedForm] (L-4): shown right after the infinitive with one space, in Label/Large `primary`; one text, so it
 *   wraps to the next line when the infinitive fills the line, and a screen reader reads both as one phrase.
 * - [selected] (tablet list–detail, N-15): `secondary-container` background, and announced as selected so colour
 *   is not the only cue.
 * - Tapping a button reports only that button, never [onClick].
 */
@Composable
fun VerbCell(
    infinitive: String,
    meta: String,
    isFavourite: Boolean,
    onClick: () -> Unit,
    onTestClick: () -> Unit,
    onFavouriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    matchedForm: String? = null,
    score: String? = null,
    selected: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val spacing = VerbeClairTheme.spacing
    val formStyle = MaterialTheme.typography.labelLarge.toSpanStyle().copy(color = colors.primary)
    val title = if (matchedForm == null) {
        AnnotatedString(infinitive)
    } else {
        buildAnnotatedString {
            append(infinitive)
            append(' ')
            withStyle(formStyle) { append(matchedForm) }
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (selected) colors.secondaryContainer else Color.Transparent)
            .bottomLine(colors.outlineVariant)
            .clickable(onClick = onClick)
            .then(if (selected) Modifier.semantics { this.selected = true } else Modifier)
            .padding(start = spacing.space16, end = spacing.space4, top = spacing.space12, bottom = spacing.space12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space12),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.space2),
        ) {
            Text(text = title, style = VerbeClairTheme.typography.titleVerb, color = colors.onSurface)
            Text(text = meta, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (score != null) {
                Text(text = score, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
            }
            // 44 dp is the Figma size (and Apple's minimum); M3 would grow the buttons to 48 dp and change the
            // cell height.
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides CellButtonSize) {
                IconButton(onClick = onTestClick) {
                    Icon(
                        imageVector = VerbeClairIcons.Exam,
                        contentDescription = stringResource(Res.string.test_start),
                        tint = colors.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onFavouriteClick) {
                    Icon(
                        imageVector = if (isFavourite) VerbeClairIcons.StarFilled else VerbeClairIcons.Star,
                        contentDescription = stringResource(
                            if (isFavourite) Res.string.favourite_remove else Res.string.favourite_add,
                        ),
                        tint = if (isFavourite) VerbeClairTheme.extendedColors.ending else colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
