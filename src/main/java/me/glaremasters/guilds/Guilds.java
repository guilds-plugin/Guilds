/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
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
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guis.GUIHandler;
import me.glaremasters.guilds.listeners.ArenaListener;
import me.glaremasters.guilds.listeners.ChatListener;
import me.glaremasters.guilds.listeners.ClaimSignListener;
import me.glaremasters.guilds.listeners.EntityListener;
import me.glaremasters.guilds.listeners.EssentialsChatListener;
import me.glaremasters.guilds.listeners.PlayerListener;
import me.glaremasters.guilds.listeners.TicketListener;
import me.glaremasters.guilds.listeners.VaultBlacklistListener;
import me.glaremasters.guilds.listeners.WorldGuardListener;
import me.glaremasters.guilds.persistence.PersistenceCoordinator;
import me.glaremasters.guilds.persistence.PersistenceGate;
import me.glaremasters.guilds.placeholders.PlaceholderAPI;
import me.glaremasters.guilds.updater.UpdateChecker;
import me.glaremasters.guilds.utils.LanguageUpdater;
import me.glaremasters.guilds.utils.LoggingUtils;
import me.glaremasters.guilds.utils.StringUtils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.bxteam.quark.bukkit.BukkitLibraryManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.stream.Stream;

public final class Guilds extends JavaPlugin {

    /** Ticks in one Minecraft minute, as used by the autosave period. */
    private static final long TICKS_PER_MINUTE = 20L * 60L;

    /** Delay before the first autosave runs, in ticks (one minute). */
    private static final long SAVE_TASK_INITIAL_DELAY_TICKS = TICKS_PER_MINUTE;

    /** Smallest autosave period the scheduler will accept, in whole minutes. */
    private static final int MINIMUM_SAVE_INTERVAL_MINUTES = 1;

    /**
     * How long a capture may spend on the main thread per tick, in milliseconds.
     *
     * <p>A tick is 50ms and the server budget for one tick is well under that, so this is deliberately a
     * small fraction of it. Measured worst case for a fully stocked vault is about 175us, so the budget
     * is spent on roughly a dozen guilds per tick before it is checked, which keeps the overshoot per tick
     * to a single guild.
     */
    private static final long CAPTURE_BUDGET_MILLIS = 3L;

    /**
     * A save routine that is allowed to throw {@link IOException}.
     *
     * <p>{@link java.util.function.Consumer} cannot express a checked exception, so the save
     * paths need their own functional interface to be passed around as a value.
     */
    @FunctionalInterface
    private interface SaveRoutine {
        void run() throws IOException;
    }

    private static GuildsAPI api;
    private static Gson gson;
    private ACFHandler acfHandler;
    private GuildHandler guildHandler;
    private CooldownHandler cooldownHandler;
    private ArenaHandler arenaHandler;
    private ChallengeHandler challengeHandler;
    private static TaskChainFactory taskChainFactory;

    /**
     * Volatile because migration replaces it from a worker thread.
     *
     * <p>A reader on the autosave thread or the main thread was otherwise permitted to keep observing
     * the pre-migration adapter, and would then write to a pool that migration had already closed.
     */
    private volatile DatabaseAdapter database;

    /**
     * The gate every writer shares. Replaced on each enable rather than being a field initialiser, because
     * the gate latches {@code shuttingDown} for the life of the plugin and a `/reload` re-enables this
     * same instance. Reusing it would mean every save silently skipped forever after the first reload.
     */
    private PersistenceGate persistenceGate = new PersistenceGate();
    private PersistenceCoordinator persistenceCoordinator;
    private BukkitTask autosaveTask;
    private SettingsHandler settingsHandler;
    private PaperCommandManager commandManager;
    private ActionHandler actionHandler;
    private GUIHandler guiHandler;
    private Economy economy;
    private Permission permissions;
    private BukkitAudiences adventure;
    private ChatListener chatListener;
    private BukkitLibraryManager libraryManager;

    public static Gson getGson() {
        return gson;
    }

    public static GuildsAPI getApi() {
        return Guilds.api;
    }

    @Override
    public void onLoad() {
        libraryManager = new BukkitLibraryManager(this);
        libraryManager.loadFromGradle();
    }

    @Override
    public void onDisable() {
        /*
         * Persist first, and unconditionally. This sat behind a `checkVault() && economy != null`
         * guard, so a server that lost its Vault economy provider saved nothing at all.
         *
         * The coordinator drains any in-flight save before writing and closes the database afterwards,
         * so the final flush cannot interleave with a running autosave or lose the race to close the
         * connection pool. Bukkit has already cancelled the autosave schedule by this point, but that
         * only stops future runs; a worker that is already going keeps going.
         */
        savePluginData();

        /*
         * Teardown, one step at a time.
         *
         * Every step below can be missing after a partial onEnable, and each one is wrapped so a
         * failure is logged rather than thrown. A step that threw here would skip every step after
         * it, including closing the database, and would bury the original startup error underneath
         * a shutdown stack trace. See #777.
         *
         * The chat listener is the awkward one: it is created near the end of onEnable, so a guild
         * handler can exist while it does not, and GuildHandler#chatLogout() reads through it.
         */
        runCleanup(chatListener == null ? null : () -> guildHandler.chatLogout(), "guild chat");
        runCleanup(guildHandler == null ? null : () -> guildHandler.getLookupCache().clear(), "the guild lookup cache");
        runCleanup(commandManager == null ? null : commandManager::unregisterCommands, "commands");

        if (adventure != null) {
            runCleanup(() -> adventure.close(), "adventure audiences");
            adventure = null;
        }
    }

    /**
     * Flushes every data handler to its storage backend and then closes it.
     *
     * <p>Drain, write, close, in that order. Each handler is written independently inside the
     * coordinator, so one bad record does not cost the server owner every other kind of data.
     */
    private void savePluginData() {
        final DatabaseAdapter toClose = this.database;

        // Cancel the autosave before flushing. Bukkit does cancel a plugin's tasks when it is disabled, but
        // it does so after `onDisable` returns, so without this the timer is still nominally live while the
        // flush runs. It cannot actually fire, because the main thread is in here, but relying on that is
        // an assumption about CraftScheduler internals rather than something this code establishes.
        if (autosaveTask != null) {
            getServer().getScheduler().cancelTask(autosaveTask.getTaskId());
            autosaveTask = null;
        }

        if (persistenceCoordinator == null) {
            /*
             * A partial onEnable can reach here without a coordinator, which means one of the loaders
             * threw and the handlers may be only partly populated.
             *
             * Only the two collections whose writes cannot destroy anything are saved here. Guilds are
             * skipped because there is no snapshot path to reach them, and arenas are deliberately skipped
             * because `ArenaAdapter` deletes every stored arena missing from the collection it is given:
             * writing a half-loaded arena map would delete every arena whose load had failed. Losing a
             * shutdown save is recoverable; deleting live arenas is not.
             */
            LoggingUtils.warn("Startup did not complete, so guild and arena data was not saved on shutdown."
                    + " Whatever is in storage from the last successful save is what will be loaded.");
            runCleanup(cooldownHandler == null ? null : cooldownHandler::saveCooldowns, "cooldown data");
            runCleanup(challengeHandler == null ? null : challengeHandler::saveData, "challenge data");
            closeDatabase(toClose);
            return;
        }

        persistenceCoordinator.shutdownFlush(() -> closeDatabase(toClose));

        // Drop the reference so a second onDisable, which a failed re-enable can produce, does not flush
        // the previous session's coordinator through an adapter that is already closed.
        persistenceCoordinator = null;
    }

    /**
     * Closes the storage backend, if there is one.
     *
     * @param adapter the adapter to close
     */
    private void closeDatabase(@Nullable DatabaseAdapter adapter) {
        if (adapter == null) {
            return;
        }

        LoggingUtils.info("Shutting down database...");
        if (runCleanup(adapter::close, "the database")) {
            LoggingUtils.info("Database has been shut down.");
        }
    }

    /**
     * Runs one shutdown step, logging (never rethrowing) whatever it throws.
     *
     * @param step  the routine, or null when the object it needs was never created
     * @param label what is being saved or cleaned up, used in log messages
     * @return true when the step ran without throwing
     */
    private boolean runCleanup(@Nullable SaveRoutine step, String label) {
        if (step == null) {
            return false;
        }

        try {
            step.run();
            return true;
        } catch (IOException | RuntimeException e) {
            LoggingUtils.severe("An error occurred while saving or cleaning up " + label + ".", e);
            return false;
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

        this.adventure = BukkitAudiences.create(this);

        setupEconomy();
        setupPermissions();

        if (economy == null) {
            LoggingUtils.warn("It looks like you don't have an Economy plugin on your server! Stopping plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (permissions == null) {
            LoggingUtils.warn("It looks like you don't have a Permissions plugin hooked into Vault on your server! Stopping plugin..");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        settingsHandler = new SettingsHandler(this);

        LoggingUtils.info("Economy Found: " + economy.getName());
        LoggingUtils.info("Permissions Found: " + permissions.getName());

        if (permissions.getName().equals("GroupManager") || permissions.getName().equals("PermissionsEx")) {
            LoggingUtils.warn(permissions.getName() + " is not designed to run permissions async. Expect some possible issues");
            settingsHandler.getMainConf().setProperty(PluginSettings.RUN_VAULT_ASYNC, false);
            settingsHandler.getMainConf().save();
        }

        // This is really just for shits and giggles
        // A variable for checking how long startup took.
        long startingTime = System.currentTimeMillis();

        // Load up TaskChain
        taskChainFactory = BukkitTaskChainFactory.create(this);

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
            cooldownHandler.loadCooldowns();
            // Load the arena objects
            arenaHandler = new ArenaHandler(this);
            arenaHandler.loadArenas();
            // Load the challenge handler
            challengeHandler = new ChallengeHandler(this);
            challengeHandler.loadChallenges();
            // Load guildhandler with provider
            guildHandler = new GuildHandler(this, settingsHandler.getMainConf());
        } catch (IOException e) {
            LoggingUtils.severe("An error occurred loading data! Stopping plugin..", e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // One coordinator and one gate for every writer: autosave, migration and shutdown.
        persistenceGate = new PersistenceGate();
        persistenceCoordinator = new PersistenceCoordinator(
                this, persistenceGate, guildHandler, arenaHandler, challengeHandler, cooldownHandler);

        // If they have placeholderapi, enable it.
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPI(guildHandler).register();
            guildHandler.setPapi(true);
        }
        // start bstats
        Metrics metrics = new Metrics(this, 881);
        metrics.addCustomChart(new SingleLineChart("guilds", () -> getGuildHandler().getGuildsSize()));
        metrics.addCustomChart(new SingleLineChart("tiers", () -> getGuildHandler().getTiers().size()));
        metrics.addCustomChart(new SingleLineChart("roles", () -> getGuildHandler().getRoles().size()));
        metrics.addCustomChart(new SingleLineChart("buffs", () -> settingsHandler.getBuffConf().getProperty(GuildBuffSettings.BUFFS).size()));
        metrics.addCustomChart(new SimplePie("language", () -> settingsHandler.getMainConf().getProperty(PluginSettings.MESSAGES_LANGUAGE)));

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
                    LoggingUtils.warn("Unable to fetch console announcements.", e);
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

        chatListener = new ChatListener(this);

        LoggingUtils.info("Ready to go! That only took " + (System.currentTimeMillis() - startingTime) + "ms");
        startAutosaveTask();
    }

    /**
     * Schedules the periodic save.
     *
     * <p>Two timers, and the split is the point.
     *
     * <p>The trigger is a synchronous timer. The whole save used to run under
     * {@code scheduleAsyncRepeatingTask}, which meant it both read mutable state off the main thread and
     * could overlap itself: that method re-arms its timer as soon as it dispatches a run, so a save that
     * outlasted the interval put two threads in the same body. A synchronous timer cannot re-enter,
     * because the main thread runs one tick at a time.
     *
     * <p>The timer runs every tick and the coordinator decides what to do with the tick. That is a
     * deliberate shape: the coordinator starts a new capture when the configured interval has elapsed, and
     * spends a tick's budget finishing one that is already running. Deciding when to start inside the
     * coordinator keeps the timer to a single task. An earlier version started a second per-tick timer
     * from the first, which needed the first one to notice that a capture was pending and left a task to
     * cancel.
     *
     * <p>Capturing a thousand guilds costs about 400ms and a tick is 50ms, so a capture cannot finish in
     * one tick without stalling the server. Each pass spends at most {@link #CAPTURE_BUDGET_MILLIS}
     * milliseconds and picks the rest up next tick.
     *
     * <p>The write, which is the expensive half in wall-clock terms on a real server, is still handed to a
     * worker and never blocks a tick.
     */
    private void startAutosaveTask() {
        final long intervalNanos = resolveSaveIntervalTicks() * 50_000_000L;

        autosaveTask = getServer().getScheduler().runTaskTimer(this, () -> persistenceCoordinator.tick(
                        CAPTURE_BUDGET_MILLIS * 1_000_000L,
                        intervalNanos,
                        write -> getServer().getScheduler().runTaskAsynchronously(this, write)
                ),
                SAVE_TASK_INITIAL_DELAY_TICKS, 1L);
    }

    /**
     * Resolves the configured autosave interval into a period Bukkit will accept.
     *
     * <p>{@code scheduleAsyncRepeatingTask} rejects a non-positive period, and {@code 0} is a
     * natural thing to write to mean "only on shutdown". A large interval also overflows the
     * {@code int} tick count. Both used to escape {@link #onEnable()} as a stack trace that never
     * named the config key.
     *
     * @return the autosave period in ticks, always positive
     */
    private long resolveSaveIntervalTicks() {
        final int configuredMinutes = settingsHandler.getMainConf().getProperty(StorageSettings.SAVE_INTERVAL);

        if (configuredMinutes < MINIMUM_SAVE_INTERVAL_MINUTES) {
            LoggingUtils.warn("storage.save-interval must be at least " + MINIMUM_SAVE_INTERVAL_MINUTES
                    + " (found " + configuredMinutes + "). Falling back to " + MINIMUM_SAVE_INTERVAL_MINUTES
                    + ". Data is still saved when the server stops.");
        }

        return Math.max((long) configuredMinutes, MINIMUM_SAVE_INTERVAL_MINUTES) * TICKS_PER_MINUTE;
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
            registerWorldGuardListeners();
        }
    }

    /**
     * Register WorldGuard-backed listeners only when the optional hook is available.
     */
    private void registerWorldGuardListeners() {
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            LoggingUtils.warn("WorldGuard hook is enabled in config, but WorldGuard is not installed or enabled. Skipping WorldGuard claim listeners.");
            return;
        }

        final WorldGuardWrapper wrapper;
        try {
            wrapper = WorldGuardWrapper.getInstance();
        } catch (RuntimeException | LinkageError e) {
            LoggingUtils.warn("WorldGuard hook is enabled, but WorldGuardWrapper could not initialize. Skipping WorldGuard claim listeners.", e);
            return;
        }

        if (wrapper == null) {
            LoggingUtils.warn("WorldGuard hook is enabled, but WorldGuardWrapper returned no instance. Skipping WorldGuard claim listeners.");
            return;
        }

        getServer().getPluginManager().registerEvents(new WorldGuardListener(guildHandler, wrapper), this);
        getServer().getPluginManager().registerEvents(new ClaimSignListener(this, settingsHandler.getMainConf(), guildHandler, wrapper), this);
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

    /**
     * The gate that serialises every write to storage.
     *
     * @return the shared persistence gate
     */
    public PersistenceGate getPersistenceGate() {
        return persistenceGate;
    }

    /**
     * The coordinator that captures state and writes it.
     *
     * @return the coordinator, or null if startup failed before it was built
     */
    @Nullable public PersistenceCoordinator getPersistenceCoordinator() {
        return persistenceCoordinator;
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

    public BukkitAudiences getAdventure() {
        return adventure;
    }

    public ChatListener getChatListener() {
        return chatListener;
    }
}
