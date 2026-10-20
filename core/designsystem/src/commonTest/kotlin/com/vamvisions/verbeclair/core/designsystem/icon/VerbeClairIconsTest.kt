package com.vamvisions.verbeclair.core.designsystem.icon

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VerbeClairIconsTest {

    // Symbol names in the Figma Components Icons frame (node 4:2).
    private val figmaIcons =
        listOf(
            "search",
            "tune",
            "settings",
            "arrow-back",
            "close",
            "chevron-right",
            "check",
            "info",
            "cloud-off",
            "cloud-download",
            "search-off",
            "book-open",
            "file-x",
            "file-text",
            "star",
            "shield",
            "open-in-new",
            "refresh",
            "alert",
            "history",
            "tag",
            "sort",
            "star-filled",
            "pencil",
            "volume",
            "mic",
            "share",
            "download",
            "mail",
            "target",
            "lightbulb",
            "ad-off",
            "chevron-down",
            "check-circle",
            "x-circle",
            "list-checks",
            "repeat",
            "lock",
            "exam",
        ).map { "icon/$it" }

    @Test
    fun everyFigmaIconHasAnEntry() {
        assertEquals(39, VerbeClairIcons.all.size)
        assertEquals(figmaIcons, VerbeClairIcons.all.keys.toList())
    }

    @Test
    fun everyIconIs24By24() {
        VerbeClairIcons.all.forEach { (name, icon) ->
            assertEquals(24.dp, icon.defaultWidth, name)
            assertEquals(24.dp, icon.defaultHeight, name)
            assertEquals(24f, icon.viewportWidth, name)
            assertEquals(24f, icon.viewportHeight, name)
            assertTrue(icon.root.size > 0, "$name has paths")
        }
    }
}
