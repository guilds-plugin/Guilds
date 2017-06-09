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
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
        // this.setFileData();

        PREFIX = ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("plugin-prefix")) + ChatColor.RESET + " ";
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
            e.printStackTrace();
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

    // public File file = new File(Main.getInstance().getDataFolder(), "languages/" + Main.getInstance().getConfig().getString("lang") + ".yml");
    // public YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

    /*public void saveFile() {
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

    public void setFileData() {
        if (Main.getInstance().getConfig().getString("lang").equalsIgnoreCase("en")) {

            String error = "messages.command.error.";
            yaml.addDefault(error + "console", "&cThis command can only be executed by a player!");
            yaml.addDefault(error + "args", "&cInvalid arguments! See /guilds help for more information");
            yaml.addDefault(error + "permission", "&cYou don't have permission to do that!");
            yaml.addDefault(error + "not-found", "&cCommand not found! See /guilds help for all commands");
            yaml.addDefault(error + "no-guild", "&cYou're not in a guild!");
            yaml.addDefault(error + "not-guildmaster", "&cYou're not the Guild Master of this guild");
            yaml.addDefault(error + "invalid_number", "&c{input} is not a valid number!");
            yaml.addDefault(error + "already-in-guild", "&c{input} is not a valid number!");
            yaml.addDefault(error + "player-not-found", "&cPlayer '{player}' does not exist or is not online!");
            yaml.addDefault(error + "player-not-in-guild", "&cPlayer '{player}' is not in your guild!");
            yaml.addDefault(error + "invalid-role", "&cRole '{input}' does not exist!");
            yaml.addDefault(error + "role-no-permission", "&cYour role is not high enough to do that!");
            yaml.addDefault(error + "not-enough-money", "&aSorry! You don't have enough money to create a guild!");
            String help = "messages.command.help.";
            yaml.addDefault(help + "message", "&f/guild {command} {arguments} &7- &f{description}");
            yaml.addDefault(help + "next-page", "&7See /guilds help {next-page} for the next page");
            yaml.addDefault(help + "invalid-page", "&cPage not found!");
            String role = "messages.command.role.";
            yaml.addDefault(role + "players", "{player} - {role}");
            String list = "messages.command.list.";
            yaml.addDefault(list + "format", "The following guilds are:");
            String create = "messages.command.create.";
            yaml.addDefault(create + "successful", "&aGuild '{guild}' created successfully!");
            yaml.addDefault(create + "cancelled", "&cGuild creation cancelled!");
            yaml.addDefault(create + "warning", "&cType /guilds confirm to create your guild, type /guilds cancel to cancel.");
            yaml.addDefault(create + "error", "&cSomething went wrong while creating your guild!");
            yaml.addDefault(create + "requirements", "&cYour guild's name does not match the requirements! You can only use alphanumeric characters and the length of the name cannot exceed 64");
            yaml.addDefault(create + "guild-name-taken", "&cThis name is already taken!");
            yaml.addDefault(create + "money-warning", "&c Are you sure you want to spend {amount} to create a guild? (Type /guilds confirm to continue)");
            String delete = "messages.command.delete.";
            yaml.addDefault(delete + "successful", "&aDeleted '{guild}' successfully!");
            yaml.addDefault(delete + "cancelled", "&cGuild deletion cancelled!");
            yaml.addDefault(delete + "warning", "&cType /guilds confirm to delete your guild, type /guilds cancel to cancel.");
            yaml.addDefault(delete + "error", "&cSomething went wrong while deleting your guild!");
            String info = "messages.command.info.";
            yaml.addDefault(info + "header", "Information for &b{guild}");
            yaml.addDefault(info + "name", "Name: &b{guild}&r (&bPrefix: {prefix}&r)");
            yaml.addDefault(info + "master", "Guild Master: &b{master}");
            yaml.addDefault(info + "member-count", "Members: &b{members}/64&r (&bOnline: {members-online}&r)");
            yaml.addDefault(info + "rank", "Your rank: &b{rank}");
            String promote = "messages.command.promote.";
            yaml.addDefault(promote + "promoted", "&aYou've successfully promoted {player} from {old-rank} to {new-rank}!");
            yaml.addDefault(promote + "successful", "&aYou've been promoted from {old-rank} to {new-rank}!");
            yaml.addDefault(promote + "cannot-promote", "&cThis player cannot be promoted any further!");
            yaml.addDefault(promote + "not-promotion", "&cYou aren't promoting this player!");
            String demote = "messages.command.demote.";
            yaml.addDefault(demote + "demoted", "&cYou've been demoted from {old-rank} to {new-rank}!");
            yaml.addDefault(demote + "successful", "&aYou've successfully demoted {player} from {old-rank} to {new-rank}!");
            yaml.addDefault(demote + "cannot-demote", "&cThis player cannot be demoted any further!");
            yaml.addDefault(demote + "not-demotion", "&cYou aren't demoting this player!");
            String chat = "messages.command.chat.";
            yaml.addDefault(chat + "message", "&7&lGuild Chat> &r[{role}] {player}: {message}");
            String accept = "messages.command.accept.";
            yaml.addDefault(accept + "not-invited", "&cYou aren't invited for this guild!");
            yaml.addDefault(accept + "guild-full", "&cThis guild is full!");
            yaml.addDefault(accept + "successful", "&aYou joined guild '{guild}' successfully");
            yaml.addDefault(accept + "player-joined", "&aPlayer '{player}' joined your guild!");
            String invite = "messages.command.invite.";
            yaml.addDefault(invite + "message", "&a{player} has invited you to his/her guild, '{guild}'");
            yaml.addDefault(invite + "successful", "&aYou've successfully invited {player} to your guild!");
            yaml.addDefault(invite + "already-in-guild", "&cThis player is already in your guild!");
            String leave = "messages.command.leave.";
            yaml.addDefault(leave + "successful", "&aYou've successfully left your guild!");
            yaml.addDefault(leave + "cancelled", "&cLeaving guild cancelled!");
            yaml.addDefault(leave + "warning", "&cType /guilds confirm to leave your guild, type /guilds cancel to cancel.");
            yaml.addDefault(leave + "warning-guildmaster", "&cYou're the Guild Master of this guild, leaving the guild will mean that the guild is deleted. Type /guilds confirm to leave and delete your guild, type /guilds cancel to cancel.");
            yaml.addDefault(leave + "player-left", "&cPlayer '{player}' left your guild!");
            String boot = "messages.command.boot.";
            yaml.addDefault(boot + "successful", "&aSuccessfully kicked {player} from your guild!");
            yaml.addDefault(boot + "kicked", "&cYou have been kicked from your guild by {kicker}!");
            yaml.addDefault(boot + "player-kicked", "&cPlayer '{player}' has been kicked from the guild by {kicker}!");
            String prefix = "messages.command.prefix.";
            yaml.addDefault(prefix + "successful", "&aGuild's prefix changed successfully!");
            yaml.addDefault(prefix + "requirements", "&cYour guild's prefix does not match the requirements! You can only use alphanumeric characters and the length of the prefix cannot exceed 6");
            String confirm = "messages.command.confirm.";
            yaml.addDefault(confirm + "error", "&cYou have no actions to confirm!");
            String cancel = "messages.command.cancel.";
            yaml.addDefault(cancel + "error", "&cYou have no actions to cancel!");
            String reload = "messages.command.reload.";
            yaml.addDefault(reload + "reloaded", "&aConfiguration file reloaded!");
            String update = "messages.command.update.";
            yaml.addDefault(update + "found", "&aFound an update! Go to {url} to download it!");
            yaml.addDefault(update + "not-found", "&cNo update found!");
            String admin = "messages.command.admin.";
            yaml.addDefault(admin + "delete-successful", "&cGuild removed successfully!");
            yaml.addDefault(admin + "delete-error", "&cSomething went wrong while removing this guild!");
            yaml.addDefault(admin + "delete-warning", "&cType /guilds confirm to remove this guild, type /guilds cancel to cancel.");
            yaml.addDefault(admin + "delete-cancelled", "&cGuild deletion cancelled!");
            yaml.addDefault(admin + "player-already-in-guild", "&cThis player is already in a guild!");
            yaml.addDefault(admin + "added-player", "&aAdded player to guild");
            yaml.addDefault(admin + "player-not-in-guild", "&cPlayer is not in this guild!");
            yaml.addDefault(admin + "removed-player", "&aRemoved player from guild!");
            String ally = "messages.command.ally.";
            yaml.addDefault(ally + "guild-not-pending", "&cThis guild has not send you an ally request!");
            yaml.addDefault(ally + "accepted", "&aThis guild is now allied with {guild}!");
            yaml.addDefault(ally + "accepted-target", "&a{guild} is now allied with us!");
            yaml.addDefault(ally + "declined", "&c{guild} does not want to be an ally!");
            yaml.addDefault(ally + "send", "&aSend ally request!");
            yaml.addDefault(ally + "send-target", "&aRecieved an ally request from {guild}! Type /guilds ally accept {guild} to accept or type /guilds ally decline {guild} to decline");
            yaml.addDefault(ally + "removed", "&cThis guild is no longer allied with {guild}!");
            yaml.addDefault(ally + "removed-target", "&c{guild} is no longer allied with us!");
            yaml.addDefault(ally + "already-allies", "&cYou're already allied with {guild}!");
            yaml.addDefault(ally + "not-allies", "&cYou aren't allied with {guild}!");
            String join = "messages.event.join.";
            yaml.addDefault(join + "pending-invites", "&aYou have {number} pending invite(s) from the guild(s): &e{guilds}");
            this.saveFile();
        }
    }*/
}
