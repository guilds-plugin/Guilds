package me.glaremasters.guilds.util;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.glaremasters.guilds.Main;
import org.bukkit.plugin.Plugin;

/**
 * Created by GlareMasters on 11/20/2017.
 */
public class WorldGuardHandler {

    public WorldGuardPlugin getWorldGuard() {
        Plugin plugin = Main.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null; // Maybe you want throw an exception instead
        }

        return (WorldGuardPlugin) plugin;
    }


}
