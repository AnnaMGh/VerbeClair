package com.vamvisions.verbeclair.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme

/** Preview frame for the components: the VerbeClair theme on the screen background colour. */
@Composable
internal fun ComponentPreview(darkTheme: Boolean, content: @Composable () -> Unit) {
    VerbeClairTheme(darkTheme = darkTheme) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) { content() }
    }
}
