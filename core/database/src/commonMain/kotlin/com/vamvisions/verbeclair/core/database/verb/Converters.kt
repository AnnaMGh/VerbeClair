package com.vamvisions.verbeclair.core.database.verb

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** Lists of form parts are stored as JSON arrays (design D2). */
class Converters {
    @TypeConverter
    fun fromList(value: List<String>?): String? = value?.let { Json.encodeToString(StringList, it) }

    @TypeConverter
    fun toList(value: String?): List<String>? = value?.let { Json.decodeFromString(StringList, it) }

    private companion object {
        val StringList = ListSerializer(String.serializer())
    }
}
