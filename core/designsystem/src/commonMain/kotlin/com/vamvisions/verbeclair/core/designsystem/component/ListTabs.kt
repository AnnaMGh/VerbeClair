package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.generated.resources.Res
import com.vamvisions.verbeclair.core.designsystem.generated.resources.list_tab_all
import com.vamvisions.verbeclair.core.designsystem.generated.resources.list_tab_favourites
import org.jetbrains.compose.resources.stringResource

// Figma `List tabs` (49:145) indicator size.
private val IndicatorWidth = 64.dp
private val IndicatorHeight = 3.dp

/** The tabs of the verb list (Notion L-12). */
enum class ListTab { All, Favourites }

/**
 * List tabs (Figma 49:145): two equal-width tabs, All and Favourites ([favouritesCount]), over a 1 dp
 * `outline-variant` line. The selected label is `primary` with a 64 × 3 dp `primary` indicator under it. The row
 * has no background, so the screen's flag shows through (Notion N-14).
 */
@Composable
fun ListTabs(
    selected: ListTab,
    favouritesCount: Int,
    onSelect: (ListTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    PrimaryTabRow(
        selectedTabIndex = selected.ordinal,
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = colors.primary,
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selected.ordinal, matchContentSize = false),
                width = IndicatorWidth,
                height = IndicatorHeight,
                color = colors.primary,
            )
        },
        divider = { HorizontalDivider(thickness = 1.dp, color = colors.outlineVariant) },
    ) {
        ListTab.entries.forEach { tab ->
            Tab(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                text = {
                    Text(
                        text =
                            when (tab) {
                                ListTab.All -> stringResource(Res.string.list_tab_all)
                                ListTab.Favourites -> stringResource(Res.string.list_tab_favourites, favouritesCount)
                            },
                        style = MaterialTheme.typography.titleSmall,
                    )
                },
                selectedContentColor = colors.primary,
                unselectedContentColor = colors.onSurfaceVariant,
            )
        }
    }
}
