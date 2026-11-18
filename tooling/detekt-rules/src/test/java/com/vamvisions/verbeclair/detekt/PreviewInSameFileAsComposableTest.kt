package com.vamvisions.verbeclair.detekt

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import dev.detekt.api.Config
import dev.detekt.test.lint
import org.junit.jupiter.api.Test

class PreviewInSameFileAsComposableTest {
    private val rule = PreviewInSameFileAsComposable(Config.empty)

    @Test
    fun `does not report a preview of a composable in the same file`() {
        val findings =
            rule.lint(
                """
                @Composable
                fun Chip() {}

                @Preview
                @Composable
                private fun ChipPreview() {
                    AppTheme { Chip() }
                }
                """.trimIndent(),
            )

        assertThat(findings).isEmpty()
    }

    @Test
    fun `reports a preview that only calls composables of other files`() {
        val findings =
            rule.lint(
                """
                @Preview
                @Composable
                private fun ChipPreview() {
                    AppTheme { Chip() }
                }
                """.trimIndent(),
            )

        assertThat(findings).hasSize(1)
        assertThat(findings.first().message).contains("ChipPreview")
    }

    @Test
    fun `reports a preview that calls only another preview of its file`() {
        val findings =
            rule.lint(
                """
                @Preview
                @Composable
                private fun ChipLightPreview() {}

                @Preview
                @Composable
                private fun ChipDarkPreview() {
                    ChipLightPreview()
                }
                """.trimIndent(),
            )

        assertThat(findings).hasSize(2)
    }

    @Test
    fun `reports a multipreview function the same way`() {
        val findings =
            rule.lint(
                """
                @PreviewLightDark
                @Composable
                private fun ChipPreview() {
                    Chip()
                }
                """.trimIndent(),
            )

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `reports a preview that calls only a private frame used by previews`() {
        val findings =
            rule.lint(
                """
                @Composable
                private fun Frame(content: @Composable () -> Unit) {
                    AppTheme { content() }
                }

                @Preview
                @Composable
                private fun ChipPreview() {
                    Frame { Chip() }
                }
                """.trimIndent(),
            )

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `does not report a preview of a private composable that production code uses`() {
        val findings =
            rule.lint(
                """
                @Composable
                fun Screen() {
                    Row()
                }

                @Composable
                private fun Row() {}

                @Preview
                @Composable
                private fun RowPreview() {
                    Row()
                }
                """.trimIndent(),
            )

        assertThat(findings).isEmpty()
    }

    @Test
    fun `reports a preview that calls only a plain function of its file`() {
        val findings =
            rule.lint(
                """
                fun Chip() {}

                @Preview
                @Composable
                private fun ChipPreview() {
                    Chip()
                }
                """.trimIndent(),
            )

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `does not report a composable that is no preview`() {
        val findings =
            rule.lint(
                """
                @Composable
                fun Chip() {
                    Text("a")
                }
                """.trimIndent(),
            )

        assertThat(findings).isEmpty()
    }
}
