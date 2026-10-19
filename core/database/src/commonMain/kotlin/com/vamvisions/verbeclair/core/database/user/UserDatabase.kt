package com.vamvisions.verbeclair.core.database.user

import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * The user's own data (user-data spec), kept apart from the replaceable verb database. Its schema
 * is exported, so later versions add auto-migrations (design D6).
 */
@Database(
    entities = [FavouriteEntity::class, TestAttemptEntity::class, AttemptLineEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(UserConverters::class)
@ConstructedBy(UserDatabaseConstructor::class)
abstract class UserDatabase : RoomDatabase() {
    abstract fun favouriteDao(): FavouriteDao
    abstract fun attemptDao(): AttemptDao
}

@Suppress("KotlinNoActualForExpect")
expect object UserDatabaseConstructor : RoomDatabaseConstructor<UserDatabase> {
    override fun initialize(): UserDatabase
}

@Dao
interface FavouriteDao {
    @Query("SELECT slug FROM favourites")
    fun observeSlugs(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favourite: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE slug = :slug")
    suspend fun delete(slug: String)
}

@Dao
interface AttemptDao {
    /**
     * Line counts of the latest attempt of each of [slugs], a test or a review session (RV-4).
     * Lines correct without accents count as correct.
     */
    @Query(
        """
        SELECT a.slug AS slug,
               SUM(CASE WHEN l.result = 'FAILED' THEN 0 ELSE 1 END) AS correct,
               COUNT(*) AS total
        FROM test_attempts a JOIN attempt_lines l ON l.attempt_id = a.id
        WHERE a.slug IN (:slugs)
          AND a.id = (
              SELECT b.id FROM test_attempts b
              WHERE b.slug = a.slug
              ORDER BY b.started_at DESC, b.id DESC
              LIMIT 1
          )
        GROUP BY a.slug
        """,
    )
    fun observeLatestAttemptCounts(slugs: Set<String>): Flow<List<AttemptCounts>>

    @Insert
    suspend fun insertAttempt(attempt: TestAttemptEntity): Long

    @Insert
    suspend fun insertLines(lines: List<AttemptLineEntity>)

    /** Stores an attempt with its lines; [lines] get the new attempt's id. Returns that id. */
    @Transaction
    suspend fun insert(attempt: TestAttemptEntity, lines: List<AttemptLineEntity>): Long {
        val id = insertAttempt(attempt)
        insertLines(lines.map { it.copy(attemptId = id) })
        return id
    }
}

/** Selected tenses are stored as a JSON array of `{"mood", "tense"}` code pairs. */
class UserConverters {
    @TypeConverter
    fun fromTenses(value: List<SelectedTense>): String = Json.encodeToString(TensesSerializer, value)

    @TypeConverter
    fun toTenses(value: String): List<SelectedTense> = Json.decodeFromString(TensesSerializer, value)
}

// Outside the converter class: Room's KSP processor cannot see serializers made by the serialization plugin.
private val TensesSerializer = ListSerializer(SelectedTense.serializer())
