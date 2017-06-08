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

public class CommandPromote extends CommandBase {

    public CommandPromote() {
        super("promote", "Promote a member of your guild", "guilds.command.promote", false, new String[]{"rankup"}, "<player> [new role]", 1, 2);
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
        if (!role.canPromote()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        Player promotedPlayer = Bukkit.getPlayer(args[0]);

        if (promotedPlayer == null || !promotedPlayer.isOnline()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_PLAYER_NOT_FOUND.replace("{player}", args[0]));
            return;
        }

        GuildMember promotedMember = guild.getMember(promotedPlayer.getUniqueId());
        if (promotedMember == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_PLAYER_NOT_IN_GUILD.replace("{player}", promotedPlayer.getName()));
            return;
        }

        int currentLevel = promotedMember.getRole();

        if (currentLevel <= 1) {
            Message.sendMessage(player, Message.COMMAND_PROMOTE_CANNOT_PROMOTE);
            return;
        }

        GuildRole promotedRole;

        if (args.length == 2) {
            promotedRole = Main.getInstance().getGuildHandler().getRoles().stream().filter(r -> r.getName().equalsIgnoreCase(args[1])).findFirst().orElse(null);
            if (promotedRole == null) {
                Message.sendMessage(player, Message.COMMAND_ERROR_INVALID_ROLE.replace("{input}", args[1]));
                return;
            }

            if (role.getLevel() > promotedMember.getRole()) {
                Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
                return;
            }

            if (currentLevel < promotedRole.getLevel()) {
                Message.sendMessage(player, Message.COMMAND_PROMOTE_NOT_PROMOTION);
                return;
            }
        } else {
            promotedRole = GuildRole.getRole(currentLevel - 1);
        }

        String oldRank = GuildRole.getRole(promotedMember.getRole()).getName();
        String newRank = promotedRole.getName();
        Message.sendMessage(promotedPlayer, Message.COMMAND_PROMOTE_SUCCESSFUL.replace("{player}", promotedPlayer.getName(), "{old-rank}", oldRank, "{new-rank}", newRank));
        Message.sendMessage(player, Message.COMMAND_PROMOTE_PROMOTED.replace("{player}", promotedPlayer.getName(), "{old-rank}", oldRank, "{new-rank}", newRank));
        promotedMember.setRole(promotedRole);
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
