package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.api.events.GuildCreateEvent;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class CommandCreate extends CommandBase {

  public CommandCreate() {
    super("create", "Create a guild", "guilds.command.create", false, new String[]{"c"},
        "<name>", 1, 1);
  }

  @Override
  public void execute(Player player, String[] args) {

    if (Guild.getGuild(player.getUniqueId()) != null) {
      Message.sendMessage(player, Message.COMMAND_ERROR_ALREADY_IN_GUILD);
      return;
    }

    int minLength = Main.getInstance().getConfig().getInt("name.min-length");
    int maxLength = Main.getInstance().getConfig().getInt("name.max-length");
    String regex = Main.getInstance().getConfig().getString("name.regex");

    if (args[0].length() < minLength || args[0].length() > maxLength || !args[0]
        .matches(regex)) {
      Message.sendMessage(player, Message.COMMAND_CREATE_NAME_REQUIREMENTS);
      return;
    }

    for (String name : Main.getInstance().getGuildHandler().getGuilds().keySet()) {
      if (name.equalsIgnoreCase(args[0])) {
        Message.sendMessage(player, Message.COMMAND_CREATE_GUILD_NAME_TAKEN);
        return;
      }
    }

    double requiredMoney = Main.getInstance().getConfig().getDouble("Requirement.cost");

    if (Main.vault && requiredMoney != -1) {
      if (Main.getInstance().getEconomy().getBalance(player) < requiredMoney) {
        Message.sendMessage(player, Message.COMMAND_ERROR_NOT_ENOUGH_MONEY);
        return;
      }

      Message.sendMessage(player, Message.COMMAND_CREATE_MONEY_WARNING
          .replace("{amount}", String.valueOf(requiredMoney)));
    } else {
      Message.sendMessage(player, Message.COMMAND_CREATE_WARNING);
    }

    Main.getInstance().getCommandHandler().addAction(player, new ConfirmAction() {
      @Override
      public void accept() {
        Guild guild = new Guild(args[0], player.getUniqueId());

        GuildCreateEvent event = new GuildCreateEvent(player, guild);
        if (event.isCancelled()) {
          return;
        }

        if (Main.getInstance().getConfig().getBoolean("require-money")) {

          EconomyResponse response =
              Main.getInstance().getEconomy().withdrawPlayer(player, requiredMoney);
          if (!response.transactionSuccess()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NOT_ENOUGH_MONEY);
            return;
          }
        }

        Main.getInstance().getDatabaseProvider().createGuild(guild, (result, exception) -> {
          if (result) {
            Message.sendMessage(player,
                Message.COMMAND_CREATE_SUCCESSFUL.replace("{guild}", args[0]));

            Main.getInstance().getScoreboardHandler().update();
            Main.getInstance().getScoreboardHandler().show(player);
            Main.getInstance().guildstatusconfig.set(guild.getName(), "Private");
            Main.getInstance().saveGuildstatus();
            player.setPlayerListName(
                ChatColor.translateAlternateColorCodes('&',
                    Main.getInstance().getConfig().getString("tablist")
                        .replace("{guild}", guild.getName()).replace("{prefix}", guild.getPrefix())
                        + player.getName()));
          } else {
            Message.sendMessage(player, Message.COMMAND_CREATE_ERROR);

            Main.getInstance().getLogger().log(Level.SEVERE, String.format(
                "An error occurred while player '%s' was trying to create guild '%s'",
                player.getName(), args[0]));
            if (exception != null) {
              exception.printStackTrace();
            }
          }
        });

        Main.getInstance().getCommandHandler().removeAction(player);
      }

      @Override
      public void decline() {
        Message.sendMessage(player, Message.COMMAND_CREATE_CANCELLED);
        Main.getInstance().getCommandHandler().removeAction(player);
      }
    });
  }
}
