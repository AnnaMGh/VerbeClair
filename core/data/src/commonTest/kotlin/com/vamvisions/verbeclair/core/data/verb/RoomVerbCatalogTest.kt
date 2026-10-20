package com.vamvisions.verbeclair.core.data.verb

import androidx.paging.testing.asSnapshot
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vamvisions.verbeclair.core.data.bundle.inMemoryVerbDatabaseBuilder
import com.vamvisions.verbeclair.core.database.verb.FormSearchEntity
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import com.vamvisions.verbeclair.core.database.verb.VerbEntity
import com.vamvisions.verbeclair.core.domain.verb.Auxiliary
import com.vamvisions.verbeclair.core.domain.verb.AuxiliaryFilter
import com.vamvisions.verbeclair.core.domain.verb.GroupBy
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.core.domain.verb.ListFilters
import com.vamvisions.verbeclair.core.domain.verb.ListQuery
import com.vamvisions.verbeclair.core.domain.verb.ListTab
import com.vamvisions.verbeclair.core.domain.verb.SortOrder
import com.vamvisions.verbeclair.core.domain.verb.VerbGroup
import com.vamvisions.verbeclair.core.domain.verb.VerbListItem
import com.vamvisions.verbeclair.core.domain.verb.normalizeKey
import com.vamvisions.verbeclair.core.domain.verb.sortKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The verb-data search, filter, group and sort rules (design D5) on a small fixture catalogue. */
class RoomVerbCatalogTest {
    private val database: VerbDatabase =
        inMemoryVerbDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    private val catalog = RoomVerbCatalog(database)

    @AfterTest
    fun close() = database.close()

    @Test
    fun conjugatedFormsCountAndCarryTheMatchedForm() =
        runTest {
            seed()
            val result = list(search = "su").associate { it.slug to it.matchedForm }

            assertEquals(mapOf("etre" to "suis", "savoir" to "su", "suivre" to null), result)
        }

    @Test
    fun searchIgnoresAccentsAndKeepsTheOriginalSpelling() =
        runTest {
            seed()
            val result = list(search = "ete")

            assertEquals(listOf("etre"), result.map { it.slug })
            assertEquals("été", result.single().matchedForm)
        }

    @Test
    fun searchIgnoresCaseAndTypographicApostrophe() =
        runTest {
            seed()

            assertEquals(listOf("s-appeler"), list(search = "  S’APP ").map { it.slug })
        }

    @Test
    fun semiAuxiliariesAndAgreementVariantsAreNotMatched() =
        runTest {
            seed()

            assertEquals(listOf("aller"), list(search = "va").map { it.slug })
            assertEquals(emptyList(), list(search = "parlée"))
        }

    @Test
    fun searchIsAPrefixMatchOnly() =
        runTest {
            seed()

            assertEquals(emptyList(), list(search = "arl"))
        }

    @Test
    fun blankSearchReturnsEveryVerb() =
        runTest {
            seed()

            assertEquals(VERBS.map { it.slug }.toSet(), list(search = "   ").map { it.slug }.toSet())
        }

    @Test
    fun rowsCarryTheVerbFields() =
        runTest {
            seed()

            val etre = list().single { it.slug == "etre" }

            assertEquals(VerbListItem("etre", "être", VerbGroup.III, false, Auxiliary.AVOIR, 32236.5), etre)
        }

    @Test
    fun etreFilterIncludesVerbsWithBothAuxiliaries() =
        runTest {
            seed()
            val result = list(filters = ListFilters(auxiliaries = setOf(AuxiliaryFilter.ETRE))).map { it.slug }

            assertEquals(listOf("aller", "s-appeler", "passer"), result)
        }

    @Test
    fun valuesOfOneCategoryCombineWithOr() =
        runTest {
            seed()
            val result = list(filters = ListFilters(groups = setOf(VerbGroup.I, VerbGroup.II)))

            assertTrue(result.isNotEmpty())
            assertTrue(result.none { it.group == VerbGroup.III })
            assertTrue(result.any { it.group == VerbGroup.II })
        }

    @Test
    fun categoriesCombineWithAnd() =
        runTest {
            seed()
            val filters = ListFilters(auxiliaries = setOf(AuxiliaryFilter.ETRE), reflexive = setOf(false))

            assertEquals(listOf("aller", "passer"), list(filters = filters).map { it.slug })
        }

    @Test
    fun filtersCombineWithTheSearch() =
        runTest {
            seed()
            val filters = ListFilters(groups = setOf(VerbGroup.III))

            assertEquals(listOf("savoir"), list(search = "sa", filters = filters).map { it.slug })
        }

    @Test
    fun groupedByGroupAndMostFrequentStartsWithTheMostCommonFirstGroupVerb() =
        runTest {
            seed()
            val result = list(groupBy = GroupBy.GROUP, sort = SortOrder.MOST_FREQUENT).map { it.slug }

            assertEquals(
                listOf(
                    "parler",
                    "passer",
                    "appeler",
                    "s-appeler",
                    "finir",
                    "choisir",
                    "etre",
                    "aller",
                    "savoir",
                    "suivre",
                ),
                result,
            )
        }

    @Test
    fun groupedByAuxiliaryPutsBothLast() =
        runTest {
            seed()
            val result = list(groupBy = GroupBy.AUXILIARY).map { it.auxiliary.name }

            assertEquals(result.sortedBy { listOf("AVOIR", "ETRE", "BOTH").indexOf(it) }, result)
        }

    @Test
    fun groupedByReflexivePutsNonReflexiveFirst() =
        runTest {
            seed()

            assertEquals("s-appeler", list(groupBy = GroupBy.REFLEXIVE).last().slug)
        }

    @Test
    fun aToZFollowsTheSortKeySoReflexiveVerbsSortWithoutThePronoun() =
        runTest {
            seed()

            assertEquals(
                listOf(
                    "aller",
                    "appeler",
                    "s-appeler",
                    "choisir",
                    "etre",
                    "finir",
                    "parler",
                    "passer",
                    "savoir",
                    "suivre",
                ),
                list().map { it.slug },
            )
        }

    @Test
    fun zToAStartsWithTheLastSortKey() =
        runTest {
            seed()
            val result = list(sort = SortOrder.Z_TO_A).map { it.slug }

            assertEquals("suivre", result.first())
            assertEquals("aller", result.last())
        }

    @Test
    fun tiesAreBrokenByInfinitiveKeyThenSlug() =
        runTest {
            seed()
            // appeler and s'appeler share the sort key and the frequency.
            val bySort = list(sort = SortOrder.Z_TO_A).map { it.slug }
            val byFrequency = list(sort = SortOrder.MOST_FREQUENT).map { it.slug }

            assertEquals(bySort.indexOf("appeler") + 1, bySort.indexOf("s-appeler"))
            assertEquals(byFrequency.indexOf("appeler") + 1, byFrequency.indexOf("s-appeler"))
        }

    @Test
    fun eachVerbAppearsAtMostOnce() =
        runTest {
            seed()
            // être matches through several forms (suis, sommes); suivre through its infinitive and "suis".
            val result = list(search = "s").map { it.slug }

            assertEquals(result.distinct(), result)
            assertEquals(setOf("etre", "savoir", "suivre", "s-appeler"), result.toSet())
        }

    @Test
    fun favouritesTabListsOnlyFavourites() =
        runTest {
            seed()

            assertEquals(
                listOf(
                    "etre",
                    "parler",
                ),
                list(tab = ListTab.FAVOURITES, favourites = setOf("parler", "etre", "gone")).map {
                    it.slug
                },
            )
            assertEquals(emptyList(), list(tab = ListTab.FAVOURITES, favourites = emptySet()))
        }

    @Test
    fun isEmptyFollowsTheDatabase() =
        runTest {
            assertTrue(catalog.isEmpty().first())
            seed()
            assertFalse(catalog.isEmpty().first())
        }

    @Test
    fun existingSlugsDropsVerbsMissingFromTheDatabase() =
        runTest {
            seed()

            assertEquals(setOf("etre"), catalog.existingSlugs(setOf("etre", "gone")).first())
            assertEquals(emptySet(), catalog.existingSlugs(emptySet()).first())
        }

    private suspend fun list(
        search: String = "",
        filters: ListFilters = ListFilters(),
        groupBy: GroupBy = GroupBy.NONE,
        sort: SortOrder = SortOrder.A_TO_Z,
        tab: ListTab = ListTab.ALL,
        favourites: Set<String> = emptySet(),
    ): List<VerbListItem> =
        catalog
            .pagedList(
                ListQuery(search, ListChoices(filters, groupBy, sort), tab, favourites),
            ).asSnapshot()

    private suspend fun seed() {
        val dao = database.verbDao()
        dao.insertVerbs(VERBS.map { it.entity() })
        dao.insertFormSearch(VERBS.flatMap { it.searchKeys() })
    }

    private class Fixture(
        val slug: String,
        val infinitive: String,
        val group: String,
        val auxiliary: String,
        val frequency: Double,
        val reflexive: Boolean = false,
        /** Simple forms with their display order, as the mapper stores them. */
        val forms: List<Pair<String, Int>> = emptyList(),
    ) {
        fun entity() =
            VerbEntity(
                slug = slug,
                infinitive = infinitive,
                group = group,
                reflexive = reflexive,
                auxiliary = auxiliary,
                frequency = frequency,
                updatedAt = "2026-10-08T09:26:22Z",
                infinitiveKey = normalizeKey(infinitive),
                sortKey = sortKey(infinitive),
            )

        fun searchKeys() = forms.map { (form, order) -> FormSearchEntity(normalizeKey(form), slug, form, order) }
    }

    private companion object {
        // Frequencies from dataset v3; finir, choisir and aller are made up but keep the real order.
        val VERBS =
            listOf(
                Fixture(
                    "etre",
                    "être",
                    "III",
                    "AVOIR",
                    32236.5,
                    forms =
                        listOf(
                            "suis" to 100,
                            "es" to 101,
                            "est" to 102,
                            "sommes" to 103,
                            "étais" to 110,
                            "été" to 500,
                        ),
                ),
                Fixture(
                    "aller",
                    "aller",
                    "III",
                    "ETRE",
                    5000.0,
                    forms =
                        listOf(
                            "vais" to 100,
                            "vas" to 101,
                            "va" to 102,
                            "allé" to 500,
                        ),
                ),
                Fixture(
                    "savoir",
                    "savoir",
                    "III",
                    "AVOIR",
                    4516.71,
                    forms = listOf("sais" to 100, "sus" to 400, "su" to 500),
                ),
                Fixture(
                    "suivre",
                    "suivre",
                    "III",
                    "AVOIR",
                    2090.54,
                    forms =
                        listOf(
                            "suis" to 100,
                            "suit" to 102,
                            "suivi" to 500,
                        ),
                ),
                Fixture("parler", "parler", "I", "AVOIR", 1970.55, forms = listOf("parle" to 100, "parlé" to 500)),
                Fixture("passer", "passer", "I", "BOTH", 1495.49, forms = listOf("passe" to 100, "passé" to 500)),
                Fixture("appeler", "appeler", "I", "AVOIR", 1165.48, forms = listOf("appelle" to 100)),
                Fixture(
                    "s-appeler",
                    "s'appeler",
                    "I",
                    "ETRE",
                    1165.48,
                    reflexive = true,
                    forms =
                        listOf("appelle" to 100),
                ),
                Fixture("finir", "finir", "II", "AVOIR", 900.0, forms = listOf("finis" to 100)),
                Fixture("choisir", "choisir", "II", "AVOIR", 800.0, forms = listOf("choisis" to 100)),
            )
    }
}
