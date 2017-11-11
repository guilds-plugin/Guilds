package me.glaremasters.guilds;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.stream.Stream;
import me.glaremasters.guilds.api.Metrics;
import me.glaremasters.guilds.commands.*;
import me.glaremasters.guilds.commands.base.CommandHandler;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.database.databases.json.Json;
import me.glaremasters.guilds.database.databases.mysql.MySql;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.leaderboard.LeaderboardHandler;
import me.glaremasters.guilds.listeners.*;
import me.glaremasters.guilds.placeholders.Placeholders;
import me.glaremasters.guilds.scoreboard.GuildScoreboardHandler;
import me.glaremasters.guilds.updater.SpigotUpdater;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
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
        instance = this;
        if (getConfig().getBoolean("announcements.console")) {
            try {
                URL url = new URL("https://glaremasters.me/guilds/announcements/" + getDescription().getVersion());
                URLConnection con = url.openConnection();
                con.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                InputStream in = con.getInputStream();
                String encoding = con.getContentEncoding();
                encoding = encoding == null ? "UTF-8" : encoding;
                String body = IOUtils.toString(in, encoding);
                Bukkit.getConsoleSender().sendMessage(body);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

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


        taskChainFactory = BukkitTaskChainFactory.create(this);

        setDatabaseType();

        // TODO: Clean this up and make it function easier.


        if (!getConfig().isSet("version") || getConfig().getInt("version") != 18) {
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
                new CommandUpgrade(), new CommandBank(), new CommandGive(), new CommandClaim()
        ).forEach(commandHandler::register);

        Stream.of(
                new JoinListener(), new ChatListener(), new CommandHome(), new ClickListener(), new GuildVaultListener(),
                new GuildBuffListener(), new GuildChatListener(), new MobDeathListener(), new PlayerDamageListener(),
                new DamageMultiplierListener(), new AnnouncementListener()
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
        metrics.addCustomChart(new Metrics.SingleLineChart("guilds", () -> Main.getInstance().getGuildHandler().getGuilds().values().size()));

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
            SpigotUpdater updater = new SpigotUpdater(this, 48920);
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

        if (getConfig().getBoolean("server-list")) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, this::sendUpdate, 0L, 5000L);
        }

        if (languageYamlFile.exists()) {
            return;
        } else {
            Stream.of(
                    "english", "chinese", "french", "dutch", "japanese", "swedish", "hungarian", "romanian", "slovak",
                    "russian", "simplifiedchinese", "polish", "portuguese", "german", "vietnamese", "norwegian",
                    "spanish", "italian"
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

    public void sendUpdate() {
        try {
            URL url = new URL("https://glaremasters.me/add/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                URL checkIp = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(checkIp.openStream()));
                String ip = in.readLine();
                dos.write(String.format("ip=%s&port=%s", ip, getServer().getPort())
                        .getBytes(StandardCharsets.UTF_8));
                conn.getResponseCode();
            }
        } catch (Exception ex) {
            return;
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




}
