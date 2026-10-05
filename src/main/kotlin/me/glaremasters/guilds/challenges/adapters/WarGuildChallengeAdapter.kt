package me.glaremasters.guilds.challenges.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import me.glaremasters.guilds.guild.Guild
import java.util.*

/**
 * WarGuildChallengeAdapter is a [TypeAdapter] for [Guild]. It allows for serializing and deserializing [Guild]
 * objects to and from JSON.
 */
class WarGuildChallengeAdapter : TypeAdapter<Guild>() {

    /**
     * Writes the given [Guild] to a [JsonWriter].
     *
     * @param out The [JsonWriter] to write the [Guild] to.
     * @param guild The [Guild] to write.
     */
    override fun write(out: JsonWriter, guild: Guild) {
        out.beginObject()
        out.name("uuid")
        out.value(guild.id.toString())
        out.endObject()
    }

    /**
     * Reads a [Guild] from a [JsonReader].
     *
     * @param reader The [JsonReader] to read the [Guild] from.
     * @return The [Guild] that was read from the [JsonReader].
     */
    override fun read(reader: JsonReader): Guild {
        reader.beginObject()
        reader.nextName()
        val id = reader.nextString()
        reader.endObject()
        return Guild(UUID.fromString(id))
    }
}
