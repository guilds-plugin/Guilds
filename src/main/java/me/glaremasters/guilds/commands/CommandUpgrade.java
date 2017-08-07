package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 8/7/2017.
 */
public class CommandUpgrade extends CommandBase {

  public CommandUpgrade() {
    super("upgrade", "Upgrade your guild to a higher level!", "guilds.command.tier", false, null,
        null, 0, 0);
  }

  @Override
  public void execute(Player player, String[] args) {
    Guild guild = Guild.getGuild(player.getUniqueId());

    GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
    if (!role.canUpgradeGuild()) {
      Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
      return;
    }
  }

}
