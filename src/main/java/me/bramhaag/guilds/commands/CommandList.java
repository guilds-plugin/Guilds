package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.message.Message;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CommandList extends CommandBase {

    public CommandList() {
        super("list", "List all guilds on the server", "guilds.command.list", false, null, null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        List<Collection<Guild>> guilds = new ArrayList<>();
        guilds.add(Main.getInstance().getGuildHandler().getGuilds().values());

        Message.sendMessage(player, Message.COMMAND_LIST_FORMAT);
        player.sendMessage("§b" + guilds + " - §8(§b" + String.valueOf(guild.getMembers().size()) + "/64§8)");
    }
}
