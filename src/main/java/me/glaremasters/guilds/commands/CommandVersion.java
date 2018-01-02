package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class CommandVersion extends CommandBase {

    public CommandVersion() {
        super("version", Guilds.getInstance().getConfig().getString("commands.description.version"),
                "guilds.command.version", true, new String[]{"ver"},
                null, 0, 0);
    }

    public void execute(CommandSender sender, String[] args) {
        PluginDescriptionFile pdf = Guilds.getInstance().getDescription();
        sender.sendMessage(
                ChatColor.RED + "Guilds v" + pdf.getVersion() + ChatColor.GREEN + " by " + String
                        .join(" & ", pdf.getAuthors()));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.AQUA + "The full team behind this project can be found here https://glaremasters.me/team/");
    }
}
