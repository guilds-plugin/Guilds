package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * Created by GlareMasters on 7/24/2017.
 */
public class CommandBuff extends CommandBase{
  Inventory buff = Bukkit.createInventory(null, 9, "Guild Buffs");
  public CommandBuff() {
    super("buff", "Buy buffs for your guild!", "guilds.command.buff", false,
        null, null, 0, 0);
  }

  public void execute(Player player, String[] args) {
    Guild guild = Guild.getGuild(player.getUniqueId());
    if (guild == null) {
      Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
      return;
    }
    player.openInventory(buff);
  }
}
