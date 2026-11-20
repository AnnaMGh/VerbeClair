package com.vamvisions.verbeclair.feature.verblist.presentation

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import assertk.assertThat
import assertk.assertions.containsExactly
import com.vamvisions.verbeclair.core.designsystem.theme.VerbeClairTheme
import com.vamvisions.verbeclair.core.presentation.window.WindowLayout
import java.util.Locale
import kotlin.test.Test

/**
 * The language and the font size change while the first steps card is visible (OB-1). The card must show the new
 * text and its close button must still work. Replaces a test that copied the strings from `strings.xml`.
 */
@OptIn(ExperimentalTestApi::class)
class FirstStepsConfigurationChangeTest {
    private val original = Locale.getDefault()

    @Test
    fun changingLanguageAndFontSizeKeepsTheCardAndItsCloseButtonWorking() =
        try {
            runAndroidComposeUiTest<ComponentActivity> {
                var language by mutableStateOf("en")
                var fontScale by mutableFloatStateOf(1f)
                var state by mutableStateOf(VerbListState(favouritesCount = 12, firstStepsClosed = false, hasAnyAttempt = false))
                val actions = mutableListOf<VerbListAction>()
                val baseConfiguration = Configuration(activity!!.resources.configuration)

                setContent {
                    val locale = Locale.forLanguageTag(language)
                    Locale.setDefault(locale)
                    val configuration = Configuration(baseConfiguration).apply { setLocale(locale) }
                    CompositionLocalProvider(
                        LocalConfiguration provides configuration,
                        LocalDensity provides Density(LocalDensity.current.density, fontScale),
                    ) {
                        VerbeClairTheme {
                            Box(Modifier.requiredSize(412.dp, 900.dp)) {
                                VerbListScreen(
                                    state = state,
                                    verbs = previewItems(PreviewData.tablet),
                                    layout = WindowLayout.Compact,
                                    onAction = {
                                        actions += it
                                        if (it == VerbListAction.OnFirstStepsClose) state = state.copy(firstStepsClosed = true)
                                    },
                                    onVerbClick = {},
                                    onTestClick = {},
                                )
                            }
                        }
                    }
                }
                onNodeWithText("New to French verbs?").assertExists()

                language = "fr"
                fontScale = 1.5f
                waitForIdle()

                onNodeWithText("New to French verbs?").assertDoesNotExist()
                onNodeWithText("Vous débutez avec les verbes français ?").assertExists()
                onNodeWithText("Tester être").assertExists()

                onNodeWithContentDescription("Fermer le conseil").performClick()
                waitForIdle()

                assertThat(actions).containsExactly(VerbListAction.OnFirstStepsClose)
                onNodeWithText("Vous débutez avec les verbes français ?").assertDoesNotExist()
            }
        } finally {
            Locale.setDefault(original)
        }
}
