package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.compose.runtime.Composable
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.vamvisions.verbeclair.core.domain.verb.Auxiliary
import com.vamvisions.verbeclair.core.domain.verb.AuxiliaryFilter
import com.vamvisions.verbeclair.core.domain.verb.GroupBy
import com.vamvisions.verbeclair.core.domain.verb.ListChoices
import com.vamvisions.verbeclair.core.domain.verb.ListFilters
import com.vamvisions.verbeclair.core.domain.verb.SortOrder
import com.vamvisions.verbeclair.core.domain.verb.VerbGroup
import com.vamvisions.verbeclair.core.domain.verb.VerbListItem
import kotlinx.coroutines.flow.flowOf

/** Sample rows for the previews, taken from the Figma frames they are compared with. */
internal object PreviewData {

    /** L-03 choices: être and non-reflexive, grouped by group, most frequent first. */
    val groupedChoices =
        ListChoices(
            filters = ListFilters(auxiliaries = setOf(AuxiliaryFilter.ETRE), reflexive = setOf(false)),
            groupBy = GroupBy.GROUP,
            sort = SortOrder.MOST_FREQUENT,
        )

    /** L-01 Default (10:65): A → Z, two favourites, two scores. */
    val default =
        rows(
            verb("abaisser", VerbGroup.I, Auxiliary.AVOIR, 5.0),
            verb("abandonner", VerbGroup.I, Auxiliary.AVOIR, 50.0, score = 80, favourite = true),
            verb("abattre", VerbGroup.III, Auxiliary.AVOIR, 20.0, score = 45),
            verb("abdiquer", VerbGroup.I, Auxiliary.AVOIR, 0.5),
            verb("abîmer", VerbGroup.I, Auxiliary.AVOIR, 5.0, slug = "abimer"),
            verb("abolir", VerbGroup.II, Auxiliary.AVOIR, 3.0),
            verb("abonder", VerbGroup.I, Auxiliary.AVOIR, 2.0),
            verb("aborder", VerbGroup.I, Auxiliary.AVOIR, 30.0),
            verb("aboutir", VerbGroup.II, Auxiliary.AVOIR, 15.0, favourite = true),
            verb("aboyer", VerbGroup.I, Auxiliary.AVOIR, 2.0),
            verb("abréger", VerbGroup.I, Auxiliary.AVOIR, 1.5, slug = "abreger"),
            verb("abriter", VerbGroup.I, Auxiliary.AVOIR, 4.0),
        )

    /** L-02 Search "su" (10:198): matched forms after être and savoir. */
    val searchSu =
        rows(
            verb(
                "être",
                VerbGroup.III,
                Auxiliary.AVOIR,
                5_000.0,
                slug = "etre",
                matched = "suis",
                score = 80,
                favourite = true,
            ),
            verb("savoir", VerbGroup.III, Auxiliary.AVOIR, 700.0, matched = "su"),
            verb("suivre", VerbGroup.III, Auxiliary.AVOIR, 150.0),
            verb("suffire", VerbGroup.III, Auxiliary.AVOIR, 40.0),
            verb("suggérer", VerbGroup.I, Auxiliary.AVOIR, 20.0, slug = "suggerer"),
            verb("supposer", VerbGroup.I, Auxiliary.AVOIR, 30.0),
            verb("surprendre", VerbGroup.III, Auxiliary.AVOIR, 25.0),
            verb("succéder", VerbGroup.I, Auxiliary.AVOIR, 10.0, slug = "succeder"),
            verb("sucer", VerbGroup.I, Auxiliary.AVOIR, 3.0),
            verb("suer", VerbGroup.I, Auxiliary.AVOIR, 0.8),
        )

    /** L-03 Grouped by group, most frequent, être + non-reflexive (10:338). */
    val grouped =
        rows(
            verb("passer", VerbGroup.I, Auxiliary.BOTH, 900.0),
            verb("arriver", VerbGroup.I, Auxiliary.ETRE, 600.0),
            verb("rester", VerbGroup.I, Auxiliary.ETRE, 90.0),
            verb("tomber", VerbGroup.I, Auxiliary.ETRE, 80.0),
            verb("entrer", VerbGroup.I, Auxiliary.ETRE, 70.0),
            verb("monter", VerbGroup.I, Auxiliary.BOTH, 60.0),
            verb("aller", VerbGroup.III, Auxiliary.ETRE, 1_300.0, score = 92, favourite = true),
            verb("venir", VerbGroup.III, Auxiliary.ETRE, 1_000.0),
            verb("partir", VerbGroup.III, Auxiliary.ETRE, 90.0),
            verb("sortir", VerbGroup.III, Auxiliary.BOTH, 80.0),
            groupBy = GroupBy.GROUP,
        )

    /** L-08 Favourites tab (55:3960): three favourites. */
    val favourites =
        rows(
            verb("abandonner", VerbGroup.I, Auxiliary.AVOIR, 50.0, score = 80, favourite = true),
            verb("aller", VerbGroup.III, Auxiliary.ETRE, 1_300.0, score = 92, favourite = true),
            verb("être", VerbGroup.III, Auxiliary.AVOIR, 5_000.0, slug = "etre", score = 80, favourite = true),
        )

    /** TP-01 / TL-01 list pane (88:815): terminer is selected. */
    val tablet =
        rows(
            verb("tenir", VerbGroup.III, Auxiliary.AVOIR, 600.0),
            verb("terminer", VerbGroup.I, Auxiliary.AVOIR, 60.0, score = 80),
            verb("tester", VerbGroup.I, Auxiliary.AVOIR, 3.0),
            verb("tirer", VerbGroup.I, Auxiliary.AVOIR, 150.0),
            verb("tomber", VerbGroup.I, Auxiliary.ETRE, 300.0, score = 45, favourite = true),
            verb("toucher", VerbGroup.I, Auxiliary.AVOIR, 120.0),
            verb("tourner", VerbGroup.I, Auxiliary.AVOIR, 200.0),
            verb("tracer", VerbGroup.I, Auxiliary.AVOIR, 15.0),
            verb("traduire", VerbGroup.III, Auxiliary.AVOIR, 30.0),
            verb("travailler", VerbGroup.I, Auxiliary.AVOIR, 250.0, score = 100, favourite = true),
            verb("traverser", VerbGroup.I, Auxiliary.AVOIR, 40.0),
            verb("trouver", VerbGroup.I, Auxiliary.AVOIR, 900.0),
        )

    /** TP-07 / TL-07 top 10 panel (195:18534): the 10 most frequent non-reflexive verbs. */
    val topVerbs: List<VerbUi> =
        listOf(
            "être" to "etre",
            "avoir" to "avoir",
            "aller" to "aller",
            "faire" to "faire",
            "dire" to "dire",
            "pouvoir" to "pouvoir",
            "vouloir" to "vouloir",
            "savoir" to "savoir",
            "voir" to "voir",
            "devoir" to "devoir",
        ).map { (infinitive, slug) -> verb(infinitive, VerbGroup.III, Auxiliary.AVOIR, 1.0, slug = slug).first.toUi() }

    private fun verb(
        infinitive: String,
        group: VerbGroup,
        auxiliary: Auxiliary,
        frequency: Double,
        slug: String = infinitive,
        matched: String? = null,
        score: Int? = null,
        favourite: Boolean = false,
    ) = Triple(VerbListItem(slug, infinitive, group, false, auxiliary, frequency, matched), score, favourite)

    private fun rows(
        vararg verbs: Triple<VerbListItem, Int?, Boolean>,
        groupBy: GroupBy = GroupBy.NONE,
    ): List<VerbListRowUi> =
        buildList {
            var section: VerbSection? = null
            verbs.forEach { (item, score, favourite) ->
                val itemSection = item.section(groupBy)
                if (itemSection != null && itemSection != section) add(VerbListRowUi.Header(itemSection))
                section = itemSection
                add(VerbListRowUi.Verb(item.toUi(score, favourite), itemSection))
            }
        }
}

private val Complete = LoadState.NotLoading(endOfPaginationReached = true)

/** Rows as a fully loaded list, shown at once in previews. */
@Composable
internal fun previewItems(rows: List<VerbListRowUi>): LazyPagingItems<VerbListRowUi> =
    flowOf(
        PagingData.from(rows, LoadStates(LoadState.NotLoading(false), Complete, Complete)),
    ).collectAsLazyPagingItems()

/** A first page still loading (Figma L-07). */
@Composable
internal fun previewLoadingItems(): LazyPagingItems<VerbListRowUi> =
    flowOf(
        PagingData.empty<VerbListRowUi>(LoadStates(LoadState.Loading, Complete, Complete)),
    ).collectAsLazyPagingItems()
