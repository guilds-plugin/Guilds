package me.glaremasters.guilds;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import io.papermc.lib.PaperLib;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.api.Metrics;
import me.glaremasters.guilds.commands.CommandAdmin;
import me.glaremasters.guilds.commands.CommandAlly;
import me.glaremasters.guilds.commands.CommandBank;
import me.glaremasters.guilds.commands.CommandClaim;
import me.glaremasters.guilds.commands.CommandGuilds;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.database.databases.json.JSON;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.listeners.EssentialsChat;
import me.glaremasters.guilds.listeners.GuildPerks;
import me.glaremasters.guilds.listeners.InventoryListener;
import me.glaremasters.guilds.listeners.Players;
import me.glaremasters.guilds.listeners.TablistListener;
import me.glaremasters.guilds.listeners.Tickets;
import me.glaremasters.guilds.listeners.WorldGuard;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.updater.SpigotUpdater;
import me.glaremasters.guilds.utils.ActionHandler;
import me.glaremasters.guilds.utils.HeadUtils;
import me.glaremasters.guilds.utils.Serialization;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import static co.aikar.commands.ACFBukkitUtil.color;
import static me.glaremasters.guilds.utils.AnnouncementUtil.unescape_perl_string;

public final class Guilds extends JavaPlugin {

    public static Guilds guilds;
    private DatabaseProvider database;
    private GuildHandler guildHandler;
    private ActionHandler actionHandler;
    private BukkitCommandManager manager;
    private static TaskChainFactory taskChainFactory;
    public static boolean vaultEconomy;
    public static boolean vaultPermissions;
    private static Economy economy = null;
    private static Permission permissions = null;
    private GuildsAPI api;
    private String logPrefix = "&f[&aGuilds&f]&r ";
    private List<Player> spy;
    private Map<Guild, Inventory> vaults;

    @Override
    public void onEnable() {
        if (!checkVault()) {
            info("It looks like you don't have Vault on your server! You need this to use Guilds!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        long start = System.currentTimeMillis();
        logo();
        guilds = this;
        info("Enabling the Guilds API...");
        api = new GuildsAPI();
        spy = new ArrayList<>();
        vaults = new HashMap<>();
        info("API Enabled!");
        info("Hooking into Vault...");
        vaultEconomy = setupEconomy();
        vaultPermissions = setupPermissions();
        info("Hooked into Economy and Permissions!");
        initializePlaceholder();
        info("Enabling Metrics...");
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SingleLineChart("guilds", () -> getGuildHandler().getGuilds().values().size()));
        checkConfig();
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
        manager.usePerIssuerLocale(true);
        loadLanguages(manager);
        manager.enableUnstableAPI("help");
        loadContexts(manager);

        Stream.of(new CommandGuilds(), new CommandBank(), new CommandAdmin(), new CommandAlly(), new CommandClaim()).forEach(manager::registerCommand);


        if (getConfig().getBoolean("announcements.console")) {
            info("Checking for updates...");
            getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
                SpigotUpdater updater = new SpigotUpdater(Guilds.getGuilds(), 48920);

                @Override
                public void run() {
                    updateCheck(updater);
                    info(getAnnouncements());
                }
            });
        }


        Stream.of(new GuildPerks(), new Players(this), new Tickets(this), new InventoryListener(this)).forEach(l -> Bukkit.getPluginManager().registerEvents(l, this));
        optionalListeners();
        info("Ready to go! That only took " + (System.currentTimeMillis() - start) + "ms");
        PaperLib.suggestPaper(this);
        loadSkulls();
        createVaultCaches();
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::saveVaultCaches, 500L, 2400L);
    }


    @Override
    public void onDisable() {
        if (checkVault()) {
            saveVaultCaches();
            guildHandler.disable();
            actionHandler.disable();
            spy.clear();
            HeadUtils.textures.clear();
            vaults.clear();
        }
    }

    /**
     * Grabs an instance of the plugin
     *
     * @return instance of plugin
     */
    public static Guilds getGuilds() {
        return guilds;
    }

    /**
     * Implement Vault's Economy API
     *
     * @return the value of the method
     */
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) economy = economyProvider.getProvider();
        return (economy != null);
    }

    /**
     * Implement Vault's Permission API
     *
     * @return the value of the method
     */
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) permissions = rsp.getProvider();
        return (permissions != null);
    }

    /**
     * Save and handle new files if needed
     */
    private void saveData() {
        saveDefaultConfig();
        File languageFolder = new File(getDataFolder(), "languages");
        if (!languageFolder.exists()) languageFolder.mkdirs();
        try {
            final JarURLConnection connection = (JarURLConnection) getClassLoader().getResource("languages").openConnection();
            final JarFile thisJar = connection.getJarFile();
            final Enumeration<JarEntry> entries = thisJar.entries();
            while (entries.hasMoreElements()) {
                final JarEntry current = entries.nextElement();
                if (!current.getName().startsWith("languages/") || current.getName().length() == "languages/".length()) {
                    continue;
                }
                final String name = current.getName().substring("languages/".length());
                File langFile = new File(languageFolder, name);
                if (!langFile.exists()) {
                    this.saveResource("languages/" + name, false);
                }
            }

        } catch (final IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Preload skulls on the server to prevent lag
     */
    private void loadSkulls() {
        getServer().getScheduler().runTaskLaterAsynchronously(this, () -> getGuildHandler().getGuilds().values().forEach(guild -> {
            if (guild.getTexture().equalsIgnoreCase("")) {
                guild.updateTexture(HeadUtils.getTextureUrl(guild.getGuildMaster().getUniqueId()));
                ItemStack skull = HeadUtils.getSkull(HeadUtils.getTextureUrl(guild.getGuildMaster().getUniqueId()));
            } else {
                HeadUtils.textures.put(guild.getGuildMaster().getUniqueId(), guild.getTexture());
            }
        }), 100L);
    }

    private void createVaultCaches() {
        String vaultName;
        try {
            vaultName = color(getConfig().getString("gui-name.vault"));
        } catch (Exception ex) {
            vaultName = color("Guild Vault");
        }
        String finalVaultName = vaultName;
        getServer().getScheduler().runTaskLater(this, () -> getGuildHandler().getGuilds().values().forEach(guild -> {
            if (guild.getInventory().equalsIgnoreCase("")) {
                Inventory inv = Bukkit.createInventory(null, 54, finalVaultName);
                vaults.put(guild, inv);
            } else {
                try {
                    vaults.put(guild, Serialization.deserializeInventory(guild.getInventory()));
                } catch (InvalidConfigurationException ex) {
                    ex.printStackTrace();
                }
            }
        }), 100L);
    }

    /**
     * Create a new Vault when a guild is created while server is running
     * @param guild
     */
    public void createNewVault(Guild guild) {
        String vaultName;
        try {
            vaultName = color(getConfig().getString("gui-name.vault"));
        } catch (Exception ex) {
            vaultName = color("Guild Vault");
        }
        String finalVaultName = vaultName;
        Inventory inv = Bukkit.createInventory(null, 54, finalVaultName);
        vaults.put(guild, inv);
    }

    private void saveVaultCaches() {
        getGuildHandler().getGuilds().values().forEach(guild -> {
            guild.updateInventory(Serialization.serializeInventory(getVaults().get(guild)));
        });
    }

    /**
     * Load the languages for the server from ACF BCM
     *
     * @param manager ACF BCM
     */
    private void loadLanguages(BukkitCommandManager manager) {
        try {
            File languageFolder = new File(getDataFolder(), "languages");
            for (File file : Objects.requireNonNull(languageFolder.listFiles())) {
                if (file.isFile()) {
                    String updatedName = file.getName().replace(".yml", "");
                    manager.addSupportedLanguage(Locale.forLanguageTag(updatedName));
                    manager.getLocales().loadYamlLanguageFile(new File(languageFolder, file.getName()), Locale.forLanguageTag(updatedName));
                }
            }
            manager.getLocales().setDefaultLocale(Locale.forLanguageTag(getConfig().getString("lang")));
            info("Loaded successfully!");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            info("Failed to load!");
        }
    }

    /**
     * Get the database we are using to store data
     *
     * @return the database currently being used
     */
    public DatabaseProvider getDatabase() {
        return database;
    }

    /**
     * Get the guild handlers in the plugin
     *
     * @return the guild handlers being used
     */
    public GuildHandler getGuildHandler() {
        return guildHandler;
    }

    /**
     * Get the action handlers in the plugin
     *
     * @return the action handlers being used
     */
    public ActionHandler getActionHandler() {
        return actionHandler;
    }

    /**
     * Create a new chain for async
     *
     * @param <T> taskchain
     * @return the new chain created for data modification
     */
    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    /**
     * Execute the update checker
     *
     * @param updater the SpigotUpdater
     */
    private void updateCheck(SpigotUpdater updater) {
        try {
            if (getConfig().getBoolean("check-for-updates")) {
                if (updater.checkForUpdates()) {
                    info("You appear to be running a version other than our latest stable release." + " You can download our newest version at: " + updater.getResourceURL());
                }
            }
        } catch (Exception ex) {
            info("Could not check for updates! Stacktrace:");
            ex.printStackTrace();
        }
    }

    /**
     * Get a holder of the API
     *
     * @return API holder
     */
    public GuildsAPI getApi() {
        return api;
    }

    /**
     * Get the CommandManager
     *
     * @return command manager
     */
    public BukkitCommandManager getManager() {
        return manager;
    }

    /**
     * Useful tool for colorful texts to console
     *
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
     * Load the contexts for the server from ACF BCM
     *
     * @param manager ACF BCM
     */
    private void loadContexts(BukkitCommandManager manager) {
        manager.getCommandContexts().registerIssuerOnlyContext(Guild.class, c -> {
            Guild guild = Guild.getGuild(c.getPlayer().getUniqueId());
            if (guild == null) {
                throw new InvalidCommandArgument(Messages.ERROR__NO_GUILD);
            }
            return guild;
        });

        manager.getCommandContexts().registerIssuerOnlyContext(GuildRole.class, c -> {
            Guild guild = Guild.getGuild(c.getPlayer().getUniqueId());
            if (guild == null) {
                return null;
            }
            return GuildRole.getRole(guild.getMember(c.getPlayer().getUniqueId()).getRole());
        });
    }

    /**
     * Grab the announcements for the plugins
     *
     * @return the announcements string
     */
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

    /**
     * Check if MVdWPlaceholderAPI is running
     *
     * @return true or false
     */
    private boolean checkMVDW() {
        return Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI");
    }

    /**
     * Check if Vault is running
     *
     * @return true or false
     */
    private boolean checkVault() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault");
    }

    /**
     * Check if PAPI is running
     * @return
     */
    private boolean checkPAPI() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    /**
     * Load all the placeholders for MVDW
     */
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

        if (checkPAPI()) {
            info("Hooking into PlaceholderAPI...");
            new me.glaremasters.guilds.utils.PlaceholderAPI(guilds).register();
            info("Hooked!");
        }

    }

    /**
     * Get the economy
     *
     * @return economy
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * Get the permissions from vault
     *
     * @return the permissions from vault
     */
    public Permission getPermissions() {
        return permissions;
    }

    /**
     * Register optional listeners based off values in the config
     */
    public void optionalListeners() {
        if (getConfig().getBoolean("main-hooks.essentials-chat")) {
            getServer().getPluginManager().registerEvents(new EssentialsChat(this), this);
        }

        if (getConfig().getBoolean("main-hooks.tablist-guilds")) {
            getServer().getPluginManager().registerEvents(new TablistListener(this), this);
        }

        if (getConfig().getBoolean("main-hooks.worldguard-claims")) {
            getServer().getPluginManager().registerEvents(new WorldGuard(this), this);
        }
    }

    /**
     * Get a list of all users that have spy mode enabled
     * @return
     */
    public List<Player> getSpy() {
        return spy;
    }

    /**
     * Check if a guild has a claim
     *
     * @param player
     * @param guild
     */
    public static void checkForClaim(Player player, Guild guild, Guilds guilds) {
        if (guilds.getConfig().getBoolean("main-hooks.worldguard-claims")) {
            WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();
            wrapper.getRegion(player.getWorld(), guild.getName()).ifPresent(region -> wrapper.removeRegion(player.getWorld(), guild.getName()));
        }
    }

    /**
     * Check the version of the config
     */
    private void checkConfig() {
        if (!getConfig().isSet("version") || getConfig().getInt("version") != 1) {
            if (getConfig().getBoolean("auto-update-config")) {
                File oF = new File(getDataFolder(), "config.yml");
                File nF = new File(getDataFolder(), "config-old.yml");
                oF.renameTo(nF);
                info("Your config has been auto updated. You can disabled this in the config");
            } else {
                info("Your config is out of date!");
            }
        }
    }

    /**
     * Get a list of all the Guild Vaults on the Server
     * @return
     */
    public Map<Guild, Inventory> getVaults() {
        return vaults;
    }
}
