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

import co.aikar.commands.PaperCommandManager;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.glaremasters.guilds.acf.ACFHandler;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.arena.ArenaHandler;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.conf.GuildBuffSettings;
import me.glaremasters.guilds.configuration.SettingsHandler;
import me.glaremasters.guilds.configuration.sections.HooksSettings;
import me.glaremasters.guilds.configuration.sections.PluginSettings;
import me.glaremasters.guilds.configuration.sections.StorageSettings;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.database.DatabaseAdapter;
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
import me.glaremasters.guilds.utils.LanguageUpdater;
import me.glaremasters.guilds.utils.LoggingUtils;
import me.glaremasters.guilds.utils.StringUtils;
import net.byteflux.libby.BukkitLibraryManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.stream.Stream;

public final class Guilds extends JavaPlugin {

    private static GuildsAPI api;
    private static Gson gson;
    private ACFHandler acfHandler;
    private GuildHandler guildHandler;
    private CooldownHandler cooldownHandler;
    private ArenaHandler arenaHandler;
    private ChallengeHandler challengeHandler;
    private static TaskChainFactory taskChainFactory;
    private DatabaseAdapter database;
    private SettingsHandler settingsHandler;
    private PaperCommandManager commandManager;
    private ActionHandler actionHandler;
    private GUIHandler guiHandler;
    private Economy economy;
    private Permission permissions;

    public static Gson getGson() {
        return gson;
    }

    public static GuildsAPI getApi() {
        return Guilds.api;
    }

    @Override
    public void onDisable() {
        if (checkVault() && economy != null) {
            try {
                guildHandler.saveData();
                cooldownHandler.saveCooldowns();
                arenaHandler.saveArenas();
            } catch (IOException e) {
                e.printStackTrace();
            }
            guildHandler.chatLogout();
            commandManager.unregisterCommands();
        }

        LoggingUtils.info("Shutting down database...");
        database.close();
        LoggingUtils.info("Database has been shut down.");
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

    @Override
    public void onEnable() {

        LoggingUtils.logLogo(Bukkit.getConsoleSender(), this);

        // Check if the server is running Vault
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            LoggingUtils.warn("It looks like you don't have Vault on your server! Stopping plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        gson = new GsonBuilder().setPrettyPrinting().create();

        setupEconomy();
        setupPermissions();

        if (economy == null) {
            LoggingUtils.warn("It looks like you don't have an Economy plugin on your server! Stopping plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        LoggingUtils.info("Economy Found: " + economy.getName());
        LoggingUtils.info("Permissions Found: " + permissions.getName());

        if (permissions.getName().equals("GroupManager")) {
            LoggingUtils.warn("GroupManager is not designed for newer MC versions. Expect issues with permissions.");
        }
        if (permissions.getName().equals("PermissionsEx")) {
            LoggingUtils.warn("PermissionsEx is not designed to run permission async. Expect issues with permissions.");
        }

        // This is really just for shits and giggles
        // A variable for checking how long startup took.
        long startingTime = System.currentTimeMillis();

        // Load up TaskChain
        taskChainFactory = BukkitTaskChainFactory.create(this);

        settingsHandler = new SettingsHandler(this);

        downloadOptionalDependencies();

        new LanguageUpdater(this).saveLang();

        // Load data here.
        try {
            setDatabase(new DatabaseAdapter(this, settingsHandler.getMainConf()));
            if (!database.isConnected()) {
                // Jump down to the catch
                throw new IOException("Failed to connect to Database.");
            }
            // Load the cooldown objects
            cooldownHandler = new CooldownHandler(this);
            // Load the arena objects
            arenaHandler = new ArenaHandler(this);
            arenaHandler.loadArenas();
            // Load the challenge handler
            challengeHandler = new ChallengeHandler(this);
            // Load guildhandler with provider
            guildHandler = new GuildHandler(this, settingsHandler.getMainConf());
        } catch (IOException e) {
            LoggingUtils.severe("An error occurred loading data! Stopping plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // If they have placeholderapi, enable it.
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
           new PlaceholderAPI(guildHandler).register();
        }
        // start bstats
        Metrics metrics = new Metrics(this, 881);
        metrics.addCustomChart(new Metrics.SingleLineChart("guilds", () -> getGuildHandler().getGuildsSize()));
        metrics.addCustomChart(new Metrics.SingleLineChart("tiers", () -> getGuildHandler().getTiers().size()));
        metrics.addCustomChart(new Metrics.SingleLineChart("roles", () -> getGuildHandler().getRoles().size()));
        metrics.addCustomChart(new Metrics.SingleLineChart("buffs", () -> settingsHandler.getBuffConf().getProperty(GuildBuffSettings.BUFFS).size()));
        metrics.addCustomChart(new Metrics.SimplePie("language", () -> settingsHandler.getMainConf().getProperty(PluginSettings.MESSAGES_LANGUAGE)));

        // Initialize the action handler for actions in the plugin
        actionHandler = new ActionHandler();
        // Load the ACF command manager
        commandManager = new PaperCommandManager(this);
        acfHandler = new ACFHandler(this, commandManager);
        acfHandler.load();

        guiHandler = new GUIHandler(this, settingsHandler.getMainConf(), guildHandler, getCommandManager(), cooldownHandler);

        if (settingsHandler.getMainConf().getProperty(PluginSettings.ANNOUNCEMENTS_CONSOLE)) {
            newChain().async(() -> {
                try {
                    LoggingUtils.info(StringUtils.getAnnouncements(this));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).execute();
        }

        UpdateChecker.runCheck(this, settingsHandler.getMainConf());

        // Load all the listeners
        Stream.of(
                new EntityListener(guildHandler, settingsHandler.getMainConf(), challengeHandler),
                new PlayerListener(this, settingsHandler.getMainConf(), guildHandler, permissions),
                new TicketListener(this, guildHandler, settingsHandler.getMainConf()),
                new VaultBlacklistListener(this, guildHandler, settingsHandler.getMainConf()),
                new ArenaListener(this, challengeHandler, settingsHandler.getMainConf()))
                .forEach(l -> Bukkit.getPluginManager().registerEvents(l, this));
        // Load the optional listeners
        optionalListeners();

        api = new GuildsAPI(guildHandler, cooldownHandler);

        LoggingUtils.info("Ready to go! That only took " + (System.currentTimeMillis() - startingTime) + "ms");
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            try {
                if (guildHandler.isMigrating()) {
                    return;
                }
                guildHandler.saveData();
                //cooldownHandler.saveCooldowns(); We are going to save on shutdown only, no need for runtime saving
                arenaHandler.saveArenas();
                challengeHandler.saveData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 20 * 60, (20 * 60) * settingsHandler.getMainConf().getProperty(StorageSettings.SAVE_INTERVAL));

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
        if (settingsHandler.getMainConf().getProperty(HooksSettings.ESSENTIALS)) {
            getServer().getPluginManager().registerEvents(new EssentialsChatListener(guildHandler), this);
        }

        if (settingsHandler.getMainConf().getProperty(HooksSettings.WORLDGUARD)) {
            getServer().getPluginManager().registerEvents(new WorldGuardListener(guildHandler), this);
            getServer().getPluginManager().registerEvents(new ClaimSignListener(this, settingsHandler.getMainConf(), guildHandler), this);
        }
    }

    private void downloadOptionalDependencies() {
        BukkitLibraryManager loader = new BukkitLibraryManager(this);
        Libraries libraries = new Libraries();
        loader.addMavenCentral();
        loader.addRepository("https://repo.glaremasters.me/repository/public/");
        if (!settingsHandler.getMainConf().getProperty(StorageSettings.STORAGE_TYPE).toLowerCase().equals("json")) {
            try {
                libraries.loadSQL(loader);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Used to create a new chain of commands
     *
     * @param <T> the type
     * @return chain
     */
    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    /**
     * Used to create new shared chain of commands
     *
     * @param name the name of the chain
     * @param <T>  the type of chain
     * @return shared chain
     */
    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }

    public ACFHandler getAcfHandler() {
        return this.acfHandler;
    }

    public GuildHandler getGuildHandler() {
        return this.guildHandler;
    }

    public CooldownHandler getCooldownHandler() {
        return this.cooldownHandler;
    }

    public ArenaHandler getArenaHandler() {
        return this.arenaHandler;
    }

    public ChallengeHandler getChallengeHandler() {
        return this.challengeHandler;
    }

    public DatabaseAdapter getDatabase() {
        return this.database;
    }

    public void setDatabase(DatabaseAdapter database) {
        this.database = database;
    }

    public SettingsHandler getSettingsHandler() {
        return this.settingsHandler;
    }

    public PaperCommandManager getCommandManager() {
        return this.commandManager;
    }

    public ActionHandler getActionHandler() {
        return this.actionHandler;
    }

    public GUIHandler getGuiHandler() {
        return this.guiHandler;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public Permission getPermissions() {
        return this.permissions;
    }
}
