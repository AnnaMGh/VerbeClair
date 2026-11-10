package com.vamvisions.verbeclair.feature.verblist.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.component.BottomSheetHandle
import com.vamvisions.verbeclair.core.designsystem.component.ButtonStyle
import com.vamvisions.verbeclair.core.designsystem.component.RadioRow
import com.vamvisions.verbeclair.core.designsystem.component.VerbeClairButton
import com.vamvisions.verbeclair.core.designsystem.component.VerbeClairFilterChip
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.domain.verb.AuxiliaryFilter
import com.vamvisions.verbeclair.core.domain.verb.GroupBy
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.core.domain.verb.SortOrder
import com.vamvisions.verbeclair.core.domain.verb.VerbGroup
import com.vamvisions.verbeclair.feature.verblist.generated.resources.Res
import com.vamvisions.verbeclair.feature.verblist.generated.resources.sheet_field_auxiliary
import com.vamvisions.verbeclair.feature.verblist.generated.resources.sheet_field_group
import com.vamvisions.verbeclair.feature.verblist.generated.resources.sheet_field_reflexive
import com.vamvisions.verbeclair.feature.verblist.generated.resources.sheet_filter
import com.vamvisions.verbeclair.feature.verblist.generated.resources.sheet_group_by
import com.vamvisions.verbeclair.feature.verblist.generated.resources.sheet_reset
import com.vamvisions.verbeclair.feature.verblist.generated.resources.sheet_sort
import com.vamvisions.verbeclair.feature.verblist.generated.resources.sheet_title
import com.vamvisions.verbeclair.feature.verblist.presentation.PreviewData
import com.vamvisions.verbeclair.feature.verblist.presentation.VerbListAction
import com.vamvisions.verbeclair.feature.verblist.presentation.label
import com.vamvisions.verbeclair.feature.verblist.presentation.reflexiveChipLabel
import org.jetbrains.compose.resources.stringResource

/** Sheets keep their phone design on wide windows, at most 640 dp wide and centred (N-15). */
val SheetMaxWidth = 640.dp

/** The list controls sheet (L-5, Figma L-04 10:519), as a modal bottom sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListControlsSheet(
    choices: ListChoices,
    onAction: (VerbListAction) -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(VerbListAction.OnDismissSheet) },
        modifier = modifier,
        sheetState = sheetState,
        sheetMaxWidth = SheetMaxWidth,
        shape = RoundedCornerShape(topStart = VerbeClairTheme.radius.xl, topEnd = VerbeClairTheme.radius.xl),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetHandle(Modifier.fillMaxWidth()) },
    ) {
        ListControlsContent(choices = choices, onAction = onAction)
    }
}

/** The sheet content below the handle; every change applies to the list at once (L-5). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListControlsContent(
    choices: ListChoices,
    onAction: (VerbListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = VerbeClairTheme.spacing
    val filters = choices.filters
    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = spacing.space32)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = spacing.space24, end = spacing.space12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.sheet_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            VerbeClairButton(
                text = stringResource(Res.string.sheet_reset),
                onClick = { onAction(VerbListAction.OnReset) },
                style = ButtonStyle.Text,
            )
        }

        Overline(stringResource(Res.string.sheet_filter))
        ChipField(stringResource(Res.string.sheet_field_group)) {
            VerbGroup.entries.forEach { group ->
                VerbeClairFilterChip(
                    label = stringResource(group.label()),
                    selected = group in filters.groups,
                    onClick = { onAction(VerbListAction.OnToggleGroup(group)) },
                )
            }
        }
        ChipField(stringResource(Res.string.sheet_field_auxiliary)) {
            AuxiliaryFilter.entries.forEach { auxiliary ->
                VerbeClairFilterChip(
                    label = stringResource(auxiliary.label()),
                    selected = auxiliary in filters.auxiliaries,
                    onClick = { onAction(VerbListAction.OnToggleAuxiliary(auxiliary)) },
                )
            }
        }
        ChipField(stringResource(Res.string.sheet_field_reflexive)) {
            listOf(true, false).forEach { reflexive ->
                VerbeClairFilterChip(
                    label = stringResource(reflexiveChipLabel(reflexive)),
                    selected = reflexive in filters.reflexive,
                    onClick = { onAction(VerbListAction.OnToggleReflexive(reflexive)) },
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = spacing.space24, vertical = spacing.space8),
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        Overline(stringResource(Res.string.sheet_group_by))
        // The chips' 48 dp touch targets give the 8 dp around each 32 dp chip (Figma 10:568).
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.space24),
            horizontalArrangement = Arrangement.spacedBy(spacing.space8),
        ) {
            GroupBy.entries.forEach { groupBy ->
                VerbeClairFilterChip(
                    label = stringResource(groupBy.label()),
                    selected = choices.groupBy == groupBy,
                    onClick = { onAction(VerbListAction.OnGroupBySelect(groupBy)) },
                )
            }
        }

        Overline(stringResource(Res.string.sheet_sort))
        Column(Modifier.fillMaxWidth().padding(start = spacing.space20, end = spacing.space24)) {
            SortOrder.entries.forEach { sort ->
                RadioRow(
                    label = stringResource(sort.label()),
                    selected = choices.sort == sort,
                    onClick = { onAction(VerbListAction.OnSortSelect(sort)) },
                )
            }
        }
    }
}

/** Part title in Label/Overline `primary`, uppercase (Figma 10:535). */
@Composable
private fun Overline(text: String) {
    val spacing = VerbeClairTheme.spacing
    Text(
        text = text.uppercase(),
        modifier = Modifier.padding(start = spacing.space24, end = spacing.space24, top = spacing.space8),
        style = VerbeClairTheme.typography.labelOverline,
        color = MaterialTheme.colorScheme.primary,
    )
}

/**
 * A filter category: its label in Title/Small `on-surface-variant` over wrapping chips (Figma 10:537). The chips'
 * 48 dp touch targets give the 8 dp above and below each 32 dp chip.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipField(label: String, chips: @Composable () -> Unit) {
    val spacing = VerbeClairTheme.spacing
    Column(
        modifier =
            Modifier.fillMaxWidth().padding(
                start = spacing.space24,
                end = spacing.space24,
                top = spacing.space8,
            ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.space8)) { chips() }
    }
}

// Sheet previews, one per Figma state (design D13). The sheet is drawn without its modal window.

@Composable
private fun SheetFrame(darkTheme: Boolean, content: @Composable ColumnScope.() -> Unit) {
    VerbeClairTheme(darkTheme = darkTheme) {
        val radius = VerbeClairTheme.radius.xl
        Column(
            Modifier.fillMaxWidth().background(
                MaterialTheme.colorScheme.surfaceContainerLow,
                RoundedCornerShape(topStart = radius, topEnd = radius),
            ),
        ) {
            BottomSheetHandle(Modifier.fillMaxWidth())
            content()
        }
    }
}

private val SheetPreviewChoices = ListChoices(filters = PreviewData.groupedChoices.filters)

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-405
@Preview(widthDp = 393)
@Composable
private fun L04ListControlsSheetLightPreview() {
    SheetFrame(darkTheme = false) { ListControlsContent(choices = SheetPreviewChoices, onAction = {}) }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=13-985
@Preview(widthDp = 393)
@Composable
private fun L04ListControlsSheetDarkPreview() {
    SheetFrame(darkTheme = true) { ListControlsContent(choices = SheetPreviewChoices, onAction = {}) }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-405
@Preview(widthDp = 393, locale = "fr")
@Composable
private fun L04ListControlsSheetFrenchPreview() {
    SheetFrame(darkTheme = false) { ListControlsContent(choices = SheetPreviewChoices, onAction = {}) }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=10-405
@Preview(widthDp = 393, locale = "ro")
@Composable
private fun L04ListControlsSheetRomanianPreview() {
    SheetFrame(darkTheme = false) { ListControlsContent(choices = SheetPreviewChoices, onAction = {}) }
}
