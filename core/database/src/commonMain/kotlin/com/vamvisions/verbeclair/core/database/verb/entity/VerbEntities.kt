package com.vamvisions.verbeclair.core.database.verb.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** One verb (A-3), with its normalised keys (A-4). */
@Entity(
    tableName = "verbs",
    indices = [Index("sort_key"), Index("infinitive_key"), Index("frequency")],
)
data class VerbEntity(
    @PrimaryKey val slug: String,
    val infinitive: String,
    val group: String,
    val reflexive: Boolean,
    val auxiliary: String,
    val frequency: Double,
    @ColumnInfo(name = "updated_at") val updatedAt: String,
    @ColumnInfo(name = "infinitive_key") val infinitiveKey: String,
    @ColumnInfo(name = "sort_key") val sortKey: String,
)

/** One form with all its parts, in display order (mood, tense, form position), so *Get verb details* can be rebuilt. */
@Entity(
    tableName = "forms",
    foreignKeys = [ForeignKey(VerbEntity::class, ["slug"], ["slug"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("slug")],
)
data class FormEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slug: String,
    @ColumnInfo(name = "mood_code") val moodCode: String,
    @ColumnInfo(name = "mood_name") val moodName: String,
    @ColumnInfo(name = "mood_pos") val moodPos: Int,
    @ColumnInfo(name = "tense_code") val tenseCode: String,
    @ColumnInfo(name = "tense_name") val tenseName: String,
    @ColumnInfo(name = "tense_pos") val tensePos: Int,
    val person: String?,
    @ColumnInfo(name = "form_pos") val formPos: Int,
    val conjugation: List<String>,
    val root: String?,
    val suffix: List<String>?,
    val auxiliary: String?,
    @ColumnInfo(name = "semi_auxiliary") val semiAuxiliary: String?,
    @ColumnInfo(name = "infinitive_preposition") val infinitivePreposition: String?,
    @ColumnInfo(name = "reflexive_pronoun") val reflexivePronoun: String?,
    val conjunction: String?,
    val preposition: String?,
    val pronoun: List<String>?,
    @ColumnInfo(name = "spoken_text") val spokenText: String?,
)

@Entity(
    tableName = "translations",
    foreignKeys = [ForeignKey(VerbEntity::class, ["slug"], ["slug"], onDelete = ForeignKey.CASCADE)],
)
data class TranslationEntity(
    @PrimaryKey val slug: String,
    val language: String,
    val definition: String?,
    val translation: String?,
)

@Entity(
    tableName = "synonyms",
    primaryKeys = ["slug", "position"],
    foreignKeys = [ForeignKey(VerbEntity::class, ["slug"], ["slug"], onDelete = ForeignKey.CASCADE)],
)
data class SynonymEntity(
    val slug: String,
    val position: Int,
    @ColumnInfo(name = "synonym_slug") val synonymSlug: String,
    @ColumnInfo(name = "synonym_infinitive") val synonymInfinitive: String,
)

/**
 * Search key of one distinct default spelling (index 0) of a simple form of a verb (L-3). The
 * infinitive itself is searched through `verbs.infinitive_key`. [displayOrder] is the D-3 rank of
 * the first form with that spelling, used to pick the matched form (L-4).
 */
@Entity(
    tableName = "form_search",
    primaryKeys = ["slug", "form"],
    foreignKeys = [ForeignKey(VerbEntity::class, ["slug"], ["slug"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("key", "slug")],
)
data class FormSearchEntity(
    val key: String,
    val slug: String,
    val form: String,
    @ColumnInfo(name = "display_order") val displayOrder: Int,
)

/** Key–value facts about the installed verb database: [MetadataKeys]. */
@Entity(tableName = "metadata")
data class MetadataEntity(
    @PrimaryKey val key: String,
    val value: String,
)

object MetadataKeys {
    const val LAST_UPDATED_AT = "lastUpdatedAt"
    const val SCHEMA_VERSION = "schemaVersion"
    const val BUNDLE_BUILD = "bundleBuild"

    /** The sync checkpoint (SY-2). After a bundled install it is the bundled `lastUpdatedAt` (A-1). */
    const val CHECKPOINT = "checkpoint"
}
