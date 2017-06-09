package me.bramhaag.guilds;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import me.bramhaag.guilds.api.MCUpdate;
import me.bramhaag.guilds.api.Metrics;
import me.bramhaag.guilds.commands.*;
import me.bramhaag.guilds.commands.base.CommandHandler;
import me.bramhaag.guilds.database.DatabaseProvider;
import me.bramhaag.guilds.database.databases.json.Json;
import me.bramhaag.guilds.database.databases.mysql.MySql;
import me.bramhaag.guilds.guild.GuildHandler;
import me.bramhaag.guilds.leaderboard.LeaderboardHandler;
import me.bramhaag.guilds.listeners.ChatListener;
import me.bramhaag.guilds.listeners.JoinListener;
import me.bramhaag.guilds.listeners.PlayerDamangeListener;
import me.bramhaag.guilds.placeholders.Placeholders;
import me.bramhaag.guilds.scoreboard.GuildScoreboardHandler;
import me.bramhaag.guilds.updater.Updater;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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

    private static Main instance;

    private DatabaseProvider database;

    private GuildHandler guildHandler;
    private CommandHandler commandHandler;
    private LeaderboardHandler leaderboardHandler;
    private GuildScoreboardHandler scoreboardHandler;

    private static Economy econ;


    private static long creationTime;

    private static TaskChainFactory taskChainFactory;

    public static String PREFIX;
    public static boolean vault;

    @SuppressWarnings("deprecation")
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.safeFile();
        this.setFileData();
        PREFIX = ChatColor.translateAlternateColorCodes('&', getConfig().getString("plugin-prefix")) + ChatColor.RESET + " ";
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

        commandHandler.register(new CommandRole());
        commandHandler.register(new CommandPromote());
        commandHandler.register(new CommandDemote());

        commandHandler.register(new CommandPrefix());
        commandHandler.register(new CommandBoot());

        commandHandler.register(new CommandConfirm());
        commandHandler.register(new CommandCancel());

        commandHandler.register(new CommandAdmin());

        commandHandler.register(new CommandReload());
        commandHandler.register(new CommandList());
        commandHandler.register(new CommandUpdate());
        commandHandler.register(new CommandVersion());
        commandHandler.register(new CommandHelp());


        if (Main.getInstance().getConfig().getBoolean("chat.enable")) {
            getServer().getPluginManager().registerEvents(new ChatListener(), this);
        }

        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamangeListener(), this);

        vault = setupEconomy();

        if (!vault) {
            getLogger().log(Level.INFO, "Not using Vault!");
        }

        Metrics metrics = new Metrics(this);


        try {
            BasicFileAttributes attr = Files.readAttributes(Paths.get(getFile().getAbsolutePath()), BasicFileAttributes.class);
            creationTime = attr.creationTime().toMillis();
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Cannot get plugin file's creation time!");
            e.printStackTrace();

            creationTime = 0;
        }

        try {

            MCUpdate update = new MCUpdate(this, true);

        } catch (IOException e) {

        }

        if (getConfig().getBoolean("updater.check")) {
            Updater.checkForUpdates((result, exception) -> {
                if (result != null) {
                    getLogger().log(Level.INFO, "A new update for Guilds has been found! Go to " + result + " to download it!");
                } else {
                    getLogger().log(Level.INFO, "No updates found!");
                }
            });
        }

        if (getConfig().getBoolean("server-list")) {
            getServer().getScheduler().scheduleAsyncRepeatingTask(this, this::sendUpdate, 0L, 6000L); //5 minutes
        }
    }

    @Override
    public void onDisable() {
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

    private void initializePlaceholder() {
        if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            PlaceholderAPI.registerPlaceholder(this, "guild_name", event -> Placeholders.getGuild(event.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_master", event -> Placeholders.getGuildMaster(event.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_member_count", event -> Placeholders.getGuildMemberCount(event.getPlayer()));
            PlaceholderAPI.registerPlaceholder(this, "guild_prefix", event -> Placeholders.getGuildPrefix(event.getPlayer()));
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            try {
                new EZPlaceholderHook(this, "guild") {
                    @Override
                    public String onPlaceholderRequest(Player player, String identifier) {
                        if (player == null) {
                            return getConfig().getString("placeholders.default");
                        }

                        switch (identifier) {
                            case "name":
                                return Placeholders.getGuild(player);
                            case "master":
                                return Placeholders.getGuildMaster(player);
                            case "member_count":
                                return Placeholders.getGuildMemberCount(player);
                            case "prefix":
                                return Placeholders.getGuildPrefix(player);
                            default:
                                return getConfig().getString("placeholders.default");
                        }
                    }
                }.hook();
            } catch (Exception ex) {
                getLogger().log(Level.WARNING, "Error while creating PlaceholderAPI placeholders!");
            }
        }
    }

    private void sendUpdate() {
        try {
            URL url = new URL("http://64.137.232.85:4567/add");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                URL checkIp = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(checkIp.openStream()));

                String ip = in.readLine();
                dos.write(String.format("ip=%s&port=%s", ip, getServer().getPort()).getBytes(StandardCharsets.UTF_8));

                conn.getResponseCode();
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Cannot sent request to server list!");
            ex.printStackTrace();
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public LeaderboardHandler getLeaderboardHandler() {
        return leaderboardHandler;
    }

    private File file = new File(this.getDataFolder(), "languages/" + this.getConfig().getString("lang") + ".yml");
    private YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

    private void safeFile() {
        try {
            yaml.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void reloadFile() {
        try {
            yaml.load(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void setFileData() {
        if (this.getConfig().getString("lang").equalsIgnoreCase("en")) {
            if (!this.file.exists()) {
                String error = "messages.command.error.";
                yaml.set(error + "console", "&cThis command can only be executed by a player!");
                yaml.set(error + "args", "&cInvalid arguments! See /guilds help for more information");
                yaml.set(error + "permission", "&cYou don't have permission to do that!");
                yaml.set(error + "not-found", "&cCommand not found! See /guilds help for all commands");
                yaml.set(error + "no-guild", "&cYou're not in a guild!");
                yaml.set(error + "not-guildmaster", "&cYou're not the Guild Master of this guild");
                yaml.set(error + "invalid_number", "&c{input} is not a valid number!");
                yaml.set(error + "already-in-guild", "&c{input} is not a valid number!");
                yaml.set(error + "player-not-found", "&cPlayer '{player}' does not exist or is not online!");
                yaml.set(error + "player-not-in-guild", "&cPlayer '{player}' is not in your guild!");
                yaml.set(error + "invalid-role", "&cRole '{input}' does not exist!");
                yaml.set(error + "role-no-permission", "&cYour role is not high enough to do that!");
                yaml.set(error + "not-enough-money", "&aSorry! You don't have enough money to create a guild!");
                String help = "messages.command.help.";
                yaml.set(help + "message", "&f/guild {command} {arguments} &7- &f{description}");
                yaml.set(help + "next-page", "&7See /guilds help {next-page} for the next page");
                yaml.set(help + "invalid-page", "&cPage not found!");
                String role = "messages.command.role.";
                yaml.set(role + "players", "{player} - {role}");
                String list = "messages.command.list.";
                yaml.set(list + "format", "The following guilds are:");
                String create = "messages.command.create.";
                yaml.set(create + "successful", "&aGuild '{guild}' created successfully!");
                yaml.set(create + "cancelled", "&cGuild creation cancelled!");
                yaml.set(create + "warning", "&cType /guilds confirm to create your guild, type /guilds cancel to cancel.");
                yaml.set(create + "error", "&cSomething went wrong while creating your guild!");
                yaml.set(create + "requirements", "&cYour guild's name does not match the requirements! You can only use alphanumeric characters and the length of the name cannot exceed 64");
                yaml.set(create + "guild-name-taken", "&cThis name is already taken!");
                yaml.set(create + "money-warning", "&c Are you sure you want to spend {amount} to create a guild? (Type /guilds confirm to continue)");
                String delete = "messages.command.delete.";
                yaml.set(delete + "successful", "&aDeleted '{guild}' successfully!");
                yaml.set(delete + "cancelled", "&cGuild deletion cancelled!");
                yaml.set(delete + "warning", "&cType /guilds confirm to delete your guild, type /guilds cancel to cancel.");
                yaml.set(delete + "error", "&cSomething went wrong while deleting your guild!");
                String info = "messages.command.info.";
                yaml.set(info + "header", "Information for &b{guild}");
                yaml.set(info + "name", "Name: &b{guild}&r (&bPrefix: {prefix}&r)");
                yaml.set(info + "master", "Guild Master: &b{master}");
                yaml.set(info + "member-count", "Members: &b{members}/64&r (&bOnline: {members-online}&r)");
                yaml.set(info + "rank", "Your rank: &b{rank}");
                String promote = "messages.command.promote.";
                yaml.set(promote + "promoted", "&aYou've successfully promoted {player} from {old-rank} to {new-rank}!");
                yaml.set(promote + "successful", "&aYou've been promoted from {old-rank} to {new-rank}!");
                yaml.set(promote + "cannot-promote", "&cThis player cannot be promoted any further!");
                yaml.set(promote + "not-promotion", "&cYou aren't promoting this player!");
                String demote = "messages.command.demote.";
                yaml.set(demote + "demoted", "&cYou've been demoted from {old-rank} to {new-rank}!");
                yaml.set(demote + "successful", "&aYou've successfully demoted {player} from {old-rank} to {new-rank}!");
                yaml.set(demote + "cannot-demote", "&cThis player cannot be demoted any further!");
                yaml.set(demote + "not-demotion", "&cYou aren't demoting this player!");
                String chat = "messages.command.chat.";
                yaml.set(chat + "message", "&7&lGuild Chat> &r[{role}] {player}: {message}");
                String accept = "messages.command.accept.";
                yaml.set(accept + "not-invited", "&cYou aren't invited for this guild!");
                yaml.set(accept + "guild-full", "&cThis guild is full!");
                yaml.set(accept + "successful", "&aYou joined guild '{guild}' successfully");
                yaml.set(accept + "player-joined", "&aPlayer '{player}' joined your guild!");
                String invite = "messages.command.invite.";
                yaml.set(invite + "message", "&a{player} has invited you to his/her guild, '{guild}'");
                yaml.set(invite + "successful", "&aYou've successfully invited {player} to your guild!");
                yaml.set(invite + "already-in-guild", "&cThis player is already in your guild!");
                String leave = "messages.command.leave.";
                yaml.set(leave + "successful", "&aYou've successfully left your guild!");
                yaml.set(leave + "cancelled", "&cLeaving guild cancelled!");
                yaml.set(leave + "warning", "&cType /guilds confirm to leave your guild, type /guilds cancel to cancel.");
                yaml.set(leave + "warning-guildmaster", "&cYou're the Guild Master of this guild, leaving the guild will mean that the guild is deleted. Type /guilds confirm to leave and delete your guild, type /guilds cancel to cancel.");
                yaml.set(leave + "player-left", "&cPlayer '{player}' left your guild!");
                String boot = "messages.command.boot.";
                yaml.set(boot + "successful", "&aSuccessfully kicked {player} from your guild!");
                yaml.set(boot + "kicked", "&cYou have been kicked from your guild by {kicker}!");
                yaml.set(boot + "player-kicked", "&cPlayer '{player}' has been kicked from the guild by {kicker}!");


                this.safeFile();
            }
        }
    }
}
