package com.vamvisions.verbeclair.core.domain.verb

import kotlin.test.Test
import kotlin.test.assertEquals

class NormalizedKeysTest {

    @Test
    fun accentsAndCase() = assertEquals("etre", normalizeKey("Être"))

    @Test
    fun ligatures() {
        assertEquals("oeuvrer", normalizeKey("œuvrer"))
        assertEquals("ae", normalizeKey("Æ"))
    }

    @Test
    fun typographicApostrophe() = assertEquals("s'appeler", normalizeKey("s’appeler"))

    @Test
    fun everyFrenchAccentedLetterIsFolded() {
        assertEquals("aaaeeeeiioouuuyc", normalizeKey("àâäéèêëîïôöùûüÿç"))
        assertEquals("aaaeeeeiioouuuyc", normalizeKey("ÀÂÄÉÈÊËÎÏÔÖÙÛÜŸÇ"))
    }

    @Test
    fun otherAccentedLettersAreFoldedToo() = assertEquals("aaaiiooouyn", normalizeKey("áãåíìóòõúýñ"))

    @Test
    fun sortKeyDropsReflexivePrefix() {
        assertEquals("appeler", sortKey("s'appeler"))
        assertEquals("appeler", sortKey("s’appeler"))
        assertEquals("souvenir", sortKey("se souvenir"))
        assertEquals("etre", sortKey("être"))
    }

    @Test
    fun sortKeyKeepsVerbsThatOnlyStartWithS() = assertEquals("savoir", sortKey("savoir"))
}
