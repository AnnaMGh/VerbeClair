package com.vamvisions.verbeclair.core.database.dataset

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * The dataset file `french-verbs-dataset-v<N>.json.gz` (A-2). Each verb has the shape of the API's
 * *Get verb details* response with `lang=en`, so the same model is used by sync later (SY-1).
 */
@Serializable
data class DatasetFile(
    val datasetVersion: Int,
    val lastUpdatedAt: String,
    val datasetLicense: String,
    val datasetLicenseUrl: String,
    val attribution: String,
    val sources: JsonElement,
    val verbs: List<VerbDetailsRecord>,
)

@Serializable
data class VerbDetailsRecord(
    val slug: String,
    val name: String,
    val group: String,
    val reflexive: Boolean,
    val auxiliary: String,
    val frequency: Double,
    val updatedAt: String,
    val language: String,
    val definition: String?,
    val translation: String?,
    val synonyms: List<SynonymRecord>,
    val mood: List<MoodRecord>,
)

@Serializable
data class SynonymRecord(val slug: String, val infinitive: String)

@Serializable
data class MoodRecord(val code: String, val name: String, val tense: List<TenseRecord>)

@Serializable
data class TenseRecord(val code: String, val name: String, val form: List<FormRecord>)

/** All twelve keys are always present in the API; a part that doesn't apply is null. */
@Serializable
data class FormRecord(
    val person: String?,
    val conjugation: List<String>,
    val root: String?,
    val suffix: List<String>?,
    val auxiliary: String?,
    val semiAuxiliary: String?,
    val infinitivePreposition: String?,
    val reflexivePronoun: String?,
    val conjunction: String?,
    val preposition: String?,
    val pronoun: List<String>?,
    val spokenText: String?,
)

/** JSON settings for API and dataset data: new fields may be added at any time (A-5). */
val DatasetJson: Json = Json { ignoreUnknownKeys = true }
