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
