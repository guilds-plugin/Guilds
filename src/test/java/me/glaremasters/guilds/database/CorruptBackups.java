package me.glaremasters.guilds.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Finds the backups the plugin writes for the data it cannot read.
 *
 * <p>The backups live under {@code plugins/Guilds/backups/corrupt/<timestamp>/<data-type>/}, so
 * the kind of record a backup belongs to is the name of the folder it sits in.
 */
public final class CorruptBackups {

    private CorruptBackups() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * @param pluginFolder the plugin's data folder
     * @return the folder every backup lives under
     */
    public static Path backupRoot(Path pluginFolder) {
        return pluginFolder.resolve("backups").resolve("corrupt");
    }

    /**
     * @param pluginFolder the plugin's data folder
     * @param dataType     the kind of record
     * @return every backup file of that kind of record
     * @throws IOException if the backups cannot be listed
     */
    public static List<Path> backupFiles(Path pluginFolder, String dataType) throws IOException {
        final List<Path> files = new ArrayList<>();
        final Path root = backupRoot(pluginFolder);

        if (!Files.isDirectory(root)) {
            return files;
        }

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.getParent() != null && dataType.equals(path.getParent().getFileName().toString()))
                    .forEach(files::add);
        }

        return files;
    }

    /**
     * @param pluginFolder the plugin's data folder
     * @param dataType     the kind of record
     * @return the one backup of that kind of record
     * @throws IOException if the backups cannot be listed
     */
    public static Path onlyBackup(Path pluginFolder, String dataType) throws IOException {
        final List<Path> files = backupFiles(pluginFolder, dataType);

        if (files.size() != 1) {
            fail("expected one " + dataType + " backup but found " + files.size() + ": " + files);
        }

        return files.get(0);
    }
}
