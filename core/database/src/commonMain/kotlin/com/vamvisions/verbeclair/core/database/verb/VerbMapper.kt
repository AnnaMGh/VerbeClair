package com.vamvisions.verbeclair.core.database.verb

import com.vamvisions.verbeclair.core.database.dataset.FormRecord
import com.vamvisions.verbeclair.core.database.dataset.MoodRecord
import com.vamvisions.verbeclair.core.database.dataset.SynonymRecord
import com.vamvisions.verbeclair.core.database.dataset.TenseRecord
import com.vamvisions.verbeclair.core.database.dataset.VerbDetailsRecord
import com.vamvisions.verbeclair.core.domain.verb.normalizeKey
import com.vamvisions.verbeclair.core.domain.verb.sortKey

/** Every row of one verb. */
data class VerbEntities(
    val verb: VerbEntity,
    val forms: List<FormEntity>,
    val translation: TranslationEntity,
    val synonyms: List<SynonymEntity>,
    val formSearch: List<FormSearchEntity>,
)

/**
 * The simple tenses whose verb word is searched (L-3, design D2): simple tenses, Infinitif présent
 * and the participles. Compound tenses, Gérondif and Constructions verbales are not.
 */
private val SIMPLE_TENSES: Set<Pair<String, String>> = setOf(
    "INFINITIF" to "PRESENT",
    "INDICATIF" to "PRESENT",
    "INDICATIF" to "IMPARFAIT",
    "INDICATIF" to "PASSE_SIMPLE",
    "INDICATIF" to "FUTUR_SIMPLE",
    "CONDITIONNEL" to "PRESENT",
    "SUBJONCTIF" to "PRESENT",
    "SUBJONCTIF" to "IMPARFAIT",
    "IMPERATIF" to "PRESENT",
    "PARTICIPE" to "PRESENT",
    "PARTICIPE" to "PASSE",
)

/** The dataset/API → entity mapping, shared by the bundled-database build and (later) sync. */
fun VerbDetailsRecord.toEntities(): VerbEntities {
    val forms = mood.flatMapIndexed { moodPos, mood ->
        mood.tense.flatMapIndexed { tensePos, tense ->
            tense.form.mapIndexed { formPos, form -> form.toEntity(slug, mood, moodPos, tense, tensePos, formPos) }
        }
    }
    val formSearch = forms
        .filter { (it.moodCode to it.tenseCode) in SIMPLE_TENSES }
        .map { FormSearchEntity(normalizeKey(it.conjugation.first()), slug, it.conjugation.first(), it.displayOrder()) }
        .groupBy { it.form }
        .map { (_, sameSpelling) -> sameSpelling.minBy { it.displayOrder } }
    return VerbEntities(
        verb = VerbEntity(
            slug = slug,
            infinitive = name,
            group = group,
            reflexive = reflexive,
            auxiliary = auxiliary,
            frequency = frequency,
            updatedAt = updatedAt,
            infinitiveKey = normalizeKey(name),
            sortKey = sortKey(name),
        ),
        forms = forms,
        translation = TranslationEntity(slug, language, definition, translation),
        synonyms = synonyms.mapIndexed { position, synonym ->
            SynonymEntity(slug, position, synonym.slug, synonym.infinitive)
        },
        formSearch = formSearch,
    )
}

/** Rebuilds the *Get verb details* record from the stored rows (A-3). */
fun VerbEntities.toRecord(): VerbDetailsRecord = VerbDetailsRecord(
    slug = verb.slug,
    name = verb.infinitive,
    group = verb.group,
    reflexive = verb.reflexive,
    auxiliary = verb.auxiliary,
    frequency = verb.frequency,
    updatedAt = verb.updatedAt,
    language = translation.language,
    definition = translation.definition,
    translation = translation.translation,
    synonyms = synonyms.sortedBy { it.position }.map { SynonymRecord(it.synonymSlug, it.synonymInfinitive) },
    mood = forms
        .sortedWith(compareBy({ it.moodPos }, { it.tensePos }, { it.formPos }))
        .groupBy { it.moodPos }
        .map { (_, moodForms) ->
            MoodRecord(
                code = moodForms.first().moodCode,
                name = moodForms.first().moodName,
                tense = moodForms.groupBy { it.tensePos }.map { (_, tenseForms) ->
                    TenseRecord(
                        code = tenseForms.first().tenseCode,
                        name = tenseForms.first().tenseName,
                        form = tenseForms.map { it.toRecord() },
                    )
                },
            )
        },
)

private fun FormRecord.toEntity(
    slug: String,
    mood: MoodRecord,
    moodPos: Int,
    tense: TenseRecord,
    tensePos: Int,
    formPos: Int,
) = FormEntity(
    slug = slug,
    moodCode = mood.code,
    moodName = mood.name,
    moodPos = moodPos,
    tenseCode = tense.code,
    tenseName = tense.name,
    tensePos = tensePos,
    person = person,
    formPos = formPos,
    conjugation = conjugation,
    root = root,
    suffix = suffix,
    auxiliary = auxiliary,
    semiAuxiliary = semiAuxiliary,
    infinitivePreposition = infinitivePreposition,
    reflexivePronoun = reflexivePronoun,
    conjunction = conjunction,
    preposition = preposition,
    pronoun = pronoun,
    spokenText = spokenText,
)

private fun FormEntity.toRecord() = FormRecord(
    person = person,
    conjugation = conjugation,
    root = root,
    suffix = suffix,
    auxiliary = auxiliary,
    semiAuxiliary = semiAuxiliary,
    infinitivePreposition = infinitivePreposition,
    reflexivePronoun = reflexivePronoun,
    conjunction = conjunction,
    preposition = preposition,
    pronoun = pronoun,
    spokenText = spokenText,
)

/** D-3 display rank: mood, then tense, then form position. */
private fun FormEntity.displayOrder(): Int = moodPos * 10_000 + tensePos * 100 + formPos
