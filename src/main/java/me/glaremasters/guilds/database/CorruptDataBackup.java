package me.glaremasters.guilds.database;

import me.glaremasters.guilds.utils.LoggingUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Keeps a copy of data the plugin could not read.
 *
 * <p>A file the plugin cannot parse is often the only copy of that record. The plugin has no
 * in-memory version of it, so anything it wrote in its place would be a guess. Rather than
 * discarding the file or writing over it, the file is copied to a backup directory and the
 * original is left exactly where the operator put it.
 *
 * <p>The backups live outside every data folder the plugin reads, under
 * {@code plugins/Guilds/backups/corrupt/<timestamp>/<data-type>/}. The plugin enumerates only its
 * own data folders, so a backup is never loaded as a record, however it is named.
 *
 * <p>The timestamp comes from the file's own last-modified time rather than from the moment the
 * backup was taken. An unchanged file therefore always lands in the same directory, and a second
 * read of the same corruption finds the copy that is already there instead of making another one.
 * The same holds across a restart.
 */
public final class CorruptDataBackup {

    /**
     * The directory under the plugin's data folder that holds the backups.
     */
    public static final String BACKUP_DIRECTORY = "backups/corrupt";

    /**
     * The timestamp format used for a backup directory.
     */
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneId.systemDefault());

    private CorruptDataBackup() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * What happened when a backup was attempted.
     */
    public static final class Result {

        private final boolean created;
        private final boolean skipped;
        private final boolean failed;
        private final File backupFile;
        private final String failure;

        private Result(boolean created, boolean skipped, boolean failed, File backupFile, String failure) {
            this.created = created;
            this.skipped = skipped;
            this.failed = failed;
            this.backupFile = backupFile;
            this.failure = failure;
        }

        /**
         * Whether a new copy was written.
         *
         * @return true when a backup file was created
         */
        public boolean isCreated() {
            return created;
        }

        /**
         * Whether an identical backup was already there.
         *
         * @return true when the corruption had already been backed up
         */
        public boolean isSkipped() {
            return skipped;
        }

        /**
         * Whether the backup could not be written.
         *
         * @return true when the backup failed
         */
        public boolean isFailed() {
            return failed;
        }

        /**
         * Where the backup is.
         *
         * @return the backup file, or null when the backup failed
         */
        public File getBackupFile() {
            return backupFile;
        }

        /**
         * Why the backup failed.
         *
         * @return the reason, or null when the backup succeeded
         */
        public String getFailure() {
            return failure;
        }
    }

    /**
     * Copies a file the plugin could not read into the backup directory.
     *
     * <p>The copy is byte for byte. The original file is not moved, not truncated and not
     * overwritten: it is left exactly as it was, and a backup is a safeguard rather than
     * permission to replace it.
     *
     * @param pluginDataFolder the plugin's data folder, {@code plugins/Guilds}
     * @param sourceFile       the file that could not be read
     * @param dataType         the kind of record the file holds, used as a directory name
     * @return what happened
     */
    public static Result backup(File pluginDataFolder, File sourceFile, String dataType) {
        if (pluginDataFolder == null || sourceFile == null || !sourceFile.isFile()) {
            return new Result(false, false, true, null,
                    "there was no file to copy: " + (sourceFile == null ? "no file was given" : sourceFile.getAbsolutePath()));
        }

        final File directory = backupDirectory(pluginDataFolder, sourceFile, dataType);
        final File original = new File(directory, sourceFile.getName());

        // The same corruption, read again, must not produce a second copy.
        if (original.isFile() && sameContent(original, sourceFile)) {
            return new Result(false, true, false, original, null);
        }

        // Something with that name is already there and holds different data, so the copy needs a
        // name of its own rather than the one that is taken.
        File target = original;
        if (target.exists()) {
            target = freeName(directory, sourceFile.getName());
            if (target == null) {
                return new Result(false, false, true, null,
                        "the backup directory holds a file with that name and it could not be replaced: " + directory.getAbsolutePath());
            }
        }

        try {
            final Path source = sourceFile.toPath();
            final Path destination = target.toPath();
            Files.createDirectories(destination.getParent());
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            return new Result(true, false, false, target, null);
        } catch (IOException | RuntimeException ex) {
            final String reason = ex.getMessage() == null ? ex.toString() : ex.getMessage();
            LoggingUtils.severe("The backup of " + sourceFile.getAbsolutePath() + " could not be written to "
                    + directory.getAbsolutePath() + ". The file has been left untouched: " + reason, ex);
            return new Result(false, false, true, null, reason);
        }
    }

    /**
     * Whether a backup file is a byte-for-byte copy of its source.
     *
     * @param sourceFile the original file
     * @param backupFile the proposed backup
     * @return true when both files exist and have the same contents
     */
    public static boolean isVerifiedCopy(File sourceFile, File backupFile) {
        return sourceFile != null && sourceFile.isFile() && backupFile != null && backupFile.isFile()
                && sameContent(sourceFile, backupFile);
    }

    /**
     * The directory a backup of a file goes in.
     *
     * <p>The timestamp is the file's own last-modified time, so an unchanged file always maps to
     * the same directory and is only ever backed up once.
     *
     * @param pluginDataFolder the plugin's data folder
     * @param sourceFile       the file being backed up
     * @param dataType         the kind of record the file holds
     * @return the directory
     */
    private static File backupDirectory(File pluginDataFolder, File sourceFile, String dataType) {
        final long modified = sourceFile.lastModified();
        final String timestamp = modified <= 0L
                ? TIMESTAMP.format(Instant.now())
                : TIMESTAMP.format(Instant.ofEpochMilli(modified));
        return new File(new File(new File(pluginDataFolder, BACKUP_DIRECTORY), timestamp), safeName(dataType));
    }

    /**
     * A name that can be used as a directory name.
     *
     * @param dataType the kind of record
     * @return the name
     */
    private static String safeName(String dataType) {
        if (dataType == null || dataType.trim().isEmpty()) {
            return "unknown";
        }
        return dataType.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    /**
     * A free name for a backup inside its directory.
     *
     * <p>The original name is kept, so the operator can tell what the file was. When something with
     * that name is already there, a number is added until the name is free, so a backup never
     * overwrites another one.
     *
     * @param directory the backup directory
     * @param name      the original file name
     * @return the name to write, or null when none could be found
     */
    private static File freeName(File directory, String name) {
        for (int suffix = 1; suffix < 1000; suffix++) {
            final File candidate = new File(directory, name + "." + suffix);
            if (!candidate.exists()) {
                return candidate;
            }
        }

        return null;
    }

    /**
     * Whether two files hold the same bytes.
     *
     * @param first  the first file
     * @param second the second file
     * @return true when their contents are identical
     */
    private static boolean sameContent(File first, File second) {
        try {
            return digest(first).equals(digest(second));
        } catch (IOException | RuntimeException ex) {
            return false;
        }
    }

    /**
     * The SHA-256 digest of a file's contents.
     *
     * @param file the file
     * @return the digest, as hex
     * @throws IOException if the file cannot be read
     */
    private static String digest(File file) throws IOException {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException("SHA-256 is not available", ex);
        }

        final byte[] hash = digest.digest(Files.readAllBytes(file.toPath()));
        final StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte value : hash) {
            hex.append(Character.forDigit((value >> 4) & 0xF, 16));
            hex.append(Character.forDigit(value & 0xF, 16));
        }
        return hex.toString();
    }
}
