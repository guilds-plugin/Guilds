package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.updater.Updater;
import org.bukkit.command.CommandSender;

public class CommandUpdate extends CommandBase {

    public CommandUpdate() {
        super("update", Main.getInstance().getConfig().getString("commands.description.update"),
                "guilds.command.update",
                true, null, null, 0, 0);
    }

    public void execute(CommandSender sender, String[] args) {
        Updater.checkForUpdates((result, exception) -> {
            if (result != null) {
                Message.sendMessage(sender, Message.COMMAND_UPDATE_FOUND.replace("{url}", result));
            } else {
                Message.sendMessage(sender, Message.COMMAND_UPDATE_NOT_FOUND);
            }
        });
    }
}
