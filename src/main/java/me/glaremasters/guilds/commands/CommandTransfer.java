package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 7/22/2017.
 */
public class CommandTransfer extends CommandBase {

  public CommandTransfer() {
    super("transfer", "Transfer your guild to another user", "guilds.command.transfer", false, null,
        "<name>", 1, 1);
  }

  @Override
  public void execute(Player player, String[] args) {
    Guild guild = Guild.getGuild(player.getUniqueId());
    if (guild == null) {
      Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
      return;
    }
    GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
    if (!role.canTransfer()) {
      Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
      return;
    }

    Player transferPlayer = Bukkit.getPlayer(args[0]);

    if (transferPlayer == null || !transferPlayer.isOnline()) {
      Message.sendMessage(player,
          Message.COMMAND_ERROR_PLAYER_NOT_FOUND.replace("{player}", args[0]));
      return;
    }

  }

}
