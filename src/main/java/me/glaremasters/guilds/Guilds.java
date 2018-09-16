package me.glaremasters.guilds;

import co.aikar.commands.*;
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
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.listeners.GuildPerks;
import me.glaremasters.guilds.listeners.Players;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.updater.SpigotUpdater;
import me.glaremasters.guilds.utils.ActionHandler;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.stream.Stream;

import static me.glaremasters.guilds.utils.ConfigUtils.color;

public final class Guilds extends JavaPlugin {

    private static Guilds guilds;
    private DatabaseProvider database;
    private GuildHandler guildHandler;
    private ActionHandler actionHandler;
    private BukkitCommandManager manager;
    private static TaskChainFactory taskChainFactory;
    private GuildsAPI api;
    private String logPrefix = "&f[&aGuilds&f]&r ";

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        logo();
        guilds = this;
        info("Enabling the Guilds API...");
        api = new GuildsAPI();
        info("API Enabled!");
        info("Hooking into Vault...");
        setupEconomy();
        setupPermissions();
        info("Hooked into Economy and Permissions!");
        saveData();


        taskChainFactory = BukkitTaskChainFactory.create(this);

        database = new JSON(this);
        database.initialize();

        info("Loading Guilds...");
        guildHandler = new GuildHandler();
        guildHandler.enable();
        info("The Guilds have been loaded!");

        actionHandler = new ActionHandler();
        actionHandler.enable();

        info("Loading Commands and Language Data...");
        manager = new BukkitCommandManager(this);
        loadLanguages(manager);
        manager.enableUnstableAPI("help");
        loadContexts(manager);

        Stream.of(new CommandGuilds(), new CommandBank(), new CommandAdmin(), new CommandAlly()).forEach(manager::registerCommand);


        info("Checking for updates...");
        SpigotUpdater updater = new SpigotUpdater(this, 48920);
        updateCheck(updater);

        Stream.of(new GuildPerks(), new Players(this)).forEach(l -> Bukkit.getPluginManager().registerEvents(l, this));
        info("Ready to go! That only took " + (System.currentTimeMillis() - start) + "ms");
    }


    @Override
    public void onDisable() {
        guildHandler.disable();
        actionHandler.disable();
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
        saveDefaultConfig();
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

    /**
     * Get the CommandManager
     * @return command manager
     */
    public BukkitCommandManager getManager() {
        return manager;
    }

    /**
     * Useful tool for colorful texts to console
     * @param msg the msg you want to log
     */
    private void info(String msg) {
        Bukkit.getServer().getConsoleSender().sendMessage(color(logPrefix + msg));
    }

    /**
     * Guilds logo in console
     */
    private void logo() {
        info("  _______  __    __   __   __       _______       _______.    ___        ___   ");
        info(" /  _____||  |  |  | |  | |  |     |       \\     /       |   |__ \\      / _ \\  ");
        info("|  |  __  |  |  |  | |  | |  |     |  .--.  |   |   (----`      ) |    | | | | ");
        info("|  | |_ | |  |  |  | |  | |  |     |  |  |  |    \\   \\         / /     | | | | ");
        info("|  |__| | |  `--'  | |  | |  `----.|  '--'  |.----)   |       / /_   __| |_| | ");
        info(" \\______|  \\______/  |__| |_______||_______/ |_______/       |____| (__)\\___/  ");
        info("");
    }

    /**
     * Load the languages for the server from ACF BCM
     * @param manager ACF BCM
     */
    private void loadLanguages(BukkitCommandManager manager) {
        try {
            File languageFolder = new File(getDataFolder(), "languages");
            manager.getLocales().loadYamlLanguageFile(new File(languageFolder, getConfig().getString("lang") + ".yml"), Locale.ENGLISH);
            info("Loaded successfully!");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            info("Failed to load!");
        }
    }

    /**
     * Load the contexts for the server from ACF BCM
     * @param manager ACF BCM
     */
    private void loadContexts(BukkitCommandManager manager) {
        manager.getCommandContexts().registerIssuerOnlyContext(Guild.class, c-> {
            Guild guild = Guild.getGuild(c.getPlayer().getUniqueId());
            if (guild == null) {
                throw new InvalidCommandArgument(Messages.ERROR__NO_GUILD);
            }
            return guild;
        });

        manager.getCommandContexts().registerIssuerOnlyContext(GuildRole.class, c-> {
            Guild guild = Guild.getGuild(c.getPlayer().getUniqueId());
            if (guild == null) {
                return null;
            }
            return GuildRole.getRole(guild.getMember(c.getPlayer().getUniqueId()).getRole());
        });
    }
}
