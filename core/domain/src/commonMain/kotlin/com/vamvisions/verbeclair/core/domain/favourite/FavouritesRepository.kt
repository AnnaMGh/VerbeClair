package com.vamvisions.verbeclair.core.domain.favourite

import kotlinx.coroutines.flow.Flow

/**
 * The user's favourite verbs (A-7, L-13). Favourites of verbs missing from the local verb database
 * are hidden from [observeSlugs] and [observeCount] but stay stored, so they come back with the verb.
 */
interface FavouritesRepository {
    fun observeSlugs(): Flow<Set<String>>

    fun observeCount(): Flow<Int>

    suspend fun add(slug: String)

    suspend fun remove(slug: String)
}
