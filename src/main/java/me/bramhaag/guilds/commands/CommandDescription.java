package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.guild.GuildRole;
import me.bramhaag.guilds.message.Message;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 6/14/2017.
 */
public class CommandDescription extends CommandBase {

    public CommandDescription() {
        super("description", "Give your guild a description!", "guilds.command.description", false, null, "<new description>", 1, 1);
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canChangeDescription()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        String description = player.getWorld().getName();

        Main.getInstance().guilddescriptionsconfig.set(Guild.getGuild(player.getUniqueId()).getName(), description);
        Main.getInstance().saveGuilddescriptions();
        Message.sendMessage(player, Message.COMMAND_CREATE_GUILD_HOME);
    }
}
