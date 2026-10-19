package com.vamvisions.verbeclair.core.data.user

import com.vamvisions.verbeclair.core.database.user.FavouriteEntity
import com.vamvisions.verbeclair.core.database.user.UserDatabase
import com.vamvisions.verbeclair.core.domain.favourite.FavouritesRepository
import com.vamvisions.verbeclair.core.domain.verb.VerbCatalog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Favourites from the user database (design D6). The verb database is separate, so favourites of
 * verbs missing from it are filtered out through [VerbCatalog.existingSlugs]; their rows stay stored.
 */
@OptIn(ExperimentalTime::class)
class RoomFavouritesRepository(
    database: UserDatabase,
    private val catalog: VerbCatalog,
    private val clock: Clock = Clock.System,
) : FavouritesRepository {
    private val dao = database.favouriteDao()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeSlugs(): Flow<Set<String>> =
        dao.observeSlugs().flatMapLatest { slugs -> catalog.existingSlugs(slugs.toSet()) }

    override fun observeCount(): Flow<Int> = observeSlugs().map { it.size }

    override suspend fun add(slug: String) {
        dao.insert(FavouriteEntity(slug, addedAt = clock.now().toEpochMilliseconds()))
    }

    override suspend fun remove(slug: String) {
        dao.delete(slug)
    }
}
