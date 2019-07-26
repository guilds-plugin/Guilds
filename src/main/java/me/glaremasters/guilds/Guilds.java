/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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

import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.PaperCommandManager;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import lombok.Getter;
import me.glaremasters.guilds.acf.ACFHandler;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.arena.ArenaHandler;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.configuration.SettingsHandler;
import me.glaremasters.guilds.configuration.sections.HooksSettings;
import me.glaremasters.guilds.configuration.sections.PluginSettings;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.database.arenas.ArenasProvider;
import me.glaremasters.guilds.database.challenges.ChallengesProvider;
import me.glaremasters.guilds.database.cooldowns.CooldownsProvider;
import me.glaremasters.guilds.database.providers.JsonProvider;
import me.glaremasters.guilds.dependency.Libraries;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guis.GUIHandler;
import me.glaremasters.guilds.listeners.ArenaListener;
import me.glaremasters.guilds.listeners.ClaimSignListener;
import me.glaremasters.guilds.listeners.EntityListener;
import me.glaremasters.guilds.listeners.EssentialsChatListener;
import me.glaremasters.guilds.listeners.PlayerListener;
import me.glaremasters.guilds.listeners.TicketListener;
import me.glaremasters.guilds.listeners.VaultBlacklistListener;
import me.glaremasters.guilds.listeners.WorldGuardListener;
import me.glaremasters.guilds.placeholders.PlaceholderAPI;
import me.glaremasters.guilds.updater.UpdateChecker;
import me.glaremasters.guilds.utils.Constants;
import me.glaremasters.guilds.utils.StringUtils;
import net.byteflux.libby.BukkitLibraryManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public final class Guilds extends JavaPlugin {

    @Getter
    private static GuildsAPI api;
    private ACFHandler acfHandler;
    private GuildHandler guildHandler;
    private CooldownHandler cooldownHandler;
    private ArenaHandler arenaHandler;
    private ChallengeHandler challengeHandler;
    private static TaskChainFactory taskChainFactory;
    private DatabaseProvider database;
    private CooldownsProvider cooldownsProvider;
    private ChallengesProvider challengesProvider;
    private ArenasProvider arenasProvider;
    private SettingsHandler settingsHandler;
    private PaperCommandManager commandManager;
    private ActionHandler actionHandler;
    private GUIHandler guiHandler;
    private Economy economy;
    private Permission permissions;
    private List<String> loadedLanguages;
    private boolean successfulLoad = false;

    @Override
    public void onDisable() {
        if (checkVault()) {
            try {
                guildHandler.saveData();
                cooldownHandler.saveCooldowns();
                arenaHandler.saveArenas();
            } catch (IOException e) {
                e.printStackTrace();
            }
            guildHandler.chatLogout();
        }
    }

    @Override
    public void onLoad() {
        BukkitLibraryManager loader = new BukkitLibraryManager(this);
        Libraries libraries = new Libraries();
        loader.addRepository("https://repo.glaremasters.me/repository/public/");
        loader.addMavenCentral();
        try {
            libraries.loadDepLibs(loader);
            successfulLoad = true;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
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
     *
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
    private void logLogo(ConsoleCommandSender sender) {
        sender.sendMessage(ACFBukkitUtil.color("&a  ________ "));
        sender.sendMessage(ACFBukkitUtil.color("&a /  _____/ "));
        sender.sendMessage(ACFBukkitUtil.color("&a/   \\  ___ " + "  &3Guilds &8v" + getDescription().getVersion()));
        sender.sendMessage(ACFBukkitUtil.color("&a\\    \\_\\  \\" + "  &3Server Version: &8" + getServer().getVersion()));
        sender.sendMessage(ACFBukkitUtil.color("&a \\______  /"));
        sender.sendMessage(ACFBukkitUtil.color("&a        \\/ "));
    }

    @Override
    public void onEnable() {
        if (!successfulLoad) {
            warn("Dependencies could not be downloaded, shutting down to prevent file corruption.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        // Check if the server is running Vault
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            warn("It looks like you don't have Vault on your server! Stopping plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        loadedLanguages = new ArrayList<>();

        // This is really just for shits and giggles
        // A variable for checking how long startup took.
        long startingTime = System.currentTimeMillis();

        // Load up TaskChain
        taskChainFactory = BukkitTaskChainFactory.create(this);

        // Flex teh guild logLogo
        logLogo(Bukkit.getConsoleSender());

        // Load the config
        info("Loading config..");
        settingsHandler = new SettingsHandler(this);
        info("Loaded config!");

        saveData();

        // Load data here.
        try {
            info("Loading Data..");
            // This will soon be changed to an automatic storage chooser from the config
            // Load the json provider
            database = new JsonProvider(getDataFolder());
            // Load the cooldown folder
            cooldownsProvider = new CooldownsProvider(getDataFolder());
            // Load the cooldown objects
            cooldownHandler = new CooldownHandler(cooldownsProvider);
            // Load the arena folder
            arenasProvider = new ArenasProvider(getDataFolder());
            // Load the arena objects
            arenaHandler = new ArenaHandler(arenasProvider);
            // Load the challenge handler
            challengeHandler = new ChallengeHandler();
            // Load the challenge provider
            challengesProvider = new ChallengesProvider(getDataFolder());
            // Load guildhandler with provider
            guildHandler = new GuildHandler(database, getCommandManager(), getPermissions(), getConfig(), settingsHandler.getSettingsManager());
            info("Loaded data!");
        } catch (IOException e) {
            severe("An error occurred loading data! Stopping plugin..");
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
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPI(guildHandler).register();
        }

        info("Enabling Metrics..");
        // start bstats
        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SingleLineChart("guilds", () -> getGuildHandler().getGuildsSize()));
        info("Enabled Metrics!");

        // Initialize the action handler for actions in the plugin
        actionHandler = new ActionHandler();
        info("Loading Commands and Language Data..");
        // Load the ACF command manager
        commandManager = new PaperCommandManager(this);
        acfHandler = new ACFHandler(this, commandManager);

        guiHandler = new GUIHandler(this, settingsHandler.getSettingsManager(), guildHandler, getCommandManager(), cooldownHandler);

        if (settingsHandler.getSettingsManager().getProperty(PluginSettings.ANNOUNCEMENTS_CONSOLE)) {
            newChain().async(() -> {
                try {
                    info(StringUtils.getAnnouncements(this));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).execute();
        }

        UpdateChecker.runCheck(this, settingsHandler.getSettingsManager());

        // Load all the listeners
        Stream.of(
                new EntityListener(guildHandler, settingsHandler.getSettingsManager(), challengeHandler),
                new PlayerListener(guildHandler, settingsHandler.getSettingsManager(), this, permissions),
                new TicketListener(this, guildHandler, settingsHandler.getSettingsManager()),
                new VaultBlacklistListener(this, guildHandler, settingsHandler.getSettingsManager()),
                new ArenaListener(this, challengeHandler, challengesProvider, settingsHandler.getSettingsManager()))
                .forEach(l -> Bukkit.getPluginManager().registerEvents(l, this));
        // Load the optional listeners
        optionalListeners();

        info("Enabling the Guilds API..");
        // Initialize the API (probably be placed in different spot?)
        api = new GuildsAPI(getGuildHandler());
        info("Enabled API!");

        info("Ready to go! That only took " + (System.currentTimeMillis() - startingTime) + "ms");
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            try {
                guildHandler.saveData();
                cooldownHandler.saveCooldowns();
                arenaHandler.saveArenas();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 20 * 60, (20 * 60) * settingsHandler.getSettingsManager().getProperty(PluginSettings.SAVE_INTERVAL));

    }

    /**
     * Check if Vault is running
     *
     * @return true or false
     */
    private boolean checkVault() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault");
    }

    //todo what about a hook package with a hook manager for these 3 listeners and PlaceholderAPI?

    /**
     * Register optional listeners based off values in the config
     */
    private void optionalListeners() {
        if (settingsHandler.getSettingsManager().getProperty(HooksSettings.ESSENTIALS)) {
            getServer().getPluginManager().registerEvents(new EssentialsChatListener(guildHandler), this);
        }

        if (settingsHandler.getSettingsManager().getProperty(HooksSettings.WORLDGUARD)) {
            getServer().getPluginManager().registerEvents(new WorldGuardListener(guildHandler), this);
            getServer().getPluginManager().registerEvents(new ClaimSignListener(this, settingsHandler.getSettingsManager(), guildHandler), this);
        }
    }

    /**
     * Used to create a new chain of commands
     * @param <T> the type
     * @return chain
     */
    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    /**
     * Used to create new shared chain of commands
     * @param name the name of the chain
     * @param <T> the type of chain
     * @return shared chain
     */
    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }

}
