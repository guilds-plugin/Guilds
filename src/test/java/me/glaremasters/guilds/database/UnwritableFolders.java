package me.glaremasters.guilds.database;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Locks a folder so the files in it cannot be removed.
 *
 * <p>A file in a folder that cannot be written to cannot be unlinked, which is how a delete
 * fails in the field: the folder was made read-only, or the file system ran out of space.
 */
public final class UnwritableFolders {

    private UnwritableFolders() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Takes the write permission off a folder.
     *
     * @param folder the folder to lock
     * @return true when the folder really cannot be written to. A process that can write to it
     *         anyway, such as one running as root, can still remove the files in it.
     * @throws IOException if the permissions cannot be changed
     */
    public static boolean lock(Path folder) throws IOException {
        folder.toFile().setWritable(false, false);
        return !folder.toFile().canWrite();
    }

    /**
     * Gives a folder its write permission back, so the folder can be cleaned up afterwards.
     *
     * @param folder the folder to unlock
     */
    public static void unlock(Path folder) {
        folder.toFile().setWritable(true, false);
    }
}
