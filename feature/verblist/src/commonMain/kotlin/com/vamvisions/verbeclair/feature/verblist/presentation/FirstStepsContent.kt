package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.component.CardActionsAlignment
import com.vamvisions.verbeclair.core.designsystem.component.FirstStepsCard
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.feature.verblist.generated.resources.Res
import com.vamvisions.verbeclair.feature.verblist.generated.resources.first_steps_body
import com.vamvisions.verbeclair.feature.verblist.generated.resources.first_steps_close
import com.vamvisions.verbeclair.feature.verblist.generated.resources.first_steps_most_common
import com.vamvisions.verbeclair.feature.verblist.generated.resources.first_steps_test_etre
import com.vamvisions.verbeclair.feature.verblist.generated.resources.first_steps_title
import com.vamvisions.verbeclair.feature.verblist.generated.resources.top_verbs_title
import org.jetbrains.compose.resources.stringResource

// Figma TP-07 (195:18534): the top 10 rows are 14 dp above and below the infinitive.
private val PanelRowPadding = 14.dp

/** Test tags of the first steps card and the top 10 panel, for UI tests. */
object FirstStepsTestTags {

    const val CARD = "verbList.firstSteps"
    const val PANEL = "verbList.topVerbsPanel"

    fun panelVerb(slug: String) = "verbList.topVerb.$slug"
}

/**
 * The first steps card as a grid item (Figma 152:15398): 16 dp at the sides and 8 dp above and below. The wording
 * lives here, because the design-system card has none. [alignment] is Center on phone landscape (PL-07).
 */
@Composable
internal fun FirstStepsCardItem(
    alignment: CardActionsAlignment,
    onAction: (VerbListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = VerbeClairTheme.spacing
    FirstStepsCard(
        title = stringResource(Res.string.first_steps_title),
        body = stringResource(Res.string.first_steps_body),
        primaryLabel = stringResource(Res.string.first_steps_test_etre),
        secondaryLabel = stringResource(Res.string.first_steps_most_common),
        closeLabel = stringResource(Res.string.first_steps_close),
        onPrimaryClick = { onAction(VerbListAction.OnFirstStepsTestEtre) },
        onSecondaryClick = { onAction(VerbListAction.OnFirstStepsMostCommon) },
        onCloseClick = { onAction(VerbListAction.OnFirstStepsClose) },
        modifier =
            modifier
                .padding(
                    horizontal = spacing.space16,
                    vertical = spacing.space8,
                ).testTag(FirstStepsTestTags.CARD),
        actionsAlignment = alignment,
    )
}

/**
 * The top 10 panel in the tablet right pane (Figma TP-07, 195:18534): a title and the 10 most frequent
 * non-reflexive verbs as plain rows, with no score, rank or chevron. A tap selects the verb like a cell tap.
 */
@Composable
internal fun TopVerbsPanel(
    verbs: List<VerbUi>,
    onVerbClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = VerbeClairTheme.spacing
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(
                    start = spacing.space24,
                    top = spacing.space32,
                    end = spacing.space24,
                    bottom = spacing.space24,
                ),
    ) {
        // Tagged on its own node: a test tag here would lose to the pane's tag on the same node.
        Column(
            modifier = Modifier.fillMaxWidth().testTag(FirstStepsTestTags.PANEL),
            verticalArrangement = Arrangement.spacedBy(spacing.space16),
        ) {
            Text(
                text = stringResource(Res.string.top_verbs_title),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Column(Modifier.fillMaxWidth()) {
                verbs.forEach { verb -> TopVerbRow(verb, onVerbClick) }
            }
        }
    }
}

@Composable
private fun TopVerbRow(verb: VerbUi, onVerbClick: (String) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .clickable { onVerbClick(verb.slug) }
                .padding(vertical = PanelRowPadding)
                .testTag(FirstStepsTestTags.panelVerb(verb.slug)),
        ) {
            Text(
                text = verb.infinitive,
                style = VerbeClairTheme.typography.titleVerb,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
