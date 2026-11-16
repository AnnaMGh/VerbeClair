package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.designsystem.theme.elevation

// Figma `Snackbar` (7:144) sizes.
private val SnackbarMinHeight = 48.dp
private val SnackbarIconSize = 20.dp
private val SnackbarPreviewWidth = 361.dp

/**
 * Snackbar (Figma 7:144): a transient message on `inverse-surface` with a 20 dp [icon] and the [message] in
 * Body/Medium, 48 dp high at least, with the Elevation/Level 2 shadow. Figma also has an optional action ("Retry");
 * no screen needs one yet, so it is not built. The icon is decoration and is hidden from screen readers.
 */
@Composable
fun VerbeClairSnackbar(
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val spacing = VerbeClairTheme.spacing
    val shape = MaterialTheme.shapes.small
    Row(
        modifier =
            modifier
                .elevation(VerbeClairTheme.elevation.level2, shape)
                .clip(shape)
                .background(colors.inverseSurface)
                .heightIn(min = SnackbarMinHeight)
                .padding(
                    PaddingValues(
                        start = spacing.space16,
                        end = spacing.space8,
                        top = spacing.space4,
                        bottom = spacing.space4,
                    ),
                ),
        horizontalArrangement = Arrangement.spacedBy(spacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(SnackbarIconSize),
            tint = colors.inverseOnSurface,
        )
        Text(
            text = message,
            modifier = Modifier.padding(end = spacing.space8),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.inverseOnSurface,
        )
    }
}

// The Figma Snackbar (7:144) is one component with an optional action and no variants, so its only states are the
// themes and the window sizes it is shown in. Figma has no frame per window size, so all use the warning instance 169:226 (Snackbar 7:144 with the alert icon).
private const val PREVIEW_MESSAGE = "Couldn't update favourites. Try again."

// Places the snackbar where VerbListRoot does: bottom centre, 16 dp in from the edges, at most 361 dp wide.
@Composable
private fun SnackbarPreviewFrame(darkTheme: Boolean, snackbar: @Composable (Modifier) -> Unit) {
    VerbeClairTheme(darkTheme = darkTheme) {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomCenter,
        ) {
            snackbar(Modifier.padding(VerbeClairTheme.spacing.space16).widthIn(max = SnackbarPreviewWidth))
        }
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=169-226
@Preview(widthDp = 393, heightDp = 120)
@Composable
private fun VerbeClairSnackbarPhonePortraitLightPreview() {
    SnackbarPreviewFrame(darkTheme = false) { modifier ->
        VerbeClairSnackbar(message = PREVIEW_MESSAGE, icon = VerbeClairIcons.Alert, modifier = modifier)
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=169-226
@Preview(widthDp = 393, heightDp = 120)
@Composable
private fun VerbeClairSnackbarPhonePortraitDarkPreview() {
    SnackbarPreviewFrame(darkTheme = true) { modifier ->
        VerbeClairSnackbar(message = PREVIEW_MESSAGE, icon = VerbeClairIcons.Alert, modifier = modifier)
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=169-226
@Preview(widthDp = 772, heightDp = 393)
@Composable
private fun VerbeClairSnackbarPhoneLandscapeLightPreview() {
    SnackbarPreviewFrame(darkTheme = false) { modifier ->
        VerbeClairSnackbar(message = PREVIEW_MESSAGE, icon = VerbeClairIcons.Alert, modifier = modifier)
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=169-226
@Preview(widthDp = 772, heightDp = 393)
@Composable
private fun VerbeClairSnackbarPhoneLandscapeDarkPreview() {
    SnackbarPreviewFrame(darkTheme = true) { modifier ->
        VerbeClairSnackbar(message = PREVIEW_MESSAGE, icon = VerbeClairIcons.Alert, modifier = modifier)
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=169-226
@Preview(widthDp = 1114, heightDp = 790)
@Composable
private fun VerbeClairSnackbarTabletLightPreview() {
    SnackbarPreviewFrame(darkTheme = false) { modifier ->
        VerbeClairSnackbar(message = PREVIEW_MESSAGE, icon = VerbeClairIcons.Alert, modifier = modifier)
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=169-226
@Preview(widthDp = 1114, heightDp = 790)
@Composable
private fun VerbeClairSnackbarTabletDarkPreview() {
    SnackbarPreviewFrame(darkTheme = true) { modifier ->
        VerbeClairSnackbar(message = PREVIEW_MESSAGE, icon = VerbeClairIcons.Alert, modifier = modifier)
    }
}
