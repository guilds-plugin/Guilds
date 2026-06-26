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
package me.glaremasters.guilds.utils;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class LoggingUtils {

    private static final Logger LOGGER = Logger.getLogger("Guilds");

    private LoggingUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Log any message to console with any level.
     *
     * @param level the log level to log on.
     * @param msg   the message to log.
     */
    public static void log(Level level, String msg) {
        log(level, msg, null);
    }

    /**
     * Log any message and optional exception to console with any level.
     *
     * @param level     the log level to log on.
     * @param msg       the message to log.
     * @param throwable the exception to log, or null if there is no exception.
     */
    public static void log(Level level, String msg, Throwable throwable) {
        if (throwable == null) {
            LOGGER.log(level, msg);
            return;
        }

        LOGGER.log(level, msg, throwable);
    }

    /**
     * Log a message to console on INFO level.
     *
     * @param msg the msg you want to log.
     */
    public static void info(String msg) {
        log(Level.INFO, StringUtils.color(msg));
    }

    /**
     * Log a message to console on WARNING level.
     *
     * @param msg the msg you want to log.
     */
    public static void warn(String msg) {
        warn(msg, null);
    }

    /**
     * Log a message and optional exception to console on WARNING level.
     *
     * @param msg       the msg you want to log.
     * @param throwable the exception to log, or null if there is no exception.
     */
    public static void warn(String msg, Throwable throwable) {
        log(Level.WARNING, msg, throwable);
    }

    /**
     * Log a message to console on SEVERE level.
     *
     * @param msg the msg you want to log.
     */
    public static void severe(String msg) {
        severe(msg, null);
    }

    /**
     * Log a message and optional exception to console on SEVERE level.
     *
     * @param msg       the msg you want to log.
     * @param throwable the exception to log, or null if there is no exception.
     */
    public static void severe(String msg, Throwable throwable) {
        log(Level.SEVERE, msg, throwable);
    }

    /**
     * Guilds logLogo in console.
     *
     * @param sender the console command sender.
     * @param plugin the plugin instance.
     */
    public static void logLogo(ConsoleCommandSender sender, Plugin plugin) {
        sender.sendMessage(StringUtils.color("&a  ________ "));
        sender.sendMessage(StringUtils.color("&a /  _____/ "));
        sender.sendMessage(StringUtils.color("&a/   \\  ___   &3Guilds &8v" + plugin.getDescription().getVersion()));
        sender.sendMessage(StringUtils.color("&a\\    \\_\\  \\  &3Server Version: &8" + plugin.getServer().getVersion()));
        sender.sendMessage(StringUtils.color("&a \\______  /"));
        sender.sendMessage(StringUtils.color("&a        \\/ "));
    }

}
