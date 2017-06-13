package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.guild.GuildRole;
import me.bramhaag.guilds.message.Message;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.io.IOException;

/**
 * Created by MisterFantasy on 13-6-2017.
 */
public class CommandClaim extends CommandBase {

    public CommandClaim() {
        super("claim", "The ability to claim land for your guild.", "guilds.command.claim", false, null, "<size>", 0, -1);
    }

    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canClaim()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        Chunk chunk = player.getWorld().getChunkAt(player.getLocation());

        if (Main.getInstance().guildclaimsconfig.getStringList(guild.getName() + ".claims").contains(chunk)) {
            player.sendMessage("You have already claimed this chunk.");
        } else {
            Main.getInstance().guildhomesconfig.getStringList(guild.getName() + ".claims").add(chunk.toString());
            try {
                Main.getInstance().guildhomesconfig.save(Main.getInstance().guildclaims);
            } catch (IOException e) {
                e.printStackTrace();
            }

            player.sendMessage("Claimed this chunk!");
        }

    }

}
