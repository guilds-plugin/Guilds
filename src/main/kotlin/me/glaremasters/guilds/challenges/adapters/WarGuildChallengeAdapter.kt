/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
