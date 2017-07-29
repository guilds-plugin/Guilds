package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import org.bukkit.entity.Player;

public class CommandCancel extends CommandBase {

  public CommandCancel() {
    super("cancel", "Cancel an action", "guilds.command.cancel", false, null, null, 0, 0);
  }

  public void execute(Player player, String[] args) {
    ConfirmAction action = Main.getInstance().getCommandHandler().getActions().get(player);
    if (action == null) {
      Message.sendMessage(player, Message.COMMAND_CANCEL_ERROR);
      return;
    }

    action.decline();
  }
}
