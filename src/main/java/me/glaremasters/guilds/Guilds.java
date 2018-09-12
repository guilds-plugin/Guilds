package me.glaremasters.guilds;

import co.aikar.commands.*;
import co.aikar.locales.MessageKey;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.commands.CommandAdmin;
import me.glaremasters.guilds.commands.CommandAlly;
import me.glaremasters.guilds.commands.CommandBank;
import me.glaremasters.guilds.commands.CommandGuilds;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.database.databases.json.JSON;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.listeners.GuildPerks;
import me.glaremasters.guilds.listeners.Players;
import me.glaremasters.guilds.updater.SpigotUpdater;
import me.glaremasters.guilds.utils.ActionHandler;
import me.glaremasters.guilds.utils.LoggerUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.stream.Stream;

public final class Guilds extends JavaPlugin {

    private static Guilds guilds;
    private DatabaseProvider database;
    private GuildHandler guildHandler;
    private ActionHandler actionHandler;
    private BukkitCommandManager manager;
    private static TaskChainFactory taskChainFactory;
    private GuildsAPI api;

    @Override
    public void onEnable() {
        guilds = this;
        api = new GuildsAPI();
        setupEconomy();
        setupPermissions();
        saveData();

        taskChainFactory = BukkitTaskChainFactory.create(this);

        database = new JSON(this);
        database.initialize();

        guildHandler = new GuildHandler();
        guildHandler.enable();

        actionHandler = new ActionHandler();
        actionHandler.enable();

        manager = new BukkitCommandManager(this);
        try {
            File languageFolder = new File(getDataFolder(), "languages");
            manager.getLocales().loadYamlLanguageFile(new File(languageFolder, getConfig().getString("lang") + ".yml"), Locale.ENGLISH);
            LoggerUtils.info("Loaded successfully");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            LoggerUtils.info("Failed to load");
        }
        manager.enableUnstableAPI("help");

        manager.getCommandContexts().registerIssuerOnlyContext(Guild.class, c-> {
            Guild guild = Guild.getGuild(c.getPlayer().getUniqueId());
            if (guild == null) {
                throw new InvalidCommandArgument("No guild");
            }
            return guild;
        });
        Stream.of(new CommandGuilds(), new CommandBank(), new CommandAdmin(), new CommandAlly()).forEach(manager::registerCommand);



        SpigotUpdater updater = new SpigotUpdater(this, 48920);
        updateCheck(updater);

        Stream.of(new GuildPerks(), new Players(this)).forEach(l -> Bukkit.getPluginManager().registerEvents(l, this));
    }


    @Override
    public void onDisable() {
        guildHandler.disable();
    }

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
        Economy econ = rsp.getProvider();
        return econ != null;
    }

    /**
     * Implement Vault's Permission API
     * @return the value of the method
     */
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        Permission perms = rsp.getProvider();
        return perms != null;
    }

    /**
     * Save and handle new files if needed
     */
    private void saveData() {
        File languageFolder = new File(getDataFolder(), "languages");
        if (!languageFolder.exists()) languageFolder.mkdirs();
        File language = new File(languageFolder, getConfig().getString("lang") + ".yml");
        if (!language.exists()) Stream.of("english").forEach(l -> this.saveResource("languages/" + l + ".yml", false));
    }

    /**
     * Get the database we are using to store data
     * @return the database currently being used
     */
    public DatabaseProvider getDatabase() {
        return database;
    }

    /**
     * Get the guild handler in the plugin
     * @return the guild handler being used
     */
    public GuildHandler getGuildHandler() {
        return guildHandler;
    }

    /**
     * Get the action handler in the plugin
     * @return the action handler being used
     */
    public ActionHandler getActionHandler() {
        return actionHandler;
    }

    /**
     * Create a new chain for async
     * @param <T> taskchain
     * @return the new chain created for data modification
     */
    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    /**
     * Execute the update checker
     * @param updater the SpigotUpdater
     */
    private void updateCheck(SpigotUpdater updater) {
        try {
            if (updater.checkForUpdates()) {
                getLogger().info("You appear to be running a version other than our latest stable release." + " You can download our newest version at: " + updater.getResourceURL());
            }
        } catch (Exception ex) {
            getLogger().info("Could not check for updates! Stacktrace:");
            ex.printStackTrace();
        }
    }

    /**
     * Get a holder of the API
     * @return API holder
     */
    public GuildsAPI getApi() {
        return api;
    }

    public BukkitCommandManager getManager() {
        return manager;
    }
}
