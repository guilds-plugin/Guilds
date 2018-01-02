package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import org.bukkit.entity.Player;

public class CommandConfirm extends CommandBase {

    public CommandConfirm() {
        super("confirm", Guilds.getInstance().getConfig().getString("commands.description.confirm"),
                "guilds.command.confirm", false, null, null, 0, 0);
    }

    public void execute(Player player, String[] args) {
        ConfirmAction action = Guilds.getInstance().getCommandHandler().getActions().get(player);

        if (action == null) {
            Message.sendMessage(player, Message.COMMAND_CONFIRM_ERROR);
            return;
        }

        action.accept();
    }
}
