package me.glaremasters.guilds.database;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Collects what the plugin logged while a block of code ran.
 *
 * <p>The plugin logs through {@code java.util.logging} under the logger name {@code Guilds}, so a
 * handler on that logger is enough to see what it said. The tests use this to check that the
 * plugin did not report a problem about a file it was supposed to ignore.
 */
public final class LogCapture implements AutoCloseable {

    private final Logger logger = Logger.getLogger("Guilds");
    private final Handler handler;
    private final List<LogRecord> records = new ArrayList<>();

    public LogCapture() {
        handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
    }

    /**
     * @return the messages logged so far
     */
    public List<String> messages() {
        final List<String> messages = new ArrayList<>();
        for (LogRecord record : records) {
            messages.add(record.getMessage());
        }
        return messages;
    }

    /**
     * @param needle a fragment of the message to look for
     * @return true if any logged message contains the fragment
     */
    public boolean contains(String needle) {
        for (String message : messages()) {
            if (message.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() {
        logger.removeHandler(handler);
    }
}
