package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class CommandVersion extends CommandBase {
    public CommandVersion() {
        super("version", "Check version", "guilds.command.version", true, new String[]{"ver"}, null, 0, 0);
    }

    public void execute(CommandSender sender, String[] args) {
        PluginDescriptionFile pdf = Main.getInstance().getDescription();
        sender.sendMessage(ChatColor.RED + "Guilds v" + pdf.getVersion() + ChatColor.GREEN + " by " + String.join(" & ", pdf.getAuthors()));
    }
}
