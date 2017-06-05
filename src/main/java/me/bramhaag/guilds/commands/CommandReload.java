package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.message.Message;
import org.bukkit.command.CommandSender;

public class CommandReload extends CommandBase {


    public CommandReload() {
        super("reload", "Reload Guilds' configuration file", "guilds.command.reload", true, null, null, 0, 0);
    }

    public void execute(CommandSender sender, String[] args) {
        Main.getInstance().reloadConfig();
        Main.getInstance().setDatabaseType();

        Message.sendMessage(sender, Message.COMMAND_RELOAD_RELOADED);
    }
}
