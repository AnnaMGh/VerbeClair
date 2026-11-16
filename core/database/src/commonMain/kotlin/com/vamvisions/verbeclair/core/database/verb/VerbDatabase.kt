package com.vamvisions.verbeclair.core.database.verb

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.vamvisions.verbeclair.core.database.verb.entity.FormEntity
import com.vamvisions.verbeclair.core.database.verb.entity.FormSearchEntity
import com.vamvisions.verbeclair.core.database.verb.entity.MetadataEntity
import com.vamvisions.verbeclair.core.database.verb.entity.SynonymEntity
import com.vamvisions.verbeclair.core.database.verb.entity.TranslationEntity
import com.vamvisions.verbeclair.core.database.verb.entity.VerbEntity

/**
 * The replaceable verb catalogue (A-1, A-3). It never migrates: a schema change raises
 * [VERB_SCHEMA_VERSION] and the bundled database replaces the local one.
 */
@Database(
    entities = [
        VerbEntity::class,
        FormEntity::class,
        TranslationEntity::class,
        SynonymEntity::class,
        FormSearchEntity::class,
        MetadataEntity::class,
    ],
    version = VERB_SCHEMA_VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
@ConstructedBy(VerbDatabaseConstructor::class)
abstract class VerbDatabase : RoomDatabase() {
    abstract fun verbDao(): VerbDao

    abstract fun metadataDao(): MetadataDao
}

@Suppress("KotlinNoActualForExpect")
expect object VerbDatabaseConstructor : RoomDatabaseConstructor<VerbDatabase> {
    override fun initialize(): VerbDatabase
}

/** Room schema version of [VerbDatabase]; part of the bundled metadata (A-1). */
const val VERB_SCHEMA_VERSION = 1

/** Raised whenever the dataset → entity mapping changes, so installed copies are rebuilt (A-1). */
const val BUNDLE_BUILD = 1
