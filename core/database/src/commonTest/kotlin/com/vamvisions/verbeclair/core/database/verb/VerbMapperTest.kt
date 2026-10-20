package com.vamvisions.verbeclair.core.database.verb

import com.vamvisions.verbeclair.core.database.dataset.DatasetJson
import com.vamvisions.verbeclair.core.database.dataset.VerbDetailsRecord
import com.vamvisions.verbeclair.core.database.fixture.DatasetFixtures
import com.vamvisions.verbeclair.core.database.inMemoryVerbDatabase
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VerbMapperTest {

    private fun record(json: String): VerbDetailsRecord = DatasetJson.decodeFromString(json)

    private fun searchForms(json: String): Set<String> =
        record(json)
            .toEntities()
            .formSearch
            .map { it.form }
            .toSet()

    @Test
    fun terminerKeepsVerbFieldsAndKeys() {
        val entities = record(DatasetFixtures.terminer).toEntities()
        assertEquals(
            VerbEntity(
                "terminer",
                "terminer",
                "I",
                false,
                "AVOIR",
                142.38,
                "2026-10-08T09:26:22Z",
                "terminer",
                "terminer",
            ),
            entities.verb,
        )
        assertEquals(
            TranslationEntity("terminer", "en", "to end; to finish; to terminate", "to end"),
            entities.translation,
        )
        assertEquals(
            listOf("achever", "boucler", "clore", "cloturer"),
            entities.synonyms.take(4).map { it.synonymSlug },
        )
        assertEquals("clôturer", entities.synonyms[3].synonymInfinitive)
    }

    @Test
    fun etreSimpleFormsAreSearchable() {
        val entities = record(DatasetFixtures.etre).toEntities()
        assertEquals("etre", entities.verb.infinitiveKey)
        val suis = entities.formSearch.single { it.form == "suis" }
        assertEquals("suis", suis.key)
        assertEquals("ete", entities.formSearch.single { it.form == "été" }.key)
    }

    @Test
    fun everySimpleTenseIsSearchable() {
        val forms = searchForms(DatasetFixtures.etre)

        // imparfait, passé simple, futur, conditionnel, subjonctif présent and imparfait, participe présent
        assertTrue(forms.containsAll(listOf("étais", "fus", "serai", "serais", "sois", "fusse", "étant")), "$forms")
    }

    @Test
    fun aSpellingInSeveralTensesKeepsItsFirstPlace() {
        val entities = record(DatasetFixtures.terminer).toEntities()
        val present = entities.forms.first { it.moodCode == "INDICATIF" && it.tenseCode == "PRESENT" }
        val termine = entities.formSearch.single { it.form == "termine" }

        // `termine` is also the impératif; the indicatif présent comes first (D-3).
        assertEquals(present.moodPos * 10_000 + present.tensePos * 100, termine.displayOrder)
    }

    @Test
    fun rowsReadInAnyOrderRebuildTheSameRecord() {
        val original = record(DatasetFixtures.terminer)
        val entities = original.toEntities()

        val shuffled = entities.copy(forms = entities.forms.reversed(), synonyms = entities.synonyms.reversed())

        assertEquals(original, shuffled.toRecord())
    }

    @Test
    fun compoundTensesAndSemiAuxiliariesAreNotSearchable() {
        val forms = searchForms(DatasetFixtures.aller)
        // passé composé `allé` / `allée` belongs to a compound tense; its participe passé `allé` is a simple form.
        val entities = record(DatasetFixtures.aller).toEntities()
        assertTrue(entities.formSearch.none { it.form == "allée" }, "agreement variants are not searchable")
        val alle = entities.formSearch.single { it.form == "allé" }
        val participePasse = entities.forms.first { it.moodCode == "PARTICIPE" && it.tenseCode == "PASSE" }
        assertEquals(
            participePasse.moodPos * 10_000 + participePasse.tensePos * 100 + participePasse.formPos,
            alle.displayOrder,
        )
        // futur proche `je vais aller`: the semi-auxiliary `vais` is not the verb word; `vais` is
        // only searchable through the présent of aller itself.
        val vais = entities.formSearch.single { it.form == "vais" }
        val present = entities.forms.first { it.moodCode == "INDICATIF" && it.tenseCode == "PRESENT" }
        assertEquals(present.moodPos * 10_000 + present.tensePos * 100, vais.displayOrder)
        assertFalse("vais" in searchForms(DatasetFixtures.parler), "futur proche `vais` is not matched for parler")
        assertTrue("allé" in forms)
    }

    @Test
    fun payerKeepsBothSpellingsOfOnePerson() {
        val entities = record(DatasetFixtures.payer).toEntities()
        val firstSingular =
            entities.forms.filter {
                it.moodCode == "INDICATIF" && it.tenseCode == "PRESENT" && it.person == "FIRST_SINGULAR"
            }
        assertEquals(listOf("paye", "paie"), firstSingular.map { it.conjugation.first() })
        assertTrue(entities.formSearch.map { it.form }.containsAll(listOf("paye", "paie")))
    }

    @Test
    fun reflexiveSortKeyDropsThePronoun() {
        val verb = record(DatasetFixtures.sAppeler).toEntities().verb
        assertEquals("s'appeler", verb.infinitiveKey)
        assertEquals("appeler", verb.sortKey)
    }

    @Test
    fun searchKeysAreDistinctPerVerb() {
        for (fixture in DatasetFixtures.all) {
            val keys = record(fixture).toEntities().formSearch
            assertEquals(keys.size, keys.map { it.form }.toSet().size)
        }
    }

    @Test
    fun everyFixtureRoundTripsThroughTheDatabase() =
        runTest {
            val db = inMemoryVerbDatabase()
            val dao = db.verbDao()
            dao.replace(DatasetFixtures.all.map { record(it).toEntities() })
            for (fixture in DatasetFixtures.all) {
                val slug = record(fixture).slug
                val stored =
                    VerbEntities(
                        verb = dao.verb(slug)!!,
                        forms = dao.forms(slug),
                        translation = dao.translation(slug)!!,
                        synonyms = dao.synonyms(slug),
                        formSearch = dao.formSearch(slug),
                    )
                val rebuilt: JsonElement = Json.encodeToJsonElement(VerbDetailsRecord.serializer(), stored.toRecord())
                assertEquals(Json.parseToJsonElement(fixture), rebuilt, "round trip of $slug")
            }
            db.close()
        }
}
