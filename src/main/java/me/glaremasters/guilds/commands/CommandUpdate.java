package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.updater.SpigotUpdater;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandUpdate extends CommandBase {

    public CommandUpdate() {
        super("update", Guilds.getInstance().getConfig().getString("commands.description.update"),
                "guilds.command.update",
                true, null, null, 0, 0);
    }

    public void execute(CommandSender sender, String[] args) {
        SpigotUpdater updater = new SpigotUpdater(Guilds.getInstance(), 48920);
        try {
            if (updater.checkForUpdates()) {
                sender.sendMessage(ChatColor.GREEN + "An update was found! " + ChatColor.BLUE + "New version: " + updater.getLatestVersion()
                        + ChatColor.GREEN + " download: " + updater.getResourceURL());
            }
        } catch (Exception e) {
            sender.sendMessage("Could not check for updates!");
            e.printStackTrace();
        }
    }
}

