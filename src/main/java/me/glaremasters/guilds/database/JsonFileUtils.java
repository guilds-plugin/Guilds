package me.glaremasters.guilds.database;

import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Utility methods for JSON persistence files.
 */
public final class JsonFileUtils {

    /**
     * The extension every data file the plugin writes carries.
     *
     * <p>The writers produce the record id and this extension: {@code createGuild} and
     * {@code updateGuild} write {@code id + ".json"}, and the arena and challenge providers do the
     * same. Nothing else in a data folder is a record.
     */
    public static final String DATA_FILE_EXTENSION = ".json";

    private JsonFileUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Lists the files in a folder that hold a record.
     *
     * <p>A data folder belongs to the plugin, but the operator can see it and can put things in
     * it. It can therefore hold more than the files the plugin wrote: the temporary file a write
     * that was interrupted left behind, a note, a backup folder. Treating every entry as a record
     * made a stray file stand in for a guild that did not exist, and made the plugin complain
     * about files it had never written.
     *
     * <p>The rule is narrow on purpose. A file is a record when it is a regular file whose name
     * ends in {@code .json}. The temporary file that {@link #writeAtomically(File, String)} creates
     * next to its target ends in {@code .tmp}, so an interrupted write is ignored. A file the
     * operator put there is ignored as well.
     *
     * <p>Ignored files are left alone. They are not the plugin's to remove.
     *
     * @param folder the folder to look in
     * @return the data files in the folder, in no particular order, empty if the folder cannot be read
     */
    public static List<File> listDataFiles(@Nullable File folder) {
        final List<File> dataFiles = new ArrayList<>();

        if (folder == null) {
            return dataFiles;
        }

        final File[] files = folder.listFiles();

        if (files == null) {
            return dataFiles;
        }

        for (File file : files) {
            if (isDataFile(file)) {
                dataFiles.add(file);
            }
        }

        return dataFiles;
    }

    /**
     * Checks whether a file holds a record.
     *
     * @param file the file to check
     * @return true if the file is a regular file whose name ends in {@code .json}
     */
    public static boolean isDataFile(@Nullable File file) {
        if (file == null || !file.isFile()) {
            return false;
        }

        final String name = file.getName();
        return name.length() > DATA_FILE_EXTENSION.length()
                && name.toLowerCase(Locale.ROOT).endsWith(DATA_FILE_EXTENSION);
    }

    /**
     * Gets the id of the record a file holds.
     *
     * <p>The id is the name of the file without its extension. That is what the writers use, and
     * it is the key the SQL providers use for the same records, so the two backends have to agree
     * on it.
     *
     * @param file the file to read the id from
     * @return the id of the record
     */
    public static String getRecordId(File file) {
        final String name = file.getName();
        final int dotIndex = name.lastIndexOf('.');
        return dotIndex == -1 ? name : name.substring(0, dotIndex);
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
