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
        Logger.getLogger("Guilds").log(level, msg);
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
        log(Level.WARNING, msg);
    }

    /**
     * Log a message to console on SEVERE level.
     *
     * @param msg the msg you want to log.
     */
    public static void severe(String msg) {
        log(Level.SEVERE, msg);
    }


    /**
     * Guilds logLogo in console
     */
    public static void logLogo(ConsoleCommandSender sender, Plugin plugin) {
        sender.sendMessage(StringUtils.color("&a  ________ "));
        sender.sendMessage(StringUtils.color("&a /  _____/ "));
        sender.sendMessage(StringUtils.color("&a/   \\  ___ " + "  &3Guilds &8v" + plugin.getDescription().getVersion()));
        sender.sendMessage(StringUtils.color("&a\\    \\_\\  \\" + "  &3Server Version: &8" + plugin.getServer().getVersion()));
        sender.sendMessage(StringUtils.color("&a \\______  /"));
        sender.sendMessage(StringUtils.color("&a        \\/ "));
    }

}
