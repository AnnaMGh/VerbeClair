package com.vamvisions.verbeclair.core.domain.verb

/**
 * The normalised search key of [text] (A-4): lowercase, `œ`→`oe`, `æ`→`ae`, accents removed and the
 * typographic apostrophe `’` replaced by `'`. Used for stored keys and for the typed search text
 * alike, so they never drift apart. Never shown on screen.
 */
fun normalizeKey(text: String): String {
    val lower = text.lowercase()
    return buildString(lower.length) {
        for (char in lower) {
            when (char) {
                'œ' -> append("oe")
                'æ' -> append("ae")
                else -> append(FOLDED[char] ?: char)
            }
        }
    }
}

/** The sort key of an infinitive: [normalizeKey] without a leading `se ` or `s'`, so s'appeler sorts with appeler. */
fun sortKey(infinitive: String): String {
    val key = normalizeKey(infinitive)
    return when {
        key.startsWith("se ") -> key.removePrefix("se ")
        key.startsWith("s'") -> key.removePrefix("s'")
        else -> key
    }
}

private val FOLDED: Map<Char, Char> = buildMap {
    "àâäáãå".forEach { put(it, 'a') }
    "éèêë".forEach { put(it, 'e') }
    "îïíì".forEach { put(it, 'i') }
    "ôöóòõ".forEach { put(it, 'o') }
    "ùûüú".forEach { put(it, 'u') }
    "ÿý".forEach { put(it, 'y') }
    put('ç', 'c')
    put('ñ', 'n')
    put('’', '\'')
}
