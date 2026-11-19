package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme

// Figma `First steps card` (151:248): the close icon is 18 dp in an 8 dp padded box (34 dp). The button keeps its 48 dp
// Material 3 touch target; it overflows the 34 dp box the card lays out, so the card keeps the Figma height.
private val CloseBoxSize = 34.dp
private val CloseIconSize = 18.dp

/** Where the two buttons of the [FirstStepsCard] sit on their row. */
enum class CardActionsAlignment {
    /** Left-aligned: phone portrait and tablet. */
    Start,

    /** Centred: phone landscape (Figma PL-07), the title and body stay left-aligned. */
    Center,
}

/**
 * First steps card (Figma 151:248): a generic tip card on `secondary-container` with a [title] and a close button, a
 * [body] and two buttons, the first Filled and the second Tonal. It has no fixed wording, so other screens can reuse it;
 * the caller gives every text. [closeLabel] is the accessible label of the close button.
 */
@Composable
fun FirstStepsCard(
    title: String,
    body: String,
    primaryLabel: String,
    secondaryLabel: String,
    closeLabel: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionsAlignment: CardActionsAlignment = CardActionsAlignment.Start,
) {
    val colors = MaterialTheme.colorScheme
    val spacing = VerbeClairTheme.spacing
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(colors.secondaryContainer, MaterialTheme.shapes.large)
                .padding(
                    PaddingValues(
                        start = spacing.space16,
                        top = spacing.space16,
                        end = spacing.space8,
                        bottom = spacing.space16,
                    ),
                ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSecondaryContainer,
            )
            Box(modifier = Modifier.size(CloseBoxSize)) {
                IconButton(onClick = onCloseClick, modifier = Modifier.wrapContentSize(unbounded = true)) {
                    Icon(
                        imageVector = VerbeClairIcons.Close,
                        contentDescription = closeLabel,
                        modifier = Modifier.size(CloseIconSize),
                        tint = colors.onSecondaryContainer,
                    )
                }
            }
        }
        Text(
            text = body,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSecondaryContainer,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = spacing.space16),
            horizontalArrangement =
                when (actionsAlignment) {
                    CardActionsAlignment.Start -> Arrangement.spacedBy(spacing.space8, Alignment.Start)
                    CardActionsAlignment.Center -> Arrangement.spacedBy(spacing.space8, Alignment.CenterHorizontally)
                },
        ) {
            VerbeClairButton(text = primaryLabel, onClick = onPrimaryClick)
            VerbeClairButton(text = secondaryLabel, onClick = onSecondaryClick, style = ButtonStyle.Tonal)
        }
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=151-248
@Preview(widthDp = 361)
@Composable
private fun FirstStepsCardComponentPreview() {
    VerbeClairTheme(darkTheme = false) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            FirstStepsCard(
                title = "New to French verbs?",
                body = "Start with the most common ones.",
                primaryLabel = "Test être",
                secondaryLabel = "Most common first",
                closeLabel = "Close tip",
                onPrimaryClick = {},
                onSecondaryClick = {},
                onCloseClick = {},
            )
        }
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=152-15348
@Preview(widthDp = 361)
@Composable
private fun FirstStepsCardPortraitLightPreview() {
    VerbeClairTheme(darkTheme = false) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            FirstStepsCard(
                title = "New to French verbs?",
                body = "Start with the most common ones.",
                primaryLabel = "Test être",
                secondaryLabel = "Most common first",
                closeLabel = "Close tip",
                onPrimaryClick = {},
                onSecondaryClick = {},
                onCloseClick = {},
            )
        }
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=154-16242
@Preview(widthDp = 361)
@Composable
private fun FirstStepsCardPortraitDarkPreview() {
    VerbeClairTheme(darkTheme = true) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            FirstStepsCard(
                title = "New to French verbs?",
                body = "Start with the most common ones.",
                primaryLabel = "Test être",
                secondaryLabel = "Most common first",
                closeLabel = "Close tip",
                onPrimaryClick = {},
                onSecondaryClick = {},
                onCloseClick = {},
            )
        }
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=195-17561
@Preview(widthDp = 740)
@Composable
private fun FirstStepsCardLandscapeLightPreview() {
    VerbeClairTheme(darkTheme = false) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            FirstStepsCard(
                title = "New to French verbs?",
                body = "Start with the most common ones.",
                primaryLabel = "Test être",
                secondaryLabel = "Most common first",
                closeLabel = "Close tip",
                onPrimaryClick = {},
                onSecondaryClick = {},
                onCloseClick = {},
                actionsAlignment = CardActionsAlignment.Center,
            )
        }
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=195-17859
@Preview(widthDp = 740)
@Composable
private fun FirstStepsCardLandscapeDarkPreview() {
    VerbeClairTheme(darkTheme = true) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            FirstStepsCard(
                title = "New to French verbs?",
                body = "Start with the most common ones.",
                primaryLabel = "Test être",
                secondaryLabel = "Most common first",
                closeLabel = "Close tip",
                onPrimaryClick = {},
                onSecondaryClick = {},
                onCloseClick = {},
                actionsAlignment = CardActionsAlignment.Center,
            )
        }
    }
}
