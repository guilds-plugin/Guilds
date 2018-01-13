package me.glaremasters.guilds.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class CommandAdmin extends CommandBase {


    public CommandAdmin() {
        super("admin", Guilds.getInstance().getConfig().getString("commands.description.admin"),
                "guilds.command.admin", true, null,
                "<addplayer | removeplayer> <guild name> <player name>, "
                        + "or <claim> <guildname>,"
                        + "or <create> <guildname> <playername>"
                        + "or <upgrade> <guild name>, or <status> <guild name> <Public | Private>, "
                        + "or <prefix> <guild name> <new prefix>",
                1, 3);
    }


    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length < 2) {
            Message.sendMessage(sender, Message.COMMAND_ERROR_ARGS);
            return;
        }

        if (args[0].equalsIgnoreCase("create")) {

            FileConfiguration config = Guilds.getInstance().getConfig();
            int minLength = config.getInt("name.min-length");
            int maxLength = config.getInt("name.max-length");
            String regex = config.getString("name.regex");

            if (args[1].length() < minLength || args[1].length() > maxLength || !args[1]
                    .matches(regex)) {
                Message.sendMessage(sender, Message.COMMAND_CREATE_NAME_REQUIREMENTS);
                return;
            }

            for (String name : Guilds.getInstance().getGuildHandler().getGuilds().keySet()) {
                if (name.equalsIgnoreCase(args[1])) {
                    Message.sendMessage(sender, Message.COMMAND_CREATE_GUILD_NAME_TAKEN);
                    return;
                }
            }

            String name = args[2];
            String URL = "https://use.gameapis.net/mc/player/uuid/" + name;

            try {
                URL url = new URL(URL);
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.connect();

                JsonParser jp = new JsonParser(); //from gson
                JsonElement root = jp
                        .parse(new InputStreamReader((InputStream) request.getContent()));
                JsonObject rootobj = root.getAsJsonObject();

                UUID uuid = UUID.fromString(rootobj.get("uuid_formatted").getAsString());

                Guild guild = new Guild(ChatColor.translateAlternateColorCodes('&', args[1]), uuid);

                Guilds.getInstance().getDatabaseProvider()
                        .createGuild(guild, (result, exception) -> {
                            if (result) {
                                Message.sendMessage(sender,
                                        Message.COMMAND_CREATE_SUCCESSFUL
                                                .replace("{guild}", args[1]));

                                Guilds.getInstance().guildStatusConfig
                                        .set(guild.getName(), "Public");
                                Guilds.getInstance().guildTiersConfig.set(guild.getName(), 1);
                                Guilds.getInstance().saveGuildData();
                                for (String perms : guild.getGuildPerms()) {
                                    Guilds.getPermissions()
                                            .playerAdd(null, Bukkit.getOfflinePlayer(uuid), perms);
                                }
                            } else {
                                Message.sendMessage(sender, Message.COMMAND_CREATE_ERROR);
                                if (exception != null) {
                                    exception.printStackTrace();
                                }
                            }
                        });

            } catch (Exception exception) {
                Guilds.getInstance().getLogger().warning("Could not make guild. API down?");
            }
            return;
        }

        Guild guild = Guild.getGuild(args[1]);

        if (guild == null) {
            Message.sendMessage(sender, Message.COMMAND_ERROR_NO_GUILD);
            return;
        } else if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove")) {
            Message.sendMessage(sender,
                    Message.COMMAND_ADMIN_DELETE_WARNING.replace("{guild}", args[1]));

            Guilds.getInstance().getCommandHandler().addAction(sender, new ConfirmAction() {
                @Override
                public void accept() {
                    Guilds.getInstance().getCommandHandler().removeAction(sender);
                }

                @Override
                public void decline() {
                    Message.sendMessage(sender, Message.COMMAND_ADMIN_DELETE_CANCELLED);
                    Guilds.getInstance().getCommandHandler().removeAction(sender);
                }
            });
        } else if (args[0].equalsIgnoreCase("addplayer")) {
            if (args.length != 3) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_ARGS);
                return;
            }

            Player player = Bukkit.getPlayer(args[2]);
            if (player == null || !player.isOnline()) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_PLAYER_NOT_FOUND);
                return;
            }

            if (Guild.getGuild(player.getUniqueId()) != null) {
                Message.sendMessage(sender, Message.COMMAND_ADMIN_PLAYER_ALREADY_IN_GUILD);
                return;
            }

            guild.addMember(player.getUniqueId(), GuildRole.getLowestRole());

            Message.sendMessage(player,
                    Message.COMMAND_ACCEPT_SUCCESSFUL.replace("{guild}", guild.getName()));
            Message.sendMessage(sender, Message.COMMAND_ADMIN_ADDED_PLAYER);
        } else if (args[0].equalsIgnoreCase("removeplayer")) {
            if (args.length != 3) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_ARGS);
                return;
            }

            Player player = Bukkit.getPlayer(args[2]);
            if (player == null || !player.isOnline()) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_PLAYER_NOT_FOUND);
                return;
            }

            if (Guild.getGuild(player.getUniqueId()) == null) {
                Message.sendMessage(sender, Message.COMMAND_ADMIN_PLAYER_NOT_IN_GUILD);
                return;
            }

            guild.removeMember(player.getUniqueId());

            Message.sendMessage(player, Message.COMMAND_LEAVE_SUCCESSFUL);
            Message.sendMessage(sender, Message.COMMAND_ADMIN_REMOVED_PLAYER);
        } else if (args[0].equalsIgnoreCase("upgrade")) {
            FileConfiguration config = Guilds.getInstance().getConfig();
            int tier = guild.getTier();
            if (guild.getTier() >= Guilds.getInstance().getConfig().getInt("max-number-of-tiers")) {
                Message.sendMessage(sender, Message.COMMAND_UPGRADE_TIER_MAX);
                return;
            }
            Message.sendMessage(sender, Message.COMMAND_UPGRADE_SUCCESS);
            Guilds.getInstance().guildTiersConfig.set(guild.getName(), tier + 1);
            Guilds.getInstance().saveGuildData();

            guild.updateGuild("");
        } else if (args[0].equalsIgnoreCase("status")) {
            if (args.length != 3) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_ARGS);
                return;
            }
            if (!(args[2].equalsIgnoreCase("private") || args[2].equalsIgnoreCase("public"))) {
                Message.sendMessage(sender, Message.COMMAND_STATUS_ERROR);
            } else {
                String status = args[2];
                Guilds.getInstance().guildStatusConfig
                        .set(args[1],
                                status);
                Guild.getGuild(args[1]).updateGuild("");

                Message.sendMessage(sender,
                        Message.COMMAND_STATUS_SUCCESSFUL.replace("{status}", status));
                Guilds.getInstance().saveGuildData();
            }
        } else if (args[0].equalsIgnoreCase("prefix")) {
            FileConfiguration config = Guilds.getInstance().getConfig();
            if (args.length != 3) {
                Message.sendMessage(sender, Message.COMMAND_ERROR_ARGS);
                return;
            }
            if (!args[2].matches(Guilds.getInstance().getConfig().getString("prefix.regex"))) {
                Message.sendMessage(sender, Message.COMMAND_PREFIX_REQUIREMENTS);
                return;
            }
            Message.sendMessage(sender, Message.COMMAND_PREFIX_SUCCESSFUL);
            guild.updatePrefix(ChatColor.translateAlternateColorCodes('&', args[2]));
        }
    }
}
