package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.api.events.GuildJoinEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CommandAccept extends CommandBase {

    public CommandAccept() {
        super("accept", Main.getInstance().getConfig().getString("commands.description.accept"),
                "guilds.command.accept", false,
                new String[]{"join"}, "<guild id>", 0, 1);
    }

    public void execute(Player player, String[] args) {
        if (Guild.getGuild(player.getUniqueId()) != null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ALREADY_IN_GUILD);
            return;
        }
        Guild guild = (Guild) Main.getInstance().getGuildHandler().getGuilds().values().toArray()[0];
        try {
            if (args.length == 0){
                int invites = 0;
                int indexes = 0;
                for (int i = 0; i<Main.getInstance().getGuildHandler().getGuilds().values().size();i++){
                    Guild guildtmp = (Guild) Main.getInstance().getGuildHandler().getGuilds().values().toArray()[i];
                    if (guildtmp.getInvitedMembers().contains(player.getUniqueId())){
                        invites++;
                        indexes = i;
                    }
                }
                if (invites == 1){
                    guild = (Guild) Main.getInstance().getGuildHandler().getGuilds().values().toArray()[indexes];
                }
                else {
                    Message.sendMessage(player, Message.COMMAND_ACCEPT_NOT_INVITED);
                    return;
                }
            }
            else {
                guild = Guild.getGuild(args[0]);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_GUILD_NOT_FOUND.replace("{input}", args[0]));
            return;
        }

        if (guild.getStatus().equalsIgnoreCase("private")) {
            if (!guild.getInvitedMembers().contains(player.getUniqueId())) {
                Message.sendMessage(player, Message.COMMAND_ACCEPT_NOT_INVITED);
                return;
            }
        }

        int maxMembers = guild.getMaxMembers();
        if (guild.getMembers().size() >= maxMembers) {
            Message.sendMessage(player, Message.COMMAND_ACCEPT_GUILD_FULL);
            return;
        }

        GuildJoinEvent event = new GuildJoinEvent(player, guild);
        if (event.isCancelled()) {
            return;
        }

        guild.sendMessage(
                Message.COMMAND_ACCEPT_PLAYER_JOINED.replace("{player}", player.getName()));

        guild.addMember(player.getUniqueId(), GuildRole.getLowestRole());
        guild.removeInvitedPlayer(player.getUniqueId());
        if (Main.getInstance().getConfig().getBoolean("tablist-guilds")) {
            String name =
                    Main.getInstance().getConfig().getBoolean("tablist-use-display-name") ? player
                            .getDisplayName() : player.getName();
            player.setPlayerListName(
                    ChatColor.translateAlternateColorCodes('&',
                            Main.getInstance().getConfig().getString("tablist")
                                    .replace("{guild}", guild.getName())
                                    .replace("{prefix}", guild.getPrefix())
                                    + name));
        }

        Message.sendMessage(player,
                Message.COMMAND_ACCEPT_SUCCESSFUL.replace("{guild}", guild.getName()));
    }
}
