package com.vamvisions.verbeclair.core.designsystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import kotlin.test.Test
import kotlin.test.assertEquals

/** Figma text styles (design.md D3): weight, size / line height / letter spacing in sp. */
class TypeTest {

    private fun assertStyle(name: String, style: TextStyle, weight: FontWeight, size: Float, line: Float, tracking: Float) {
        assertEquals(weight, style.fontWeight, "$name weight")
        assertEquals(size, style.fontSize.value, "$name size")
        assertEquals(line, style.lineHeight.value, "$name line height")
        assertEquals(tracking, style.letterSpacing.value, "$name letter spacing")
        assertEquals(true, style.fontSize.isSp && style.lineHeight.isSp && style.letterSpacing.isSp, "$name uses sp")
    }

    @Test
    fun frauncesStyles() {
        assertStyle("Display/Verb Large", TypeTokens.DisplayVerbLarge, FontWeight.SemiBold, 34f, 40f, -0.5f)
        assertStyle("Headline/Small", TypeTokens.HeadlineSmall, FontWeight.SemiBold, 24f, 32f, 0f)
        assertStyle("Title/Verb", TypeTokens.TitleVerb, FontWeight.SemiBold, 20f, 26f, 0f)
    }

    @Test
    fun dmSansStyles() {
        assertStyle("Title/Large", TypeTokens.TitleLarge, FontWeight.SemiBold, 22f, 28f, 0f)
        assertStyle("Title/Medium", TypeTokens.TitleMedium, FontWeight.SemiBold, 16f, 24f, 0.1f)
        assertStyle("Title/Small", TypeTokens.TitleSmall, FontWeight.SemiBold, 14f, 20f, 0.1f)
        assertStyle("Label/Overline", TypeTokens.LabelOverline, FontWeight.Bold, 12f, 16f, 1.2f)
        assertStyle("Label/Large", TypeTokens.LabelLarge, FontWeight.Medium, 14f, 20f, 0.1f)
        assertStyle("Label/Medium", TypeTokens.LabelMedium, FontWeight.Medium, 12f, 16f, 0.4f)
        assertStyle("Body/Large", TypeTokens.BodyLarge, FontWeight.Normal, 16f, 24f, 0.2f)
        assertStyle("Body/Medium", TypeTokens.BodyMedium, FontWeight.Normal, 14f, 20f, 0.2f)
        assertStyle("Body/Small", TypeTokens.BodySmall, FontWeight.Normal, 12f, 16f, 0.3f)
        assertStyle("Conjugation/Form", TypeTokens.ConjugationForm, FontWeight.Normal, 16f, 24f, 0f)
        assertStyle("Conjugation/Ending", TypeTokens.ConjugationEnding, FontWeight.Bold, 16f, 24f, 0f)
    }

    @Test
    fun fontAxesMatchFigma() {
        assertEquals(1f, TypeTokens.FRAUNCES_WONK)
        assertEquals(0f, TypeTokens.FRAUNCES_SOFT)
        assertEquals(14f, TypeTokens.DM_SANS_OPTICAL_SIZE)
    }
}
