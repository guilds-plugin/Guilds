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
