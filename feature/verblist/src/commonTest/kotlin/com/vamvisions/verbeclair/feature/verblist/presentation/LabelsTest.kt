package com.vamvisions.verbeclair.feature.verblist.presentation

import com.vamvisions.verbeclair.core.domain.verb.Auxiliary
import com.vamvisions.verbeclair.core.domain.verb.AuxiliaryFilter
import com.vamvisions.verbeclair.core.domain.verb.GroupBy
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.core.domain.verb.ListFilters
import com.vamvisions.verbeclair.core.domain.verb.SortOrder
import com.vamvisions.verbeclair.core.domain.verb.VerbGroup
import com.vamvisions.verbeclair.core.domain.verb.VerbListItem
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The labels table (Notion section 6) and the active choices row (L-6), in English. */
class LabelsTest {

    @Test
    fun allerMetaLine() = runTest {
        val aller = verb(VerbGroup.III, Auxiliary.ETRE, reflexive = false, frequency = 1_300.0)

        assertEquals("3rd group · être · non-reflexive · very common", meta(aller))
    }

    @Test
    fun bothAuxiliariesLabel() = runTest {
        val passer = verb(VerbGroup.I, Auxiliary.BOTH, reflexive = false, frequency = 400.0)

        assertEquals("1st group · avoir or être · non-reflexive · very common", meta(passer))
    }

    @Test
    fun reflexiveLabel() = runTest {
        val sAppeler = verb(VerbGroup.I, Auxiliary.ETRE, reflexive = true, frequency = 0.5)

        assertEquals("1st group · être · reflexive · rare", meta(sAppeler))
    }

    @Test
    fun frequencyThresholds() = runTest {
        val expected = mapOf(
            100.0 to "very common",
            99.9 to "common",
            10.0 to "common",
            1.0 to "uncommon",
            0.99 to "rare",
            0.0 to "rare",
        )
        expected.forEach { (frequency, label) ->
            assertEquals(label, getString(frequencyLabel(frequency)), "frequency $frequency")
        }
    }

    @Test
    fun activeChoicesInRowOrder() = runTest {
        val choices = ListChoices(
            filters = ListFilters(auxiliaries = setOf(AuxiliaryFilter.ETRE), reflexive = setOf(false)),
            groupBy = GroupBy.GROUP,
            sort = SortOrder.MOST_FREQUENT,
        )

        assertEquals(
            listOf("être", "Non-reflexive", "By group", "Most frequent"),
            choices.activeChoices().map { getString(it.label()) },
        )
    }

    @Test
    fun everyChoiceHasItsOwnLabel() = runTest {
        assertEquals(
            listOf("1st group", "2nd group", "3rd group"),
            VerbGroup.entries.map { getString(it.label()) },
        )
        assertEquals(listOf("avoir", "être"), AuxiliaryFilter.entries.map { getString(it.label()) })
        assertEquals(listOf("None", "Group", "Auxiliary", "Reflexive"), GroupBy.entries.map { getString(it.label()) })
        assertEquals(listOf("A → Z", "Z → A", "Most frequent"), SortOrder.entries.map { getString(it.label()) })
        assertEquals(
            listOf("By group", "By auxiliary", "By reflexive"),
            listOf(GroupBy.GROUP, GroupBy.AUXILIARY, GroupBy.REFLEXIVE)
                .map { getString(ActiveChoice.Grouping(it).label()) },
        )
    }

    @Test
    fun defaultChoicesHaveNoActiveChoice() {
        assertTrue(ListChoices.Default.activeChoices().isEmpty())
    }

    @Test
    fun removingAChoiceRestoresItsDefault() {
        val choices = ListChoices(
            filters = ListFilters(groups = setOf(VerbGroup.I, VerbGroup.II)),
            groupBy = GroupBy.AUXILIARY,
            sort = SortOrder.Z_TO_A,
        )

        assertEquals(setOf(VerbGroup.II), choices.without(ActiveChoice.Group(VerbGroup.I)).filters.groups)
        assertEquals(GroupBy.NONE, choices.without(ActiveChoice.Grouping(GroupBy.AUXILIARY)).groupBy)
        assertEquals(SortOrder.A_TO_Z, choices.without(ActiveChoice.Sort(SortOrder.Z_TO_A)).sort)
    }

    @Test
    fun sectionsFollowTheGrouping() = runTest {
        val monter = verb(VerbGroup.I, Auxiliary.BOTH, reflexive = false, frequency = 50.0)

        assertEquals(null, monter.section(GroupBy.NONE))
        assertEquals("1st group", getString(monter.section(GroupBy.GROUP)!!.title))
        assertEquals("avoir or être", getString(monter.section(GroupBy.AUXILIARY)!!.title))
        assertEquals("non-reflexive", getString(monter.section(GroupBy.REFLEXIVE)!!.title))
    }

    private suspend fun meta(item: VerbListItem): String =
        item.toUi().meta.map { getString(it) }.joinToString(META_SEPARATOR)

    private fun verb(group: VerbGroup, auxiliary: Auxiliary, reflexive: Boolean, frequency: Double) =
        VerbListItem("slug", "infinitive", group, reflexive, auxiliary, frequency)

}
