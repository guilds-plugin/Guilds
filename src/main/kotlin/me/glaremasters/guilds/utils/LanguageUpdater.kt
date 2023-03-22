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
package me.glaremasters.guilds.utils

import java.io.File
import java.io.InputStream
import me.glaremasters.guilds.Guilds
import org.bukkit.configuration.file.YamlConfiguration

/**
 * A class responsible for updating the language files of a plugin.
 *
 * @property plugin the instance of the main class of the plugin.
 */
class LanguageUpdater(internal val plugin: Guilds) {


    /**
     * Saves the language files from the jar file to the plugin's data folder.
     * If a language file already exists, it will merge the new file with the old file.
     * Otherwise, it will create a new file in the plugin's data folder.
     */
    fun saveLang() {
        JarFileWalker.walk("/languages") { path, stream ->

            if (stream == null) {
                return@walk // do nothing if the stream couldn't be opened
            }

            val file = plugin.dataFolder.resolve(path.toString().drop(1)).absoluteFile
            if (file.exists()) {
                mergeLanguage(stream, file)
                return@walk // language file was already created
            }

            file.parentFile.mkdirs()
            file.createNewFile()

            file.outputStream().use {
                stream.copyTo(it)
                stream.close()
            }

            stream.close()
        }
    }

    /**
     * Merges the new language file with the existing language file.
     *
     * @param stream the input stream of the new language file.
     * @param outside the file of the existing language file.
     */
    private fun mergeLanguage(stream: InputStream, outside: File) {
        val new = YamlConfiguration.loadConfiguration(stream.reader())
        val old = YamlConfiguration.loadConfiguration(outside)

        for (path in new.getKeys(true)) {
            old.set(path, old.get(path, new.get(path)))
        }
        old.save(outside)
        stream.close()
    }
}
