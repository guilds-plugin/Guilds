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
