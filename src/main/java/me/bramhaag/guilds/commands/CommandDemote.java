package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.guild.GuildMember;
import me.bramhaag.guilds.guild.GuildRole;
import me.bramhaag.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class CommandDemote extends CommandBase {

    public CommandDemote() {
        super("demote", "Demote a member of your guild", "guilds.command.demote", false, new String[]{"rankdown"}, "<player> [new role]", 1, 2);
    }

    @SuppressWarnings({"Duplicates", "deprecation"})
    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canDemote()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        Player demotedPlayer = Bukkit.getPlayer(args[0]);

        if (demotedPlayer == null || !demotedPlayer.isOnline()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_PLAYER_NOT_FOUND.replace("{player}", args[0]));
            return;
        }

        GuildMember demotedMember = guild.getMember(demotedPlayer.getUniqueId());
        if (demotedMember == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_PLAYER_NOT_IN_GUILD.replace("{player}", demotedPlayer.getName()));
            return;
        }

        if (demotedMember.getRole() == 3) {
            Message.sendMessage(player, Message.COMMAND_DEMOTE_CANNOT_DEMOTE);
            return;
        }

        if (demotedMember.getRole() == 0) {
            Message.sendMessage(player, Message.COMMAND_DEMOTE_CANNOT_DEMOTE);
            return;
        }

        GuildRole demotedRole;

        if (args.length == 2) {
            demotedRole = Main.getInstance().getGuildHandler().getRoles().stream().filter(r -> r.getName().equalsIgnoreCase(args[1])).findFirst().orElse(null);
            if (demotedRole == null) {
                Message.sendMessage(player, Message.COMMAND_ERROR_INVALID_ROLE.replace("{input}", args[1]));
                return;
            }

            if (role.getLevel() > demotedMember.getRole()) {
                Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
                return;
            }

            if (demotedMember.getRole() > demotedRole.getLevel()) {
                Message.sendMessage(player, Message.COMMAND_DEMOTE_NOT_DEMOTION);
                return;
            }
        } else {
            demotedRole = GuildRole.getRole(demotedMember.getRole() + 1);
        }

        String oldRank = GuildRole.getRole(demotedMember.getRole()).getName();
        String newRank = demotedRole.getName();
        Message.sendMessage(demotedPlayer, Message.COMMAND_DEMOTE_DEMOTED.replace("{player}", demotedPlayer.getName(), "{old-rank}", oldRank, "{new-rank}", newRank));
        Message.sendMessage(player, Message.COMMAND_DEMOTE_SUCCESSFUL.replace("{player}", demotedPlayer.getName(), "{old-rank}", oldRank, "{new-rank}", newRank));
        demotedMember.setRole(demotedRole);
        updateGuild("", guild.getName(), Guild.getGuild(guild.getName()).getName());
    }

    public void updateGuild(String errorMessage, String guild, String... params) {
        Main.getInstance().getDatabaseProvider().updateGuild(Guild.getGuild(guild), (result, exception) -> {
            if (!result) {
                Main.getInstance().getLogger().log(Level.SEVERE, String.format(errorMessage, params));

                if (exception != null) {
                    exception.printStackTrace();
                }
            }
        });
    }
}
