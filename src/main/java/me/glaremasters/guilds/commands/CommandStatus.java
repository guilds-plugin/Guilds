package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 7/12/2017.
 */
public class CommandStatus extends CommandBase {

  public CommandStatus() {
    super("status", "Toggle your guild Public / Private", "guilds.command.status", false, null,
        "<public | private>", 1, 1);
  }

  @Override
  public void execute(Player player, String[] args) {
    Guild guild = Guild.getGuild(player.getUniqueId());

    GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
    if (!role.canToggleGuild()) {
      Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
      return;
    }
    if (!(args[0].equalsIgnoreCase("private") || args[0].equalsIgnoreCase("public"))) {
      Message.sendMessage(player, Message.COMMAND_STATUS_ERROR);
      return;
    } else {

      String status = args[0];

      Main.getInstance().guildStatusConfig
          .set(Guild.getGuild(player.getUniqueId()).getName(),
              status);

      Message.sendMessage(player, Message.COMMAND_STATUS_SUCCESSFUL.replace("{status}", status));
      Main.getInstance().saveGuildStatus();
    }
  }
}
