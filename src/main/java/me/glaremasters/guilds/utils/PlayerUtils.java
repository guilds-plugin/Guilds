package me.glaremasters.guilds.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * Utility class for working with players in Minecraft.
 */
public class PlayerUtils {

    /**
     * Retrieve a player object from their name.
     *
     * @param target the name of the player
     * @return the player object for the specified name
     */
    public static OfflinePlayer getPlayer(String target) {
        return Bukkit.getOfflinePlayer(target);
    }

    /**
     * Retrieve a player object from their UUID.
     *
     * @param uuid the UUID of the player
     * @return the player object for the specified UUID
     */
    public static OfflinePlayer getPlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }
}
