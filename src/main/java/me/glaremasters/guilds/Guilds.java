package me.glaremasters.guilds;

import java.io.File;
import java.util.stream.Stream;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Guilds extends JavaPlugin {

    private static Guilds guilds;
    private static Economy econ = null;
    private static Permission perms = null;
    private File guild, language, languageFolder;
    public YamlConfiguration guildConfig, languageConfig;

    @Override
    public void onEnable() {
        guilds = this;
        setupEconomy();
        setupPermissions();
        initData();
        saveData();
    }

    @Override
    public void onDisable() {}

    /**
     * Grabs an instance of the plugin
     * @return instance of plugin
     */
    public static Guilds getGuilds() {
        return guilds;
    }

    /**
     * Implement Vault's Economy API
     * @return the value of the method
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    /**
     * Implement Vault's Permission API
     * @return the value of the method
     */
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    /**
     * Initiate plugin data
     */
    private void initData() {
        saveDefaultConfig();
        this.languageFolder = new File(getDataFolder(), "languages");
        if (!languageFolder.exists()) languageFolder.mkdirs();
        this.guild = new File(getDataFolder(), "guilds.yml");
        this.language = new File(languageFolder, getConfig().getString("lang") + ".yml");
        this.languageConfig = YamlConfiguration.loadConfiguration(language);
    }

    /**
     * Save and handle new files if needed
     */
    private void saveData() {
        if (!this.guild.exists()) this.saveResource("guilds.yml", false);
        if (!this.language.exists()) Stream.of("english").forEach(l -> this.saveResource("languages/" + l + ".yml", false));
    }
}
