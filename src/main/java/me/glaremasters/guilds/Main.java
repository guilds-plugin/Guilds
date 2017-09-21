package me.glaremasters.guilds;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.stream.Stream;
import me.glaremasters.guilds.api.Metrics;
import me.glaremasters.guilds.commands.CommandAccept;
import me.glaremasters.guilds.commands.CommandAdmin;
import me.glaremasters.guilds.commands.CommandAlly;
import me.glaremasters.guilds.commands.CommandBank;
import me.glaremasters.guilds.commands.CommandBoot;
import me.glaremasters.guilds.commands.CommandBuff;
import me.glaremasters.guilds.commands.CommandBugReport;
import me.glaremasters.guilds.commands.CommandCancel;
import me.glaremasters.guilds.commands.CommandChat;
import me.glaremasters.guilds.commands.CommandCheck;
import me.glaremasters.guilds.commands.CommandConfirm;
import me.glaremasters.guilds.commands.CommandCreate;
import me.glaremasters.guilds.commands.CommandDecline;
import me.glaremasters.guilds.commands.CommandDelete;
import me.glaremasters.guilds.commands.CommandDemote;
import me.glaremasters.guilds.commands.CommandGive;
import me.glaremasters.guilds.commands.CommandHelp;
import me.glaremasters.guilds.commands.CommandHome;
import me.glaremasters.guilds.commands.CommandInfo;
import me.glaremasters.guilds.commands.CommandInspect;
import me.glaremasters.guilds.commands.CommandInvite;
import me.glaremasters.guilds.commands.CommandLeave;
import me.glaremasters.guilds.commands.CommandList;
import me.glaremasters.guilds.commands.CommandPrefix;
import me.glaremasters.guilds.commands.CommandPromote;
import me.glaremasters.guilds.commands.CommandReload;
import me.glaremasters.guilds.commands.CommandSetHome;
import me.glaremasters.guilds.commands.CommandStatus;
import me.glaremasters.guilds.commands.CommandTransfer;
import me.glaremasters.guilds.commands.CommandUpdate;
import me.glaremasters.guilds.commands.CommandUpgrade;
import me.glaremasters.guilds.commands.CommandVault;
import me.glaremasters.guilds.commands.CommandVersion;
import me.glaremasters.guilds.commands.base.CommandHandler;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.database.databases.json.Json;
import me.glaremasters.guilds.database.databases.mysql.MySql;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.leaderboard.LeaderboardHandler;
import me.glaremasters.guilds.listeners.ChatListener;
import me.glaremasters.guilds.listeners.ClickListener;
import me.glaremasters.guilds.listeners.DamageMultiplierListener;
import me.glaremasters.guilds.listeners.GuildBuffListener;
import me.glaremasters.guilds.listeners.GuildChatListener;
import me.glaremasters.guilds.listeners.GuildVaultListener;
import me.glaremasters.guilds.listeners.JoinListener;
import me.glaremasters.guilds.listeners.MobDeathListener;
import me.glaremasters.guilds.listeners.NameTagListener;
import me.glaremasters.guilds.listeners.PlayerDamageListener;
import me.glaremasters.guilds.listeners.PlayerDeathListener;
import me.glaremasters.guilds.listeners.PlayerSyncListener;
import me.glaremasters.guilds.listeners.SignListener;
import me.glaremasters.guilds.listeners.TablistListener;
import me.glaremasters.guilds.listeners.TicketListener;
import me.glaremasters.guilds.placeholders.Placeholders;
import me.glaremasters.guilds.scoreboard.GuildScoreboardHandler;
import me.glaremasters.guilds.updater.SpigotUpdater;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    public static String PREFIX;
    public static boolean vault;
    private static Main instance;
    private static Economy econ;
    private static long creationTime;
    private static TaskChainFactory taskChainFactory;
    public File languageYamlFile;
    public YamlConfiguration yaml;
    public File guildhomes = new File(this.getDataFolder(), "data/guild-homes.yml");
    public YamlConfiguration guildHomesConfig =
            YamlConfiguration.loadConfiguration(this.guildhomes);
    public File guildstatus = new File(this.getDataFolder(), "data/guild-status.yml");
    public YamlConfiguration guildStatusConfig =
            YamlConfiguration.loadConfiguration(this.guildstatus);
    public File guildtiers = new File(this.getDataFolder(), "data/guild-tiers.yml");
    public YamlConfiguration guildTiersConfig =
            YamlConfiguration.loadConfiguration(this.guildtiers);
    public File guildbanks = new File(this.getDataFolder(), "data/guild-banks.yml");
    public YamlConfiguration guildBanksConfig =
            YamlConfiguration.loadConfiguration(this.guildbanks);
    private DatabaseProvider database;
    private GuildHandler guildHandler;
    private CommandHandler commandHandler;
    private LeaderboardHandler leaderboardHandler;
    private GuildScoreboardHandler scoreboardHandler;

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }


    public static long getCreationTime() {
        return creationTime / 1000;
    }

    public static Main getInstance() {
        return instance;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onEnable() {

        // TODO: Change each language to their own variable or something to that affect so that new languages can be added without needs to delete the config folder.

        File languageFolder = new File(getDataFolder(), "languages");
        if (!languageFolder.exists()) {
            languageFolder.mkdirs();
        }

        this.languageYamlFile = new File(languageFolder, getConfig().getString("lang") + ".yml");
        this.yaml = YamlConfiguration.loadConfiguration(languageYamlFile);

        PREFIX =
                ChatColor.translateAlternateColorCodes('&',
                        this.getConfig().getString("plugin-prefix"))
                        + ChatColor.RESET + " ";
        instance = this;

        taskChainFactory = BukkitTaskChainFactory.create(this);

        setDatabaseType();

        // TODO: Clean this up and make it function easier.

        if (!getConfig().isSet("version") || getConfig().getInt("version") != 17) {
            if (getConfig().getBoolean("auto-update-config")) {
                File oldfile = new File(this.getDataFolder(), "config.yml");
                File newfile = new File(this.getDataFolder(), "config-old.yml");
                File dir = new File(this.getDataFolder(), "languages");
                File olddir = new File(this.getDataFolder(), "old-languages");
                dir.renameTo(olddir);
                oldfile.renameTo(newfile);
                Bukkit.getConsoleSender().sendMessage(
                        "§a[Guilds] §3Your config has been auto-updated and regenerated. You can find your old config in §3config-old.yml. You can disable this feature in the config");
            } else {
                Bukkit.getConsoleSender().sendMessage(
                        "§a[Guilds] §3The config is out of date. This update added Guild Banks. Please update the config to fix this.");
            }

        }
        this.saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        guildHandler = new GuildHandler();
        guildHandler.enable();

        commandHandler = new CommandHandler();
        commandHandler.enable();

        leaderboardHandler = new LeaderboardHandler();
        leaderboardHandler.enable();

        //scoreboardHandler is enabled after the guilds are loaded
        scoreboardHandler = new GuildScoreboardHandler();

        initializePlaceholder();

        getCommand("guild").setExecutor(commandHandler);

        Stream.of(
                new CommandAccept(), new CommandAdmin(), new CommandAlly(), new CommandBoot(), new CommandBuff(),
                new CommandBugReport(), new CommandCancel(), new CommandChat(), new CommandCheck(), new CommandConfirm(),
                new CommandCreate(), new CommandDecline(), new CommandDelete(), new CommandDemote(), new CommandHelp(),
                new CommandHome(), new CommandInfo(), new CommandInspect(), new CommandInvite(), new CommandLeave(),
                new CommandList(), new CommandPrefix(), new CommandPromote(), new CommandReload(), new CommandSetHome(),
                new CommandStatus(), new CommandTransfer(), new CommandUpdate(), new CommandVault(), new CommandVersion(),
                new CommandUpgrade(), new CommandBank(), new CommandGive()
        ).forEach(commandHandler::register);

        Stream.of(
                new JoinListener(), new ChatListener(), new CommandHome(), new ClickListener(), new GuildVaultListener(),
                new GuildBuffListener(), new GuildChatListener(), new MobDeathListener(), new PlayerDamageListener(),
                new DamageMultiplierListener(), new PlayerSyncListener()
        ).forEach(l -> Bukkit.getPluginManager().registerEvents(l, this));

        // TODO: Possibly change these all to a switch statement?

        if (getConfig().getBoolean("guild-signs")) {
            getServer().getPluginManager().registerEvents(new SignListener(), this);
        }
        if (getConfig().getBoolean("tablist-guilds")) {
            getServer().getPluginManager().registerEvents(new TablistListener(), this);
        }

        if (getConfig().getBoolean("reward-on-kill.enabled")) {
            getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        }

        if (getConfig().getBoolean("hooks.nametagedit")) {
            getServer().getPluginManager().registerEvents(new NameTagListener(), this);
        }
        if (getConfig().getBoolean("rewards-enabled")) {
            getServer().getPluginManager().registerEvents(new TicketListener(), this);
        }

        vault = setupEconomy();

        if (!vault) {
            getLogger().log(Level.INFO, "Not using Vault!");
        }

        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SingleLineChart("guilds") {
            @Override
            public int getValue() {
                // (This is useless as there is already a player chart by default.)
                return Main.getInstance().getGuildHandler().getGuilds().values().size();
            }
        });

        this.saveGuildData();

        try {
            BasicFileAttributes attr = Files
                    .readAttributes(Paths.get(getFile().getAbsolutePath()),
                            BasicFileAttributes.class);
            creationTime = attr.creationTime().toMillis();
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Cannot get plugin file's creation time!");
            e.printStackTrace();

            creationTime = 0;
        }

        if (getConfig().getBoolean("updater.check")) {
            SpigotUpdater updater = new SpigotUpdater(this, 46962);
            try {
                if (updater.checkForUpdates()) {
                    getLogger().info("An update was found! New version: " + updater.getLatestVersion()
                            + " download: " + updater.getResourceURL());
                }
            } catch (Exception e) {
                getLogger().info("Could not check for updates! Stacktrace:");
                e.printStackTrace();
            }
        }

        // TODO: Clean this section up with a switch statement or something.

        if (languageYamlFile.exists()) {
            return;
        } else {
            Stream.of(
                    "english", "chinese", "french", "dutch", "japanese", "swedish", "hungarian", "romanian", "slovak",
                    "russian", "simplifiedchinese", "polish", "portuguese", "german", "vietnamese", "norwegian",
                    "spanish"
            ).forEach(l -> this.saveResource("languages/" + l + ".yml", false));
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "guild reload");
    }

    // TODO: Possibly make these into something like saveGuildData()?

    public void saveGuildData() {
        try {
            guildHomesConfig.save(guildhomes);
            guildStatusConfig.save(guildstatus);
            guildBanksConfig.save(guildbanks);
            guildTiersConfig.save(guildtiers);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not save Guild Data");
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        guildHandler.disable();
        commandHandler.disable();
        scoreboardHandler.disable();
    }


    public GuildHandler getGuildHandler() {
        return guildHandler;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public DatabaseProvider getDatabaseProvider() {
        return database;
    }

    public GuildScoreboardHandler getScoreboardHandler() {
        return scoreboardHandler;
    }

    public Economy getEconomy() {
        return econ;
    }

    public void setDatabaseType() {
        switch (getConfig().getString("database.type").toLowerCase()) {
            case "json":
                database = new Json();
                break;
            case "mysql":
                database = new MySql();
                break;
            default:
                database = new Json();
                break;
        }

        database.initialize();
    }

    // TODO: Find a way to organize these.

    private void initializePlaceholder() {
        if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            PlaceholderAPI.registerPlaceholder(this, "guild_name",
                    event -> Placeholders.getGuild(event.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_master",
                    event -> Placeholders.getGuildMaster(event.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_member_count",
                    event -> Placeholders.getGuildMemberCount(event.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_prefix",
                    event -> Placeholders.getGuildPrefix(event.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_members_online",
                    event -> Placeholders.getGuildMembersOnline(event.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_status",
                    event -> Placeholders.getGuildStatus(event.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_role",
                    event -> Placeholders.getGuildRole(event.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_tier",
                    event -> Integer.toString(Placeholders.getGuildTier(event.getPlayer())));
            PlaceholderAPI.registerPlaceholder(this, "guild_balance",
                    event -> Double.toString(Placeholders.getBankBalance(event.getPlayer())));
            PlaceholderAPI.registerPlaceholder(this, "guild_upgrade_cost",
                    event -> Double.toString(Placeholders.getUpgradeCost(event.getPlayer())));
            PlaceholderAPI.registerPlaceholder(this, "guild_tier_name",
                    event -> Placeholders.getTierName(event.getPlayer()));
        }

    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    // TODO: Do I even use this stuff?

    public LeaderboardHandler getLeaderboardHandler() {
        return leaderboardHandler;
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            getLogger().log(Level.INFO, "Not using WorldGuard!");
        }

        return (WorldGuardPlugin) plugin;
    }

}
