package com.vamvisions.verbeclair.konsist

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isNotEmpty
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Every string in a module's default `values/strings.xml` must also exist in French and Romanian,
 * so a new key cannot ship with a missing translation (the app supports en, fr and ro).
 */
class StringResourceCompletenessTest {
    @Test
    fun `every default string key exists in French and Romanian`() {
        val defaults = composeResourceDirs().map { File(it, "values/strings.xml") }.filter { it.exists() }
        assertThat(defaults).isNotEmpty()

        val missing =
            defaults.flatMap { file ->
                val keys = keysOf(file)
                LOCALES.flatMap { locale ->
                    val translated = File(file.parentFile.parentFile, "values-$locale/strings.xml")
                    val present = if (translated.exists()) keysOf(translated) else emptySet()
                    (keys - present).map { "${file.relativeTo(repoRoot())}: missing '$it' in $locale" }
                }
            }

        assertThat(missing).isEmpty()
    }

    private fun keysOf(file: File): Set<String> = KEY.findAll(file.readText()).map { it.groupValues[1] }.toSet()

    private fun composeResourceDirs(): List<File> =
        repoRoot()
            .walkTopDown()
            .onEnter { it.name != "build" && it.name != ".git" && it.name != ".gradle" }
            .filter { it.isDirectory && it.name == "composeResources" }
            .toList()

    private fun repoRoot(): File =
        generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").exists() }

    private companion object {
        val LOCALES = listOf("fr", "ro")
        val KEY = Regex("""<string\s+name="([^"]+)"""")
    }
}
