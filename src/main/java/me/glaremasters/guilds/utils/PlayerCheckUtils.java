package me.glaremasters.guilds.utils;

import ch.jalu.configme.SettingsManager;
import me.glaremasters.guilds.configuration.sections.HooksSettings;
import me.glaremasters.guilds.configuration.sections.WorldsWhitelistSettings;

import org.bukkit.entity.Player;

public class PlayerCheckUtils {
    
    private static SettingsManager settingsManager;

    /**
     * Check if DungeonsXL hook enabled and player in DungeonsXL world
     * @return if hook enabled and player in DXL world
     */
    public static boolean checkDXLWorld(Player player) {
        return (settingsManager.getProperty(HooksSettings.DUNGEONSXL) && player.getWorld().toString().contains("DXL_"));
    }

    /**
     * Check if worlds whitelist enabled and player world
     * @return if worlds whitelist disabled or player in whitelisted world
     */
    public static boolean checkValidWorld(Player player) {
        if (settingsManager.getProperty(WorldsWhitelistSettings.WORLDS_WHITELIST)) {
            return settingsManager.getProperty(WorldsWhitelistSettings.WORLDS).contains(player.getWorld().toString());
        }
        return true;
    }
    
}
