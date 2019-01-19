/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.glaremasters.guilds;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import ch.jalu.configme.migration.PlainMigrationService;
import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.InvalidCommandArgument;
import lombok.Getter;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.commands.*;
import me.glaremasters.guilds.configuration.GuildsSettingsRetriever;
import me.glaremasters.guilds.configuration.PluginSettings;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.database.providers.JsonProvider;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.listeners.*;
import me.glaremasters.guilds.utils.HeadUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.io.IOUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public final class Guilds extends JavaPlugin {

    //todo rewrite
    //order is very important here as stuff depends on each other.
    //use logical order.
    //incorrect order will result in a NPE

    private static GuildsAPI api;
    private GuildHandler guildHandler;
    private DatabaseProvider database;
    private SettingsManager settingsManager;
    private BukkitCommandManager commandManager;
    private ActionHandler actionHandler;
    private Economy economy;
    private Permission permissions;
    private List<Player> spy;

    @Override
    public void onEnable() {
        // Check if the server is running Vault
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            warn("It looks like you don't have Vault on your server! Stopping plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // This is really just for shits and giggles
        // A variable for checking how long startup took.
        long startingTime = System.currentTimeMillis();

        // Flex teh guild logLogo
        logLogo();

        // Load the config
        info("Loading config..");
        settingsManager = SettingsManagerBuilder.withYamlFile(new File(getDataFolder(), "config.yml")).migrationService(new PlainMigrationService()).configurationData(GuildsSettingsRetriever.buildConfigurationData()).create();
        info("Loaded config!");

        // Creates / loads the languages files (can be renamed to something that makes more sense) todo
        saveData();

        // Load data here.
        try {
            info("Loading Data..");
            // This will soon be changed to an automatic storage chooser from the config
            // Load the json provider
            database = new JsonProvider(getDataFolder());
            // Load guildhandler with provider
            guildHandler = new GuildHandler(database, getCommandManager(), getPermissions(), getConfig());
            info("Loaded data!");
        } catch (IOException e) {
            severe("An error occured loading data! Stopping plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Load Vault
        info("Hooking into Vault..");
        // Setup Vaults Economy Hook
        setupEconomy();
        // Setup Vaults Permission Hook
        setupPermissions();
        info("Hooked into Vault!");

        // If they have placeholderapi, enable it.
        initializePlaceholder();

        info("Enabling Metrics..");
        // start bstats
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SingleLineChart("guilds", () -> getGuildHandler().getGuildsSize()));
        info("Enabled Metrics!");

        // Initialize the action handler for actions in the plugin
        actionHandler = new ActionHandler();
        info("Loading Commands and Language Data..");
        // Load the ACF command manager
        commandManager = new BukkitCommandManager(this);
        commandManager.usePerIssuerLocale(true);
        // Load the languages
        loadLanguages(commandManager);
        //deprecated due to being unstable
        //noinspection deprecation
        commandManager.enableUnstableAPI("help");
        // load the custom command contexts
        loadContexts(commandManager);
        // load the custom command completions
        loadCompletions(commandManager);

        // Register all the commands
        Stream.of(new CommandGuilds(guildHandler), new CommandBank(guildHandler), new CommandAdmin(guildHandler), new CommandAlly(guildHandler), new CommandClaim(guildHandler)).forEach(commandManager::registerCommand);


        // This can probably be moved into it's own method
        // This checks for updates
        if (settingsManager.getProperty(PluginSettings.ANNOUNCEMENTS_CONSOLE)) {
            info("Checking for updates..");
            getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
                SpigotUpdater updater = new SpigotUpdater(, 48920);

                @Override
                public void run() {
                    updateCheck(updater);
                    info(getAnnouncements());
                }
            });


        }

        // Load all the listeners
        Stream.of(new EntityListener(guildHandler), new PlayerListener(this, guildHandler), new TicketListener(this, guildHandler), new InventoryListener(guildHandler)).forEach(l -> Bukkit.getPluginManager().registerEvents(l, this));
        // Load the optional listeners
        optionalListeners();
        // Cache all the skulls (might be able to get rid of this)
        loadSkulls();
        // Creates a cache of all the vaults (might be able to get rid of this)
        createVaultCaches();
        // Saves the vault cache (can probably get rid of)
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::saveVaultCaches, 500L, 2400L);

        info("Enabling the Guilds API..");
        // Initialize the API (probably be placed in different spot?)
        api = new GuildsAPI(getGuildHandler());
        // Create a new list for the spies
        spy = new ArrayList<>();
        info("Enabled API!");

        info("Ready to go! That only took " + (System.currentTimeMillis() - startingTime) + "ms");
    }


    @Override
    public void onDisable() {
        if (checkVault()) {
            if (!getVaults().isEmpty()) {
                saveVaultCaches();
            }
            guildHandler.saveData();
            actionHandler.disable();
            spy.clear();
            HeadUtils.textures.clear();
            vaults.clear();

        }
    }

    /**
     * Implement Vault's Economy API
     */
    private void setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) economy = economyProvider.getProvider();
    }

    /**
     * Implement Vault's Permission API
     */
    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) permissions = rsp.getProvider();
    }

    /**
     * Save and handle new files if needed
     */
    private void saveData() {
        saveDefaultConfig();
        File languageFolder = new File(getDataFolder(), "languages");
        if (!languageFolder.exists()) //noinspection ResultOfMethodCallIgnored
            languageFolder.mkdirs();
        try {
            final JarURLConnection connection = (JarURLConnection) Objects.requireNonNull(getClassLoader().getResource("languages")).openConnection();
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
        getServer().getScheduler().runTaskLaterAsynchronously(this, () -> getGuildHandler().getGuilds().forEach(guild -> {
            if (guild.getTextureUrl().equalsIgnoreCase("")) {
                guild.setTextureUrl(HeadUtils.getTextureUrl(guild.getGuildMaster().getUuid()));
                //todo is this necessary?
                HeadUtils.getSkull(HeadUtils.getTextureUrl(guild.getGuildMaster().getUuid()));
            } else {
                HeadUtils.textures.put(guild.getGuildMaster().getUuid(), guild.getTextureUrl());
            }
        }), 100L);
    }

    // todo Make a new way to cache the inventories that we need (if we need to)
    private void createVaultCaches() {
        getServer().getScheduler().runTaskLater(this, () -> getGuildHandler().getGuilds().values().forEach(guild -> {
            if (guild.getInventory().equalsIgnoreCase("")) {
                Inventory inv = Bukkit.createInventory(null, 54, getVaultName());
                vaults.put(guild, inv);
            } else {
                try {
                    vaults.put(guild, SerialisationUtil.deserializeInventory(guild.getInventory()));
                } catch (InvalidConfigurationException ex) {
                    ex.printStackTrace();
                }
            }
        }), 100L);
    }

    private String getVaultName() {
        try {
            return color(getConfig().getString("gui-name.vault"));
        } catch (Exception ex) {
            return color("Guild Vault");
        }
    }

    /**
     * Create a new Vault when a guild is created while server is running
     * @param guild the guild of which's vault needs to be created
     */
    public void createNewVault(Guild guild) {
        getVaultName();
        Inventory inv = Bukkit.createInventory(null, 54, getVaultName());
        vaults.put(guild, inv);
    }

    // todo fix this random shit up too now that we have proper data handling
    private void saveVaultCaches() {
        getGuildHandler().getGuilds().values().forEach(guild -> guild.setInventory(SerialisationUtil.serializeInventory(getVaults().get(guild))));
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
     * Execute the update checker
     *
     * @param updater the SpigotUpdater
     */
    private void updateCheck(SpigotUpdater updater) {
        try {
            if (getConfig().getBoolean("check-for-updates")) {
                if (updater.checkForUpdates()) {
                    info("You appear to be running a version other than our latest stable release." + " You can download our newest version at: " + updater.getResourceLink());
                }
            }
        } catch (Exception ex) {
            info("Could not check for updates! Stacktrace:");
            ex.printStackTrace();
        }
    }

    /**
     * Log any message to console with any level.
     *
     * @param level the log level to log on.
     * @param msg   the message to log.
     */
    public void log(Level level, String msg) {
        getLogger().log(level, msg);
    }

    /**
     * Log a message to console on INFO level.
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
    private void logLogo() {
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
            Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null) {
                throw new InvalidCommandArgument(Messages.ERROR__NO_GUILD);
            }
            return guild;
        });

        manager.getCommandContexts().registerIssuerOnlyContext(GuildRole.class, c -> {
            Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null) {
                return null;
            }

            return guildHandler.getGuildRole(guild.getMember(c.getPlayer().getUniqueId()).getRole().getLevel());
        });
    }

    private void loadCompletions(BukkitCommandManager manager) {
        manager.getCommandCompletions().registerCompletion("members", c -> {
            Guild guild = guildHandler.getGuild(c.getPlayer());
            return guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUuid()).getName()).collect(Collectors.toList());
        });

        manager.getCommandCompletions().registerCompletion("online", c -> Bukkit.getOnlinePlayers().stream()
                .map(member -> Bukkit.getPlayer(member.getUniqueId()).getName()).collect(Collectors.toList()));

        manager.getCommandCompletions().registerCompletion("invitedTo", c -> guildHandler
                .getGuilds().stream().filter(guild -> guild.getInvitedMembers().contains(c.getPlayer().getUniqueId()))
                .map(guild -> ACFBukkitUtil.removeColors(guild.getName())).collect(Collectors.toList()));

        manager.getCommandCompletions().registerCompletion("guilds", c -> guildHandler
                .getGuilds().stream()
                .map(guild -> ACFBukkitUtil.removeColors(guild.getName())).collect(Collectors.toList()));
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
     * Check if Vault is running
     *
     * @return true or false
     */
    private boolean checkVault() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault");
    }

    /**
     * Check if PAPI is running
     * @return boolean if papi is enabled
     */
    private boolean checkPAPI() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    /**
     * Load all the placeholders for MVDW
     */
    private void initializePlaceholder() {
        if (checkPAPI()) {
            info("Hooking into PlaceholderAPI..");
            new PlaceholderAPI(this).register();
            info("Hooked!");
        }
    }

    /**
     * Register optional listeners based off values in the config
     */
    private void optionalListeners() {
        if (getConfig().getBoolean("main-hooks.essentials-chat")) {
            getServer().getPluginManager().registerEvents(new EssentialsChatListener(guildHandler, api), this);
        }

        if (getConfig().getBoolean("main-hooks.tablist-guilds")) {
            getServer().getPluginManager().registerEvents(new TablistListener(this, guildHandler), this);
        }

        if (getConfig().getBoolean("main-hooks.worldguard-claims")) {
            getServer().getPluginManager().registerEvents(new WorldGuardListener(this, guildHandler), this);
        }
    }

    /**
     * Check if a guild has a claim
     *  @param world the world to check in
     * @param guild the guild to check
     */
    public static void checkForClaim(World world, Guild guild, Guilds guilds) {
        if (guilds.getConfig().getBoolean("main-hooks.worldguard-claims")) {
            WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();
            wrapper.getRegion(world, guild.getName()).ifPresent(region -> wrapper.removeRegion(world, guild.getName()));
        }
    }

    private SettingsManager loadConfig() {
        return SettingsManagerBuilder.withYamlFile(new File(getDataFolder(), "config.yml")).migrationService(new PlainMigrationService()).configurationData(GuildsSettingsRetriever.buildConfigurationData()).create();
    }


}
