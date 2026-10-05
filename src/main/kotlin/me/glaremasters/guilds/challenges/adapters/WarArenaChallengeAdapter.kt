package me.glaremasters.guilds.challenges.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import me.glaremasters.guilds.arena.Arena
import java.util.*

/**
 * TypeAdapter for [Arena] objects, using Google's GSON library. This adapter is used for serializing and
 * deserializing Arena objects to/from JSON.
 */
class WarArenaChallengeAdapter : TypeAdapter<Arena>() {

    /**
     * Writes an [Arena] object to the [JsonWriter].
     *
     * @param out the [JsonWriter] to write to.
     * @param arena the [Arena] object to write.
     */
    override fun write(out: JsonWriter, arena: Arena) {
        out.beginObject()
        out.name("uuid")
        out.value(arena.id.toString())
        out.name("name")
        out.value(arena.name)
        out.endObject()
    }

    /**
     * Reads an [Arena] object from the [JsonReader].
     *
     * @param reader the [JsonReader] to read from.
     * @return the [Arena] object read from the [JsonReader].
     */
    override fun read(reader: JsonReader): Arena {
        var arenaName: String? = null
        var arenaId: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "uuid" -> {
                    arenaId = reader.nextString()
                }

                "name" -> {
                    arenaName = reader.nextString()
                }

                else -> {
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        if (arenaName == null) {
            arenaName = "Default"
        }
        return Arena(UUID.fromString(arenaId), arenaName)
    }
}
