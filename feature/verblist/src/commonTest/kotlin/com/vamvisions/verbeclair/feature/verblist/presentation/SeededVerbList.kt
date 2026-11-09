package com.vamvisions.verbeclair.feature.verblist.presentation

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.vamvisions.verbeclair.core.data.verb.RoomVerbCatalog
import com.vamvisions.verbeclair.core.database.verb.VerbDatabase
import com.vamvisions.verbeclair.core.database.verb.entity.FormSearchEntity
import com.vamvisions.verbeclair.core.database.verb.entity.VerbEntity
import com.vamvisions.verbeclair.core.domain.verb.normalizeKey
import com.vamvisions.verbeclair.core.domain.verb.sortKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking

/**
 * An in-memory verb database holding être, savoir, suivre (with the forms the "su" search needs)
 * and the 60 filler verbs `verb00`…`verb59`, enough to scroll.
 */
fun seededVerbDatabase(): VerbDatabase =
    inMemoryVerbDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
        .also { runBlocking { seed(it) } }

/** The real ViewModel over [database], with fakes for the user data. */
fun verbListViewModel(database: VerbDatabase) =
    VerbListViewModel(
        catalog = RoomVerbCatalog(database),
        favourites = FakeFavouritesRepository(),
        scores = FakeTestScoreRepository(),
        preferences = FakeListPreferences(),
        syncTrigger = FakeDataSyncTrigger(),
    )

private suspend fun seed(database: VerbDatabase) {
    val dao = database.verbDao()
    val fixtures =
        listOf(
            Triple("etre", "être", listOf("suis" to 100, "été" to 500)),
            Triple("savoir", "savoir", listOf("sais" to 100, "su" to 500)),
            Triple("suivre", "suivre", listOf("suis" to 100, "suivi" to 500)),
        ) +
            (0 until 60).map {
                "verb${it.toString().padStart(2, '0')}".let { name ->
                    Triple(name, name, emptyList<Pair<String, Int>>())
                }
            }
    dao.insertVerbs(
        fixtures.map { (slug, infinitive, _) ->
            VerbEntity(
                slug = slug,
                infinitive = infinitive,
                group = "III",
                reflexive = false,
                auxiliary = "AVOIR",
                frequency = 100.0,
                updatedAt = "2026-10-08T09:26:22Z",
                infinitiveKey = normalizeKey(infinitive),
                sortKey = sortKey(infinitive),
            )
        },
    )
    dao.insertFormSearch(
        fixtures.flatMap { (slug, _, forms) ->
            forms.map { (form, order) -> FormSearchEntity(normalizeKey(form), slug, form, order) }
        },
    )
}
