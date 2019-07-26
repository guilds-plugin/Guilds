package me.glaremasters.guilds.utils;

import co.aikar.commands.ACFBukkitUtil;
import lombok.experimental.UtilityClass;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

@UtilityClass
public class LoggingUtils {

    /**
     * Log any message to console with any level.
     *
     * @param level the log level to log on.
     * @param msg   the message to log.
     */
    public void log(Level level, String msg) {
        Logger.getLogger("Guilds").log(level, msg);
    }

    /**
     * Log a message to console on INFO level.
     *
     * @param msg the msg you want to log.
     */
    public void info(String msg) {
        log(Level.INFO, msg);
    }

    /**
     * Log a message to console on WARNING level.
     *
     * @param msg the msg you want to log.
     */
    public void warn(String msg) {
        log(Level.WARNING, msg);
    }

    /**
     * Log a message to console on SEVERE level.
     *
     * @param msg the msg you want to log.
     */
    public void severe(String msg) {
        log(Level.SEVERE, msg);
    }


    /**
     * Guilds logLogo in console
     */
    public void logLogo(ConsoleCommandSender sender, Plugin plugin) {
        sender.sendMessage(ACFBukkitUtil.color("&a  ________ "));
        sender.sendMessage(ACFBukkitUtil.color("&a /  _____/ "));
        sender.sendMessage(ACFBukkitUtil.color("&a/   \\  ___ " + "  &3Guilds &8v" + plugin.getDescription().getVersion()));
        sender.sendMessage(ACFBukkitUtil.color("&a\\    \\_\\  \\" + "  &3Server Version: &8" + plugin.getServer().getVersion()));
        sender.sendMessage(ACFBukkitUtil.color("&a \\______  /"));
        sender.sendMessage(ACFBukkitUtil.color("&a        \\/ "));
    }

}
