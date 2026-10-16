package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.vamvisions.verbeclair.core.designsystem.generated.resources.Res
import com.vamvisions.verbeclair.core.designsystem.generated.resources.nav_settings
import com.vamvisions.verbeclair.core.designsystem.generated.resources.nav_tests
import com.vamvisions.verbeclair.core.designsystem.generated.resources.nav_verbs
import com.vamvisions.verbeclair.core.designsystem.icon.VerbeClairIcons
import org.jetbrains.compose.resources.StringResource

/** The three destinations of the bottom bar and the navigation rail (Notion NV-1, NV-4). */
enum class TopLevelDestination { Verbs, Tests, Settings }

internal val TopLevelDestination.icon: ImageVector
    get() = when (this) {
        TopLevelDestination.Verbs -> VerbeClairIcons.BookOpen
        TopLevelDestination.Tests -> VerbeClairIcons.ListChecks
        TopLevelDestination.Settings -> VerbeClairIcons.Settings
    }

internal val TopLevelDestination.label: StringResource
    get() = when (this) {
        TopLevelDestination.Verbs -> Res.string.nav_verbs
        TopLevelDestination.Tests -> Res.string.nav_tests
        TopLevelDestination.Settings -> Res.string.nav_settings
    }

/** Item label: Title/Small when selected, Label/Medium otherwise, as in Figma 49:128 and 86:9531. */
@Composable
internal fun DestinationLabel(text: String, selected: Boolean) {
    Text(
        text = text,
        style = if (selected) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelMedium,
    )
}
