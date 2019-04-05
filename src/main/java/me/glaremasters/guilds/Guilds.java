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
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.commands.CommandGuilds;
import me.glaremasters.guilds.commands.admin.CommandAdminAddPlayer;
import me.glaremasters.guilds.commands.admin.CommandAdminPrefix;
import me.glaremasters.guilds.commands.admin.CommandAdminRemove;
import me.glaremasters.guilds.commands.admin.CommandAdminRemovePlayer;
import me.glaremasters.guilds.commands.admin.CommandAdminRename;
import me.glaremasters.guilds.commands.admin.CommandAdminSpy;
import me.glaremasters.guilds.commands.admin.CommandAdminStatus;
import me.glaremasters.guilds.commands.admin.CommandAdminUpgrade;
import me.glaremasters.guilds.commands.admin.CommandAdminVault;
import me.glaremasters.guilds.commands.ally.CommandAllyAccept;
import me.glaremasters.guilds.commands.ally.CommandAllyAdd;
import me.glaremasters.guilds.commands.ally.CommandAllyDecline;
import me.glaremasters.guilds.commands.ally.CommandAllyList;
import me.glaremasters.guilds.commands.ally.CommandAllyRemove;
import me.glaremasters.guilds.commands.bank.CommandBankBalance;
import me.glaremasters.guilds.commands.bank.CommandBankDeposit;
import me.glaremasters.guilds.commands.bank.CommandBankWithdraw;
import me.glaremasters.guilds.commands.claims.CommandClaim;
import me.glaremasters.guilds.commands.claims.CommandUnclaim;
import me.glaremasters.guilds.commands.codes.CommandCodeCreate;
import me.glaremasters.guilds.commands.codes.CommandCodeDelete;
import me.glaremasters.guilds.commands.codes.CommandCodeInfo;
import me.glaremasters.guilds.commands.codes.CommandCodeList;
import me.glaremasters.guilds.commands.codes.CommandCodeRedeem;
import me.glaremasters.guilds.commands.homes.CommandDelHome;
import me.glaremasters.guilds.commands.homes.CommandHome;
import me.glaremasters.guilds.commands.homes.CommandSetHome;
import me.glaremasters.guilds.configuration.GuildConfigurationBuilder;
import me.glaremasters.guilds.configuration.sections.HooksSettings;
import me.glaremasters.guilds.configuration.sections.PluginSettings;
import me.glaremasters.guilds.contexts.GuildTarget;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.database.providers.JsonProvider;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildCode;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.listeners.EntityListener;
import me.glaremasters.guilds.listeners.EssentialsChatListener;
import me.glaremasters.guilds.listeners.InventoryListener;
import me.glaremasters.guilds.listeners.PlayerListener;
import me.glaremasters.guilds.listeners.TicketListener;
import me.glaremasters.guilds.listeners.WorldGuardListener;
import me.glaremasters.guilds.utils.Constants;
import me.glaremasters.guilds.utils.StringUtils;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Getter
public final class Guilds extends JavaPlugin {

    //todo rewrite
    //order is very important here as stuff depends on each other.
    //use logical order.
    //incorrect order will result in a NPE

    @Getter
    private static GuildsAPI api;
    private GuildHandler guildHandler;
    private DatabaseProvider database;
    private SettingsManager settingsManager;
    private PaperCommandManager commandManager;
    private ActionHandler actionHandler;
    private Economy economy;
    private Permission permissions;
    private List<Player> spy;

    /**
     * Check if a guild has a claim
     *
     * @param world the world to check in
     * @param guild the guild to check
     */
    public static void checkForClaim(World world, Guild guild, Guilds guilds) {
        if (guilds.getConfig().getBoolean("main-hooks.worldguard-claims")) {
            WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();
            wrapper.getRegion(world, guild.getName()).ifPresent(region -> wrapper.removeRegion(world, guild.getName()));
        }
    }


    @Override
    public void onDisable() {
        if (checkVault()) {
            try {
                guildHandler.saveData();
            } catch (IOException e) {
                e.printStackTrace();
            }
            spy.clear();
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

    //todo rewrite

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
     * Load the languages for the server from ACF BCM
     *
     * @param manager ACF BCM
     */
    private void loadLanguages(PaperCommandManager manager) {
        try {
            File languageFolder = new File(getDataFolder(), "languages");
            for (File file : Objects.requireNonNull(languageFolder.listFiles())) {
                if (file.isFile()) {
                    String updatedName = file.getName().replace(".yml", "");
                    manager.addSupportedLanguage(Locale.forLanguageTag(updatedName));
                    manager.getLocales().loadYamlLanguageFile(new File(languageFolder, file.getName()), Locale.forLanguageTag(updatedName));
                }
            }
            manager.getLocales().setDefaultLocale(Locale.forLanguageTag(settingsManager.getProperty(PluginSettings.MESSAGES_LANGUAGE)));
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
        settingsManager = SettingsManagerBuilder
                .withYamlFile(new File(getDataFolder(), "config.yml"))
                .migrationService(new PlainMigrationService())
                .configurationData(GuildConfigurationBuilder.buildConfigurationData())
                .create();
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
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPI().register();
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
        commandManager.usePerIssuerLocale(true, false);
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
        Stream.of(// Admin Commands
                new CommandAdminAddPlayer(guildHandler),
                new CommandAdminPrefix(guildHandler),
                new CommandAdminRemove(guildHandler, actionHandler),
                new CommandAdminRemovePlayer(guildHandler),
                new CommandAdminRename(guildHandler),
                new CommandAdminSpy(guildHandler),
                new CommandAdminStatus(guildHandler),
                new CommandAdminUpgrade(guildHandler),
                new CommandAdminVault(guildHandler),
                // General For Now
                new CommandGuilds(this, guildHandler, settingsManager, actionHandler, economy),
                // Ally Commands
                new CommandAllyAccept(guildHandler),
                new CommandAllyAdd(guildHandler),
                new CommandAllyDecline(guildHandler),
                new CommandAllyList(),
                new CommandAllyRemove(guildHandler),
                // Bank Commands
                new CommandBankBalance(),
                new CommandBankDeposit(economy),
                new CommandBankWithdraw(economy),
                // Code Commands
                new CommandCodeCreate(settingsManager),
                new CommandCodeDelete(),
                new CommandCodeInfo(),
                new CommandCodeList(settingsManager),
                new CommandCodeRedeem(guildHandler),
                // Home Commands
                new CommandDelHome(),
                new CommandHome(),
                new CommandSetHome(economy, settingsManager)).forEach(commandManager::registerCommand);

        if (settingsManager.getProperty(HooksSettings.WORLDGUARD)) {
            // Claim Commands
            commandManager.registerCommand(new CommandClaim(WorldGuardWrapper.getInstance(), settingsManager));
            commandManager.registerCommand(new CommandUnclaim(WorldGuardWrapper.getInstance(), settingsManager));
        }


        // This can probably be moved into it's own method
        // This checks for updates
        if (settingsManager.getProperty(PluginSettings.ANNOUNCEMENTS_CONSOLE)) {
            info("Checking for updates..");
            getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
                /* SpigotUpdater updater = new SpigotUpdater(this, 48920);*/

                @Override
                public void run() {
                    /*updateCheck(updater);*/
                    try {
                        info(getAnnouncements());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });


        }

        // Load all the listeners
        Stream.of(new EntityListener(guildHandler), new PlayerListener(guildHandler, settingsManager, this, commandManager), new TicketListener(this, guildHandler), new InventoryListener(guildHandler, settingsManager)).forEach(l -> Bukkit.getPluginManager().registerEvents(l, this));
        // Load the optional listeners
        optionalListeners();

        info("Enabling the Guilds API..");
        // Initialize the API (probably be placed in different spot?)
        api = new GuildsAPI(getGuildHandler());
        // Create a new list for the spies
        spy = new ArrayList<>();
        info("Enabled API!");

        info("Ready to go! That only took " + (System.currentTimeMillis() - startingTime) + "ms");
    }

    /**
     * Load the contexts for the server from ACF BCM
     *
     * @param manager ACF BCM
     */
    private void loadContexts(PaperCommandManager manager) {
        manager.getCommandContexts().registerIssuerOnlyContext(Guild.class, c -> {
            Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null)  throw new InvalidCommandArgument(Messages.ERROR__NO_GUILD);
            return guild;
        });

        manager.getCommandContexts().registerIssuerOnlyContext(GuildRole.class, c -> {
            Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null) return null;
            return getGuildHandler().getGuildRole(guild.getMember(c.getPlayer().getUniqueId()).getRole().getLevel());
        });

        manager.getCommandContexts().registerContext(GuildTarget.class, c-> {
            Guild guild = guildHandler.getGuild(c.popFirstArg());
            if (guild == null) throw new InvalidCommandArgument(Messages.ERROR__NO_GUILD);
            return new GuildTarget(guild);
        });
    }

    private void loadCompletions(PaperCommandManager manager) {

        manager.getCommandCompletions().registerCompletion("members", c -> {
            Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null) return null;
            return guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUuid()).getName()).collect(Collectors.toList());
        });

        manager.getCommandCompletions().registerCompletion("online", c -> Bukkit.getOnlinePlayers().stream().map(member -> Bukkit.getPlayer(member.getUniqueId()).getName()).collect(Collectors.toList()));

        manager.getCommandCompletions().registerCompletion("invitedTo", c -> guildHandler.getInvitedGuilds(c.getPlayer().getUniqueId()));

        manager.getCommandCompletions().registerCompletion("guilds", c -> guildHandler.getGuildNames());

        manager.getCommandCompletions().registerAsyncCompletion("allyInvites", c -> {
           Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null) return null;
           if (!guild.hasAllies()) return null;
           return guild.getPendingAllies().stream().map(g -> guildHandler.getNameById(g)).collect(Collectors.toList());
        });

        manager.getCommandCompletions().registerAsyncCompletion("allies", c -> {
            Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null) return null;
            if (!guild.hasAllies()) return null;
            return guild.getAllies().stream().map(g -> guildHandler.getNameById(g)).collect(Collectors.toList());
        });

        manager.getCommandCompletions().registerAsyncCompletion("activeCodes", c -> {
            Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null) return null;
            if (guild.getCodes() == null) return null;
            return guild.getCodes().stream().map(GuildCode::getId).collect(Collectors.toList());
        });

        manager.getCommandCompletions().registerAsyncCompletion("vaultAmount", c -> {
            Guild guild = guildHandler.getGuild(c.getPlayer());
            if (guild == null) return null;
            if (guild.getVaults() == null) return null;
            List<Inventory> list = guildHandler.getCachedVaults().get(guild);
            if (list == null) return null;
            return IntStream.rangeClosed(1, list.size()).mapToObj(Objects::toString).collect(Collectors.toList());
        });
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
        if (settingsManager.getProperty(HooksSettings.ESSENTIALS)) {
            getServer().getPluginManager().registerEvents(new EssentialsChatListener(guildHandler), this);
        }

        if (settingsManager.getProperty(HooksSettings.WORLDGUARD)) {
            getServer().getPluginManager().registerEvents(new WorldGuardListener(guildHandler), this);
        }
    }

    /**
     * Get the announcements for the plugin
     * @return announcements
     * @throws IOException
     */
    public String getAnnouncements() throws IOException {
        String announcement;
        URL url = new URL("https://glaremasters.me/guilds/announcements/?id=" + getDescription().getVersion());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", Constants.USER_AGENT);
        try (InputStream in = con.getInputStream()) {
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            announcement = StringUtils.convert_html(IOUtils.toString(in, encoding));
            con.disconnect();
        } catch (Exception ex) {
            announcement = "Could not fetch announcements!";
        }
        return announcement;
    }




}
