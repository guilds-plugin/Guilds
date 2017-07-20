package me.glaremasters.guilds;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import me.glaremasters.guilds.api.Metrics;
import me.glaremasters.guilds.commands.*;
import me.glaremasters.guilds.commands.base.CommandHandler;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.database.databases.json.Json;
import me.glaremasters.guilds.database.databases.mysql.MySql;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.leaderboard.LeaderboardHandler;
import me.glaremasters.guilds.listeners.ChatListener;
import me.glaremasters.guilds.listeners.ClickListener;
import me.glaremasters.guilds.listeners.GuildDamageListener;
import me.glaremasters.guilds.listeners.JoinListener;
import me.glaremasters.guilds.listeners.PlayerDamangeListener;
import me.glaremasters.guilds.listeners.SignListener;
import me.glaremasters.guilds.listeners.TablistListener;
import me.glaremasters.guilds.placeholders.Placeholders;
import me.glaremasters.guilds.scoreboard.GuildScoreboardHandler;
import me.glaremasters.guilds.updater.Updater;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;

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
    public YamlConfiguration guildhomesconfig =
        YamlConfiguration.loadConfiguration(this.guildhomes);
    public File guildstatus = new File(this.getDataFolder(), "data/guild-status.yml");
    public YamlConfiguration guildstatusconfig =
        YamlConfiguration.loadConfiguration(this.guildstatus);
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

    @SuppressWarnings("deprecation") @Override public void onEnable() {

        File languageFolder = new File(getDataFolder(), "languages");
        if (!languageFolder.exists()) {
            languageFolder.mkdirs();
        }

        this.languageYamlFile = new File(languageFolder, getConfig().getString("lang") + ".yml");
        this.yaml = YamlConfiguration.loadConfiguration(languageYamlFile);


        PREFIX =
            ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("plugin-prefix"))
                + ChatColor.RESET + " ";
        instance = this;

        taskChainFactory = BukkitTaskChainFactory.create(this);

        setDatabaseType();

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

        commandHandler.register(new CommandCreate());
        commandHandler.register(new CommandDelete());

        commandHandler.register(new CommandInvite());
        commandHandler.register(new CommandAccept());
        commandHandler.register(new CommandLeave());

        commandHandler.register(new CommandAlly());

        commandHandler.register(new CommandChat());

        commandHandler.register(new CommandInfo());
        commandHandler.register(new CommandBugReport());

        commandHandler.register(new CommandPromote());
        commandHandler.register(new CommandDemote());

        commandHandler.register(new CommandPrefix());
        commandHandler.register(new CommandBoot());

        commandHandler.register(new CommandConfirm());
        commandHandler.register(new CommandCancel());

        commandHandler.register(new CommandAdmin());
        commandHandler.register(new CommandSetHome());
        commandHandler.register(new CommandHome());

        commandHandler.register(new CommandReload());
        commandHandler.register(new CommandList());
        commandHandler.register(new CommandUpdate());
        commandHandler.register(new CommandVersion());
        commandHandler.register(new CommandHelp());
        commandHandler.register(new CommandInspect());
        commandHandler.register(new CommandStatus());



        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamangeListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getServer().getPluginManager().registerEvents(new CommandHome(), this);
        getServer().getPluginManager().registerEvents(new ClickListener(), this);
        if (getConfig().getBoolean("guild-signs")) {
            getServer().getPluginManager().registerEvents(new SignListener(), this);
        }
        if (getConfig().getBoolean("tablist-guilds")) {
            getServer().getPluginManager().registerEvents(new TablistListener(), this);
        }

        if (getConfig().getBoolean("allow-guild-damage")) {
            getServer().getPluginManager().registerEvents(new GuildDamageListener(), this);
        }

        vault = setupEconomy();

        if (!vault) {
            getLogger().log(Level.INFO, "Not using Vault!");
        }

        Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SingleLineChart("guilds") {
            @Override public int getValue() {
                // (This is useless as there is already a player chart by default.)
                return Main.getInstance().getGuildHandler().getGuilds().values().size();
            }
        });



        this.saveGuildhomes();
        this.saveGuildstatus();

        try {
            BasicFileAttributes attr = Files
                .readAttributes(Paths.get(getFile().getAbsolutePath()), BasicFileAttributes.class);
            creationTime = attr.creationTime().toMillis();
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Cannot get plugin file's creation time!");
            e.printStackTrace();

            creationTime = 0;
        }

        if (getConfig().getBoolean("updater.check")) {
            Updater.checkForUpdates((result, exception) -> {
                if (result != null) {
                    getLogger().log(Level.INFO,
                        "A new update for Guilds has been found! Go to " + result
                            + " to download it!");
                } else {
                    getLogger().log(Level.INFO, "No updates found!");
                }
            });
        }

        if (getConfig().getBoolean("server-list")) {
            getServer().getScheduler()
                .scheduleAsyncRepeatingTask(this, this::sendUpdate, 0L, 2000L); //5 minutes
        }

        if (!getConfig().isSet("version")) {
            File oldfile = new File(this.getDataFolder(), "config.yml");
            File newfile = new File(this.getDataFolder(), "config-old.yml");
            oldfile.renameTo(newfile);
        }


        this.saveDefaultConfig();




        if (languageYamlFile.exists()) {
            return;
        } else {
            this.saveResource("languages/english.yml", false);
            this.saveResource("languages/chinese.yml", false);
            this.saveResource("languages/french.yml", false);
            this.saveResource("languages/dutch.yml", false);
            this.saveResource("languages/japanese.yml", false);
            this.saveResource("languages/swedish.yml", false);
            this.saveResource("languages/hungarian.yml", false);
            this.saveResource("languages/romanian.yml", false);
            this.saveResource("languages/slovak.yml", false);
            this.saveResource("languages/russian.yml", false);
            this.saveResource("languages/simplifiedchinese.yml", false);
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "guild reload");

    }

    public void saveGuildhomes() {
        try {
            Main.getInstance().guildhomesconfig.save(Main.getInstance().guildhomes);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not create Guild's Home config!");
            e.printStackTrace();
        }
    }

    public void saveGuildstatus() {
        try {
            Main.getInstance().guildstatusconfig.save(Main.getInstance().guildstatus);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not create Guild's Status config!");
            e.printStackTrace();
        }
    }

    @Override public void onDisable() {
        guildHandler.disable();
        commandHandler.disable();
        scoreboardHandler.disable();
    }

    public DatabaseProvider getDatabaseProvider() {
        return database;
    }

    public GuildHandler getGuildHandler() {
        return guildHandler;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
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
        }

    }

    private void sendUpdate() {
        try {
            URL url = new URL("http://185.185.248.3:4567/add");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
            getLogger()
                .log(Level.SEVERE, "Cannot sent request to server list! Serverlist must be down!");
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

    public LeaderboardHandler getLeaderboardHandler() {
        return leaderboardHandler;
    }
}