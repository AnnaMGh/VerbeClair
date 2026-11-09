package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.generated.resources.Res
import com.vamvisions.verbeclair.core.designsystem.generated.resources.search_clear
import com.vamvisions.verbeclair.core.designsystem.generated.resources.search_placeholder
import com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import org.jetbrains.compose.resources.stringResource

// Figma `Search bar` (5:56) height.
private val SearchBarHeight = 52.dp

/**
 * Search bar (Figma 5:56, Notion L-2, L-3): a fully rounded field on `surface-container-high` with the search icon,
 * the "Search a verb…" placeholder when [query] is empty, and a clear button when it is not.
 *
 * Stateless: every change of the text, including the clear button, is reported through [onQueryChange]. The
 * debounce of L-3 belongs to the caller.
 */
@Composable
fun VerbeClairSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val spacing = VerbeClairTheme.spacing
    val textStyle = MaterialTheme.typography.bodyLarge
    val isEmpty = query.isEmpty()
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        singleLine = true,
        textStyle = textStyle.copy(color = colors.onSurface),
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        decorationBox = { innerTextField ->
            Row(
                modifier =
                    Modifier
                        .background(colors.surfaceContainerHigh, VerbeClairTheme.radius.full)
                        .heightIn(min = SearchBarHeight)
                        // Filled: the 48 dp clear button keeps its icon 12 dp from the end, where Figma puts it
                        // (a 40 dp button inside 4 dp of padding).
                        .padding(start = spacing.space16, end = if (isEmpty) spacing.space16 else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.space12),
            ) {
                Icon(
                    imageVector = VerbeClairIcons.Search,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                )
                Box(Modifier.weight(1f)) {
                    if (isEmpty) {
                        Text(
                            text = stringResource(Res.string.search_placeholder),
                            style = textStyle,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
                if (!isEmpty) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = VerbeClairIcons.Close,
                            contentDescription = stringResource(Res.string.search_clear),
                            tint = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        },
    )
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-45
@Preview(widthDp = 260)
@Composable
private fun SearchBarEmptyLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairSearchBar(query = "", onQueryChange = {}, modifier = Modifier.fillMaxWidth())
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-45
@Preview(widthDp = 260)
@Composable
private fun SearchBarEmptyDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairSearchBar(query = "", onQueryChange = {}, modifier = Modifier.fillMaxWidth())
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-55
@Preview(widthDp = 260)
@Composable
private fun SearchBarFilledLightPreview() {
    ComponentPreview(darkTheme = false) {
        VerbeClairSearchBar(query = "su", onQueryChange = {}, modifier = Modifier.fillMaxWidth())
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=5-55
@Preview(widthDp = 260)
@Composable
private fun SearchBarFilledDarkPreview() {
    ComponentPreview(darkTheme = true) {
        VerbeClairSearchBar(query = "su", onQueryChange = {}, modifier = Modifier.fillMaxWidth())
    }
}
