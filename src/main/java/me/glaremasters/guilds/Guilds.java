package me.glaremasters.guilds;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.InvalidCommandArgument;
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
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.stream.Stream;

import static me.glaremasters.guilds.utils.AnnouncementUtil.unescape_perl_string;
import static me.glaremasters.guilds.utils.ConfigUtils.color;

public final class Guilds extends JavaPlugin {

    private static Guilds guilds;
    private DatabaseProvider database;
    private GuildHandler guildHandler;
    private ActionHandler actionHandler;
    private BukkitCommandManager manager;
    private static TaskChainFactory taskChainFactory;
    public static boolean vaultEconomy;
    private static Economy economy = null;
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
        vaultEconomy = setupEconomy();
        setupPermissions();
        info("Hooked into Economy and Permissions!");
        initializePlaceholder();
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
        getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
            SpigotUpdater updater = new SpigotUpdater(Guilds.getGuilds(), 48920);
            @Override
            public void run() {
                updateCheck(updater);
                info(getAnnouncements());
            }
        });


        Stream.of(new GuildPerks(), new Players(this)).forEach(l -> Bukkit.getPluginManager().registerEvents(l, this));
        info("Ready to go! That only took " + (System.currentTimeMillis() - start) + "ms");
        checkPaper();
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
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) economy = economyProvider.getProvider();
        return (economy != null);
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
                info("You appear to be running a version other than our latest stable release." + " You can download our newest version at: " + updater.getResourceURL());
            }
        } catch (Exception ex) {
            info("Could not check for updates! Stacktrace:");
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
     public void info(String msg) {
        Bukkit.getServer().getConsoleSender().sendMessage(color(logPrefix + msg));
    }

    /**
     * Guilds logo in console
     */
    private void logo() {
        info("");
        info("  .oooooo.                 o8o  oooo        .o8                ooooo ooooo ooooo ");
        info(" d8P'  `Y8b                `\"'  `888       \"888                `888' `888' `888' ");
        info("888           oooo  oooo  oooo   888   .oooo888   .oooo.o       888   888   888  ");
        info("888           `888  `888  `888   888  d88' `888  d88(  \"8       888   888   888  ");
        info("888     ooooo  888   888   888   888  888   888  `\"Y88b.        888   888   888  ");
        info("`88.    .88'   888   888   888   888  888   888  o.  )88b       888   888   888  ");
        info(" `Y8bood8P'    `V88V\"V8P' o888o o888o `Y8bod88P\" 8\"\"888P'      o888o o888o o888o");
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

    public String getAnnouncements() {
        String announcement;
        try {
            URL url = new URL("https://glaremasters.me/guilds/announcements/?id=" + getDescription()
                    .getVersion());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            try (InputStream in = con.getInputStream()) {
                String encoding = con.getContentEncoding();
                encoding = encoding == null ? "UTF-8" : encoding;
                announcement = unescape_perl_string(IOUtils.toString(in, encoding));
                con.disconnect();
            }
        } catch (Exception exception) {
            announcement = "Could not fetch announcements!";
        }
        return announcement;
    }

    private void checkPaper() {
        if (!Bukkit.getName().equalsIgnoreCase("Paper")) {
            info("Hey, it appears you aren't using Paper! Paper is a faster, more active version of Spigot that all your plugins will still work on, learn more at https://whypaper.emc.gs/");
        }
        else {
            info("Thanks for using this plugin on Paper! It will work a lot better!");
        }
    }

    private boolean checkMVDW() {
        return Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI");
    }

    private void initializePlaceholder() {

        if (checkMVDW()) {
            info("Hooking into MVdWPlacholderAPI...");
            PlaceholderAPI.registerPlaceholder(this, "guild_name", e -> api.getGuild(e.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_master", e -> api.getGuildMaster(e.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_member_count", e -> api.getGuildMemberCount(e.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_status", e -> api.getGuildStatus(e.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_role", e -> api.getGuildRole(e.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_prefix", e -> api.getGuildPrefix(e.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_members_online", e -> api.getGuildMembersOnline(e.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_tier", e -> String.valueOf(api.getGuildTier(e.getPlayer())));
            PlaceholderAPI.registerPlaceholder(this, "guild_balance", e -> String.valueOf(api.getBankBalance(e.getPlayer())));
            PlaceholderAPI.registerPlaceholder(this, "guild_tier_name", e -> api.getTierName(e.getPlayer()));
            info("Hooked!");
        }

    }

    public Economy getEconomy() {
        return economy;
    }

}
