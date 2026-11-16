package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme

// Figma `Bottom sheet/Handle` (7:154): a 32 × 4 dp bar at 40% opacity.
private val HandleWidth = 32.dp
private val HandleHeight = 4.dp
private const val HANDLE_ALPHA = 0.4f

/**
 * Bottom sheet handle (Figma 7:154): a centred, fully rounded 32 × 4 dp bar in `on-surface-variant` at 40%, with
 * 16 dp above and below. Not M3's `DragHandle`, whose padding is 22 dp.
 */
@Composable
fun BottomSheetHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = VerbeClairTheme.spacing.space16),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            Modifier
                .size(HandleWidth, HandleHeight)
                .background(
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HANDLE_ALPHA),
                    VerbeClairTheme.radius.full,
                ),
        )
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-154
@Preview(widthDp = 393)
@Composable
private fun BottomSheetHandleLightPreview() {
    ComponentPreview(darkTheme = false) {
        BottomSheetHandle()
    }
}

// Figma: https://www.figma.com/design/1TyF6efL7m5d6DSTktnnLy/VerbeClair-%E2%80%94-Design-System---App?node-id=7-154
@Preview(widthDp = 393)
@Composable
private fun BottomSheetHandleDarkPreview() {
    ComponentPreview(darkTheme = true) {
        BottomSheetHandle()
    }
}
