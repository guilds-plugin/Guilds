package me.glaremasters.guilds.utils

import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

/**
 * A singleton object that provides a utility function for walking through the contents of a JAR file.
 */
object JarFileWalker {

    /**
     * Walks through the contents of a JAR file located at the given path, invoking the specified function for each file.
     *
     * @param path A string representing the path to the JAR file to walk through.
     * @param function A function that takes a Path object and an InputStream object (might be null) and does some processing.
     */
    fun walk(path: String, function: (Path, InputStream?) -> Unit) {
        FileSystems.newFileSystem(javaClass.getResource(path).toURI(), emptyMap<String, Any>()).use { files ->
            Files.walk(files.getPath(path)).forEach { path ->
                if (Files.isDirectory(path)) {
                    return@forEach // do nothing if this is a directory
                }

                try {
                    // attempt to pass the stream for this resource
                    function.invoke(path, javaClass.classLoader.getResourceAsStream(path.toString().drop(1)))
                } catch (ex: Exception) {
                    // fallback to just the path
                    function.invoke(path, null)
                }
            }
        }
    }
}
