package me.glaremasters.guilds;

import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public final class Guilds extends JavaPlugin {

    private static Guilds guilds;

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    /**
     * Grabs an instance of the plugin
     * @return instance of plugin
     */
    public static Guilds getGuilds() {
        return guilds;
    }
}
