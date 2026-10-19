package com.vamvisions.verbeclair

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme

/** The app root: the shell in the VerbeClair theme, following the system appearance (N-5). */
@Composable
fun App(darkTheme: Boolean = isSystemInDarkTheme()) {
    VerbeClairTheme(darkTheme = darkTheme) {
        AppShell()
    }
}
