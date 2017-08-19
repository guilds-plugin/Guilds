package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 8/19/2017.
 */
public class CommandBank extends CommandBase {


  public CommandBank() {
    super("bank", Main.getInstance().getConfig().getString("commands.description.bank"),
        "guilds.command.bank", false,
        null, "deposit <amount> | withdraw <amount> | balance", 1, 2);
  }

  public void execute(Player player, String[] args) {
    Guild guild = Guild.getGuild(player.getUniqueId());
    if (guild == null) {
      Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
      return;
    }
    int balance = guild.getBankBalance();

    if (args[0].equalsIgnoreCase("balance")) {
      player.sendMessage(
          "Hey " + player.getName() + ", your guild: " + guild.getName() + " has a balance of: "
              + Integer.toString(balance));
    }

    if (args[0].equalsIgnoreCase("deposit")) {
      if (args.length != 2) {
        return;
      }
      if (Main.getInstance().getEconomy().getBalance(player.getName()) < Double.valueOf(args[1])) {
        Message.sendMessage(player, Message.COMMAND_UPGRADE_NOT_ENOUGH_MONEY);
        return;
      }

      EconomyResponse response =
          Main.getInstance().getEconomy().withdrawPlayer(player, Double.valueOf(args[1]));
      if (!response.transactionSuccess()) {
        Message.sendMessage(player, Message.COMMAND_UPGRADE_NOT_ENOUGH_MONEY);
        return;
      }

      Main.getInstance().guildBanksConfig.set(guild.getName(), balance + Integer.valueOf(args[1]));
      player.sendMessage("You have just moved $" + Double.valueOf(args[1])
          + " from your account into your guild bank!");

      Main.getInstance().saveGuildBanks();

    }

    if(args[0].equalsIgnoreCase("withdraw")) {
      if (args.length != 2) {
        return;
      }
      if (balance < Double.valueOf(args[1])) {
        Message.sendMessage(player, Message.COMMAND_UPGRADE_NOT_ENOUGH_MONEY);
        return;
      }

      EconomyResponse response =
          Main.getInstance().getEconomy().depositPlayer(player, Double.valueOf(args[1]));
      if (!response.transactionSuccess()) {
        Message.sendMessage(player, Message.COMMAND_UPGRADE_NOT_ENOUGH_MONEY);
        return;
      }


    }


  }

}


