package com.vamvisions.verbeclair.core.database.verb

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface VerbDao {
    @RawQuery(observedEntities = [VerbEntity::class, FormSearchEntity::class])
    fun pagedList(query: RoomRawQuery): PagingSource<Int, VerbRow>

    @RawQuery(observedEntities = [VerbEntity::class, FormSearchEntity::class])
    suspend fun list(query: RoomRawQuery): List<VerbRow>

    @Query("SELECT NOT EXISTS (SELECT 1 FROM verbs)")
    fun observeIsEmpty(): Flow<Boolean>

    @Query("SELECT slug FROM verbs WHERE slug IN (:slugs)")
    fun observeExistingSlugs(slugs: Set<String>): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM verbs")
    suspend fun count(): Int

    @Query("SELECT * FROM verbs WHERE slug = :slug")
    suspend fun verb(slug: String): VerbEntity?

    @Query("SELECT * FROM forms WHERE slug = :slug ORDER BY mood_pos, tense_pos, form_pos")
    suspend fun forms(slug: String): List<FormEntity>

    @Query("SELECT * FROM translations WHERE slug = :slug")
    suspend fun translation(slug: String): TranslationEntity?

    @Query("SELECT * FROM synonyms WHERE slug = :slug ORDER BY position")
    suspend fun synonyms(slug: String): List<SynonymEntity>

    @Query("SELECT * FROM form_search WHERE slug = :slug ORDER BY display_order")
    suspend fun formSearch(slug: String): List<FormSearchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerbs(verbs: List<VerbEntity>)

    @Insert
    suspend fun insertForms(forms: List<FormEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(translations: List<TranslationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSynonyms(synonyms: List<SynonymEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormSearch(keys: List<FormSearchEntity>)

    @Query("DELETE FROM verbs WHERE slug IN (:slugs)")
    suspend fun deleteVerbs(slugs: List<String>)

    /** Replaces every row of [verbs] (delete cascades to forms, keys, translation and synonyms). */
    @Transaction
    suspend fun replace(verbs: List<VerbEntities>) {
        deleteVerbs(verbs.map { it.verb.slug })
        insertVerbs(verbs.map { it.verb })
        insertForms(verbs.flatMap { it.forms })
        insertTranslations(verbs.map { it.translation })
        insertSynonyms(verbs.flatMap { it.synonyms })
        insertFormSearch(verbs.flatMap { it.formSearch })
    }
}

@Dao
interface MetadataDao {
    @Query("SELECT value FROM metadata WHERE `key` = :key")
    suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entries: List<MetadataEntity>)
}
