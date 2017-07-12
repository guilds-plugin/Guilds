package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.api.events.GuildJoinEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;

public class CommandAccept extends CommandBase {

    public CommandAccept() {
        super("accept", "Accept an invite to a guild", "guilds.command.accept", false, new String[]{"join"}, "<guild id>", 1, 1);
    }

    public void execute(Player player, String[] args) {
        if (Guild.getGuild(player.getUniqueId()) != null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ALREADY_IN_GUILD);
            return;
        }

        Guild guild = Guild.getGuild(args[0]);
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_GUILD_NOT_FOUND.replace("{input}", args[0]));
            return;
        }

        if(guild.getStatus().equalsIgnoreCase("private")){
            if (!guild.getInvitedMembers().contains(player.getUniqueId())) {
                Message.sendMessage(player, Message.COMMAND_ACCEPT_NOT_INVITED);
                return;
            }
        }

        int maxMembers = Main.getInstance().getConfig().getInt("members.max-members");
        if (maxMembers != -1 && guild.getMembers().size() >= maxMembers) {
            Message.sendMessage(player, Message.COMMAND_ACCEPT_GUILD_FULL);
            return;
        }

        GuildJoinEvent event = new GuildJoinEvent(player, guild);
        if (event.isCancelled()) {
            return;
        }

        guild.sendMessage(Message.COMMAND_ACCEPT_PLAYER_JOINED.replace("{player}", player.getName()));

        guild.addMember(player.getUniqueId(), GuildRole.getLowestRole());
        guild.removeInvitedPlayer(player.getUniqueId());

        Message.sendMessage(player, Message.COMMAND_ACCEPT_SUCCESSFUL.replace("{guild}", guild.getName()));
    }
}
