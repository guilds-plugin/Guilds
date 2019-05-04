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
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import lombok.Getter;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.commands.CommandChat;
import me.glaremasters.guilds.commands.CommandHelp;
import me.glaremasters.guilds.commands.CommandRequest;
import me.glaremasters.guilds.commands.actions.CommandCancel;
import me.glaremasters.guilds.commands.actions.CommandConfirm;
import me.glaremasters.guilds.commands.admin.CommandAdminAddPlayer;
import me.glaremasters.guilds.commands.admin.CommandAdminGive;
import me.glaremasters.guilds.commands.admin.CommandAdminPrefix;
import me.glaremasters.guilds.commands.admin.CommandAdminRemove;
import me.glaremasters.guilds.commands.admin.CommandAdminRemovePlayer;
import me.glaremasters.guilds.commands.admin.CommandAdminRename;
import me.glaremasters.guilds.commands.admin.CommandAdminSpy;
import me.glaremasters.guilds.commands.admin.CommandAdminStatus;
import me.glaremasters.guilds.commands.admin.CommandAdminUpdateLanguages;
import me.glaremasters.guilds.commands.admin.CommandAdminUpgrade;
import me.glaremasters.guilds.commands.admin.CommandAdminVault;
import me.glaremasters.guilds.commands.admin.CommandReload;
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
import me.glaremasters.guilds.commands.gui.CommandBuff;
import me.glaremasters.guilds.commands.gui.CommandList;
import me.glaremasters.guilds.commands.gui.CommandVault;
import me.glaremasters.guilds.commands.homes.CommandDelHome;
import me.glaremasters.guilds.commands.homes.CommandHome;
import me.glaremasters.guilds.commands.homes.CommandSetHome;
import me.glaremasters.guilds.commands.management.CommandCreate;
import me.glaremasters.guilds.commands.management.CommandDelete;
import me.glaremasters.guilds.commands.management.CommandKick;
import me.glaremasters.guilds.commands.management.CommandPrefix;
import me.glaremasters.guilds.commands.management.CommandRename;
import me.glaremasters.guilds.commands.management.CommandStatus;
import me.glaremasters.guilds.commands.management.CommandTransfer;
import me.glaremasters.guilds.commands.management.CommandUpgrade;
import me.glaremasters.guilds.commands.member.CommandAccept;
import me.glaremasters.guilds.commands.member.CommandCheck;
import me.glaremasters.guilds.commands.member.CommandDecline;
import me.glaremasters.guilds.commands.member.CommandDemote;
import me.glaremasters.guilds.commands.member.CommandInvite;
import me.glaremasters.guilds.commands.member.CommandLanguage;
import me.glaremasters.guilds.commands.member.CommandLeave;
import me.glaremasters.guilds.commands.member.CommandPromote;
import me.glaremasters.guilds.configuration.GuildConfigurationBuilder;
import me.glaremasters.guilds.configuration.sections.HooksSettings;
import me.glaremasters.guilds.configuration.sections.PluginSettings;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.database.migration.MigrationManager;
import me.glaremasters.guilds.database.providers.JsonProvider;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildCode;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.guis.BuffGUI;
import me.glaremasters.guilds.guis.ListGUI;
import me.glaremasters.guilds.listeners.EntityListener;
import me.glaremasters.guilds.listeners.EssentialsChatListener;
import me.glaremasters.guilds.listeners.InventoryListener;
import me.glaremasters.guilds.listeners.PlayerListener;
import me.glaremasters.guilds.listeners.TicketListener;
import me.glaremasters.guilds.listeners.WorldGuardListener;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.placeholders.PlaceholderAPI;
import me.glaremasters.guilds.updater.UpdateChecker;
import me.glaremasters.guilds.utils.Constants;
import me.glaremasters.guilds.utils.StringUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

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

    @Getter
    private static GuildsAPI api;
    private GuildHandler guildHandler;
    private static TaskChainFactory taskChainFactory;
    private DatabaseProvider database;
    private SettingsManager settingsManager;
    private PaperCommandManager commandManager;
    private ActionHandler actionHandler;
    private Economy economy;
    private Permission permissions;
    @Getter
    private List<Guild> oldGuilds = new ArrayList<>();
    @Getter
    private BuffGUI buffGUI;
    @Getter
    private ListGUI listGUI;
    private List<String> loadedLanguages;

    @Override
    public void onDisable() {
        if (checkVault()) {
            try {
                guildHandler.saveData();
            } catch (IOException e) {
                e.printStackTrace();
            }
            guildHandler.chatLogout();
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
    public void loadLanguages(PaperCommandManager manager) {
        loadedLanguages.clear();
        try {
            File languageFolder = new File(getDataFolder(), "languages");
            for (File file : Objects.requireNonNull(languageFolder.listFiles())) {
                if (file.isFile()) {
                    if (file.getName().endsWith(".yml")) {
                        String updatedName = file.getName().replace(".yml", "");
                        loadedLanguages.add(updatedName);
                        manager.addSupportedLanguage(Locale.forLanguageTag(updatedName));
                        manager.getLocales().loadYamlLanguageFile(new File(languageFolder, file.getName()), Locale.forLanguageTag(updatedName));
                    }
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
        try {
            info("Checking for old Guild Data....");
            MigrationManager manager = new MigrationManager(this);
            manager.checkOld(oldGuilds);
        } catch (IOException e) {
            e.printStackTrace();
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
            database = new JsonProvider(getDataFolder(), this);
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

        Stream.of(
                // Action Commands
                new CommandCancel(actionHandler),
                new CommandConfirm(actionHandler),
                // Admin Commands
                new CommandAdminAddPlayer(guildHandler),
                new CommandAdminGive(guildHandler, settingsManager),
                new CommandAdminPrefix(guildHandler),
                new CommandAdminRemove(guildHandler, actionHandler),
                new CommandAdminRemovePlayer(guildHandler),
                new CommandAdminRename(guildHandler),
                new CommandAdminSpy(guildHandler),
                new CommandAdminUpdateLanguages(actionHandler, settingsManager, this),
                new CommandAdminStatus(guildHandler),
                new CommandAdminUpgrade(guildHandler),
                new CommandAdminVault(guildHandler),
                new CommandReload(settingsManager, this),
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
                new CommandCodeList(settingsManager, guildHandler),
                new CommandCodeRedeem(guildHandler),
                // GUI Commands
                new CommandList(this, guildHandler),
                new CommandBuff(this),
                new CommandVault(guildHandler, settingsManager),
                // Home Commands
                new CommandDelHome(),
                new CommandHome(),
                new CommandSetHome(economy, settingsManager),
                // Management Commands
                new CommandCreate(this, guildHandler, settingsManager, actionHandler, economy, permissions),
                new CommandDelete(guildHandler, actionHandler, permissions),
                new CommandKick(guildHandler, permissions),
                new CommandPrefix(guildHandler, settingsManager),
                new CommandRename(guildHandler, settingsManager),
                new CommandStatus(),
                new CommandTransfer(),
                new CommandUpgrade(guildHandler, actionHandler, settingsManager, permissions),
                // Member Commands
                new CommandAccept(guildHandler, permissions),
                new CommandCheck(guildHandler),
                new CommandDecline(guildHandler),
                new CommandDemote(guildHandler),
                new CommandInvite(guildHandler),
                new CommandLanguage(this),
                new CommandLeave(guildHandler, actionHandler, permissions),
                new CommandPromote(guildHandler),
                // Misc Commands
                new CommandChat(guildHandler),
                new CommandHelp(),
                new CommandRequest(guildHandler)
        ).forEach(commandManager::registerCommand);

        if (settingsManager.getProperty(HooksSettings.WORLDGUARD)) {
            // Claim Commands
            commandManager.registerCommand(new CommandClaim(WorldGuardWrapper.getInstance(), settingsManager));
            commandManager.registerCommand(new CommandUnclaim(WorldGuardWrapper.getInstance(), settingsManager));
        }

        buffGUI = new BuffGUI(this, settingsManager, guildHandler, getCommandManager());
        listGUI = new ListGUI(this, settingsManager, guildHandler);


        if (settingsManager.getProperty(PluginSettings.ANNOUNCEMENTS_CONSOLE)) {
            newChain().async(() -> {
                try {
                    info(getAnnouncements());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).execute();
        }

        if (settingsManager.getProperty(PluginSettings.UPDATE_CHECK)) {
            UpdateChecker.init(this, 66176).requestUpdateCheck().whenComplete((result, exception) -> {
                if (result.requiresUpdate()) {
                    this.getLogger().info(String.format("An update is available! Guilds %s may be downloaded on SpigotMC", result.getNewestVersion()));
                    return;
                }

                UpdateChecker.UpdateReason reason = result.getReason();
                if (reason == UpdateChecker.UpdateReason.UP_TO_DATE) {
                    this.getLogger().info(String.format("Your version of Guilds (%s) is up to date!", result.getNewestVersion()));
                } else if (reason == UpdateChecker.UpdateReason.UNRELEASED_VERSION) {
                    this.getLogger().info(String.format("Your version of Guilds (%s) is more recent than the one publicly available. Are you on a development build?", result.getNewestVersion()));
                } else {
                    this.getLogger().warning("Could not check for a new version of Guilds. Reason: " + reason);
                }
            });
        }

        // Load all the listeners
        Stream.of(new EntityListener(guildHandler, settingsManager), new PlayerListener(guildHandler, settingsManager, this, commandManager), new TicketListener(this, guildHandler, settingsManager), new InventoryListener(guildHandler, settingsManager)).forEach(l -> Bukkit.getPluginManager().registerEvents(l, this));
        // Load the optional listeners
        optionalListeners();

        info("Enabling the Guilds API..");
        // Initialize the API (probably be placed in different spot?)
        api = new GuildsAPI(getGuildHandler());
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

        manager.getCommandCompletions().registerCompletion("languages", c -> loadedLanguages.stream().sorted().collect(Collectors.toList()));
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
            String result = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
            announcement = StringUtils.convert_html(result);
            con.disconnect();
        } catch (Exception ex) {
            announcement = "Could not fetch announcements!";
        }
        return announcement;
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
