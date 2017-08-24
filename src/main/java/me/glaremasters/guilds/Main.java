package me.glaremasters.guilds;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
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
import me.glaremasters.guilds.updater.Updater;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
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
import java.util.stream.Stream;

//FIXME: Rename to Guilds
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
//    private CommandManager commandManager;

//    private Map<CommandSender, ConfirmAction> confirmationActions = new HashMap<>();

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }

//    public CommandManager getCommandManager() {
//        return commandManager;
//    }

    public static long getCreationTime() {
        return creationTime / 1000;
    }

    public static Main getInstance() {
        return instance;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onEnable() {
        // Not working at all
//        commandManager = new BukkitCommandManager(this);
//
//        commandManager.getCommandContexts().registerContext(Guild.class, c -> {
//            String arg = c.popFirstArg();
//            Guild guild = Guild.getGuild(arg);
//            if (guild == null) {
//                throw new InvalidCommandArgument(GuildMessageKeys.INVALID_GUILD_NAME, "{name}", arg);
//            }
//            return guild;
//        });
//
//        commandManager.getCommandContexts().registerIssuerAwareContext(ConfirmAction.class, c -> {
//            CommandSender issuer = c.getIssuer().getIssuer();
//            ConfirmAction action = confirmationActions.remove(issuer);
//            if(action == null) {
//                throw new InvalidCommandArgument(GuildMessageKeys.NO_QUEUED_ACTION);
//            }
//            return action;
//        });
//
//        commandManager.registerCommand(new GuildsCommands());

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
        if (!getConfig().isSet("version") || getConfig().getInt("version") != 14) {
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
                new CommandAccept(),
                new CommandAdmin(),
                new CommandAlly(),
                new CommandBoot(),
                new CommandBuff(),
                new CommandBugReport(),
                new CommandCancel(),
                new CommandChat(),
                new CommandCheck(),
                new CommandConfirm(),
                new CommandCreate(),
                new CommandDecline(),
                new CommandDelete(),
                new CommandDemote(),
                new CommandHelp(),
                new CommandHome(),
                new CommandInfo(),
                new CommandInspect(),
                new CommandInvite(),
                new CommandLeaderboard(),
                new CommandLeave(),
                new CommandList(),
                new CommandPrefix(),
                new CommandPromote(),
                new CommandReload(),
                new CommandSetHome(),
                new CommandStatus(),
                new CommandTransfer(),
                new CommandUpdate(),
                new CommandVault(),
                new CommandVersion(),
                new CommandUpgrade(),
                new CommandBank()
        ).forEach(commandHandler::register);

        Stream.of(
                new JoinListener(),
                new ChatListener(),
                new CommandHome(),
                new ClickListener(),
                new GuildVaultListener(),
                new GuildBuffListener(),
                new GuildChatListener(),
                new MobDeathListener(),
                new PlayerDamageListener(),
                new DamageMultiplierListener()

        ).forEach(l -> Bukkit.getPluginManager().registerEvents(l, this));

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

        this.saveGuildHomes();
        this.saveGuildStatus();
        this.saveGuildTiers();
        this.saveGuildBanks();

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
                    .scheduleAsyncRepeatingTask(this, this::sendUpdate, 0L, 5000L); //5 minutes
        }

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
            this.saveResource("languages/polish.yml", false);
            this.saveResource("languages/portuguese.yml", false);
            this.saveResource("languages/german.yml", false);
            this.saveResource("languages/vietnamese.yml", false);
            this.saveResource("languages/norwegian.yml", false);
        }

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "guild reload");

    }

//    public void queueConfirmationAction(CommandSender sender, ConfirmAction action) {
//        confirmationActions.put(sender, action);
//    }

    public void saveGuildHomes() {
        try {
            Main.getInstance().guildHomesConfig.save(Main.getInstance().guildhomes);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not create Guild's Home config!");
            e.printStackTrace();
        }
    }

    public void saveGuildStatus() {
        try {
            Main.getInstance().guildStatusConfig.save(Main.getInstance().guildstatus);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not create Guild's Status config!");
            e.printStackTrace();
        }
    }

    public void saveGuildBanks() {
        try {
            Main.getInstance().guildBanksConfig.save(Main.getInstance().guildbanks);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not create Guild's Banks config!");
            e.printStackTrace();
        }
    }

    public void saveGuildTiers() {
        try {
            Main.getInstance().guildTiersConfig.save(Main.getInstance().guildtiers);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not create Guild's Tier config!");
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
        }

    }


    private void sendUpdate() {
        try {
            URL url = new URL("http://glaremasters.me/add/");

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
