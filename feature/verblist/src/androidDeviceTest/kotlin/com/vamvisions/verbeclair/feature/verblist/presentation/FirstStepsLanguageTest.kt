package com.vamvisions.verbeclair.feature.verblist.presentation

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.dp
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import java.util.Locale
import kotlin.test.Test

/**
 * The first steps card and the tablet panel in French (formal) and Romanian (informal): the common tests cannot
 * change the language, so the device language is set here (OB-1, proposal table).
 */
@OptIn(ExperimentalTestApi::class)
class FirstStepsLanguageTest {
    private val original = Locale.getDefault()

    /** Runs [assertions] on the screen in [language], and puts the device language back afterwards. */
    private fun runIn(
        language: String,
        layout: WindowLayout,
        assertions: AndroidComposeUiTest<ComponentActivity>.() -> Unit,
    ) {
        try {
            runAndroidComposeUiTest<ComponentActivity> {
                showIn(language, layout)
                assertions()
            }
        } finally {
            Locale.setDefault(original)
        }
    }

    private fun AndroidComposeUiTest<ComponentActivity>.showIn(language: String, layout: WindowLayout) {
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)
        val configuration = Configuration(activity!!.resources.configuration).apply { setLocale(locale) }
        val state =
            VerbListState(favouritesCount = 12, firstStepsClosed = false, hasAnyAttempt = false, topVerbs = PreviewData.topVerbs)
        setContent {
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                VerbeClairTheme {
                    Box(Modifier.requiredSize(754.dp, 1150.dp)) {
                        VerbListScreen(
                            state = state,
                            verbs = previewItems(PreviewData.tablet),
                            layout = layout,
                            onAction = {},
                            onVerbClick = {},
                            onTestClick = {},
                        )
                    }
                }
            }
        }
    }

    @Test
    fun frenchCard() =
        runIn("fr", WindowLayout.Compact) {
            onNodeWithText("Vous débutez avec les verbes français ?").assertExists()
            onNodeWithText("Commencez par les plus courants.").assertExists()
            onNodeWithText("Tester être").assertExists()
            onNodeWithText("Les plus courants d’abord").assertExists()
            onNodeWithContentDescription("Fermer le conseil").assertExists()
        }

    @Test
    fun romanianCard() =
        runIn("ro", WindowLayout.Compact) {
            onNodeWithText("Abia ai început să înveți verbele în franceză?").assertExists()
            onNodeWithText("Începe cu cele mai frecvente.").assertExists()
            onNodeWithText("Testează être").assertExists()
            onNodeWithText("Cele mai frecvente primele").assertExists()
            onNodeWithContentDescription("Închide sfatul").assertExists()
        }

    @Test
    fun frenchPanelTitle() =
        runIn("fr", WindowLayout.ListDetail) {
            onNodeWithText("L’un de ces verbes vous attire-t-il ?").assertExists()
        }

    @Test
    fun romanianPanelTitle() =
        runIn("ro", WindowLayout.ListDetail) {
            onNodeWithText("Îți atrage atenția vreunul dintre aceste verbe?").assertExists()
        }
}
