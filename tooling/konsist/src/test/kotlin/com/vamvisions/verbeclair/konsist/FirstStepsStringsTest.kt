package com.vamvisions.verbeclair.konsist

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import java.io.File

/**
 * The first steps card and top 10 panel strings of the proposal table (OB-1), read from the verb list resources in
 * English, French (formal) and Romanian (informal).
 */
class FirstStepsStringsTest {
    @Test
    fun `English strings match the proposal table`() {
        assertThat(strings("values")).isEqualTo(
            mapOf(
                "first_steps_title" to "New to French verbs?",
                "first_steps_body" to "Start with the most common ones.",
                "first_steps_test_etre" to "Test être",
                "first_steps_most_common" to "Most common first",
                "first_steps_close" to "Close tip",
                "top_verbs_title" to "Does any of these catch your eye?",
            ),
        )
    }

    @Test
    fun `French strings match the proposal table`() {
        assertThat(strings("values-fr")).isEqualTo(
            mapOf(
                "first_steps_title" to "Vous débutez avec les verbes français ?",
                "first_steps_body" to "Commencez par les plus courants.",
                "first_steps_test_etre" to "Tester être",
                "first_steps_most_common" to "Les plus courants d’abord",
                "first_steps_close" to "Fermer le conseil",
                "top_verbs_title" to "L’un de ces verbes vous attire-t-il ?",
            ),
        )
    }

    @Test
    fun `Romanian strings match the proposal table`() {
        assertThat(strings("values-ro")).isEqualTo(
            mapOf(
                "first_steps_title" to "Abia ai început să înveți verbele în franceză?",
                "first_steps_body" to "Începe cu cele mai frecvente.",
                "first_steps_test_etre" to "Testează être",
                "first_steps_most_common" to "Cele mai frecvente primele",
                "first_steps_close" to "Închide sfatul",
                "top_verbs_title" to "Îți atrage atenția vreunul dintre aceste verbe?",
            ),
        )
    }

    private fun strings(folder: String): Map<String, String> {
        val file = File(repoRoot(), "feature/verblist/src/commonMain/composeResources/$folder/strings.xml")
        return ENTRY
            .findAll(file.readText())
            .map { it.groupValues[1] to it.groupValues[2] }
            .filter { (key, _) -> key in KEYS }
            .toMap()
    }

    private fun repoRoot(): File =
        generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").exists() }

    private companion object {
        val ENTRY = Regex("""<string name="([^"]+)">([^<]*)</string>""")
        val KEYS =
            setOf(
                "first_steps_title",
                "first_steps_body",
                "first_steps_test_etre",
                "first_steps_most_common",
                "first_steps_close",
                "top_verbs_title",
            )
    }
}
