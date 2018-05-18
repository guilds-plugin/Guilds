package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 7/12/2017.
 */
public class CommandStatus extends CommandBase {

    public CommandStatus() {
        super("status", Guilds.getInstance().getConfig().getString("commands.description.status"),
                "guilds.command.status", false, null,
                "<public | private>", 1, 1);
    }

    @Override
    public void execute(Player player, String[] args) {
        // Get the player guild
        Guild guild = Guild.getGuild(player.getUniqueId());
        // Check if the guild is null
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }
        // Set instance variable
        Guilds instance = Guilds.getInstance();
        // Get the role of a player
        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        // Check if they have the role permission to change status
        if (!role.canChangeStatus()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }
        // Make sure it's either private or public
        boolean argCheck = !args[0].equalsIgnoreCase("private") && !args[0].equalsIgnoreCase("public");
        if (argCheck) {
            Message.sendMessage(player, Message.COMMAND_STATUS_ERROR);
            return;
        }
        // Capitalize it to look nice
        String status = StringUtils.capitalize(args[0]);
        // Save in the guild status file
        instance.guildStatusConfig.set(guild.getName(), status);
        // Send out the update command
        guild.updateGuild("");
        // Tell the user they updated their prefix with the new prefix.
        Message.sendMessage(player, Message.COMMAND_STATUS_SUCCESSFUL.replace("{status}", status));
        // Save the file
        instance.saveGuildData();
    }
}
