package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.guild.GuildRole;
import me.bramhaag.guilds.message.Message;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.List;

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

        String[] singlechunk = new String[0];
        List<String> chunks = Main.getInstance().guildclaimsconfig.getStringList(guild.getName() + ".claims");

        for (String s : chunks)
            singlechunk = s.split(":");


        if (chunks.contains(singlechunk[0] + singlechunk[1])) {
            player.sendMessage("You have already claimed this chunk.");

        } else {
            chunks.add(chunk.getX() + ":" + chunk.getZ());
            Main.getInstance().saveGuildclaims();

            player.sendMessage("Claimed this chunk!");
        }

    }

}