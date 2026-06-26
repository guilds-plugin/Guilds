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
package me.glaremasters.guilds.database;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Utility methods for JSON persistence files.
 */
public final class JsonFileUtils {

    private JsonFileUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <T> T readJson(File file, Gson gson, Class<T> type) throws IOException {
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, type);
        }
    }

    public static <T> T readJson(File file, Gson gson, Class<T> type, String description) throws IOException {
        try {
            return readJson(file, gson, type);
        } catch (IOException ex) {
            throw new IOException("Failed to read " + description + " JSON file: " + file.getAbsolutePath(), ex);
        } catch (RuntimeException ex) {
            throw new IOException("Failed to parse " + description + " JSON file: " + file.getAbsolutePath(), ex);
        }
    }

    public static <T> T readJson(File file, Gson gson, Type type) throws IOException {
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, type);
        }
    }

    public static <T> T readJson(File file, Gson gson, Type type, String description) throws IOException {
        try {
            return readJson(file, gson, type);
        } catch (IOException ex) {
            throw new IOException("Failed to read " + description + " JSON file: " + file.getAbsolutePath(), ex);
        } catch (RuntimeException ex) {
            throw new IOException("Failed to parse " + description + " JSON file: " + file.getAbsolutePath(), ex);
        }
    }

    public static void writeAtomically(File file, String data) throws IOException {
        Path target = file.toPath();
        Path parent = target.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path tempFile = parent == null
                ? Files.createTempFile(target.getFileName().toString(), ".tmp")
                : Files.createTempFile(parent, target.getFileName().toString(), ".tmp");

        try {
            Files.write(tempFile, data.getBytes(StandardCharsets.UTF_8));
            moveReplacing(tempFile, target);
        } catch (IOException ex) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException deleteEx) {
                ex.addSuppressed(deleteEx);
            }
            throw ex;
        }
    }

    private static void moveReplacing(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
