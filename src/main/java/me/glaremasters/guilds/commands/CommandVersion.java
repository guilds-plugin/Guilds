package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class CommandVersion extends CommandBase {

  public CommandVersion() {
    super("version", Main.getInstance().getConfig().getString("commands.description.version"),
        "guilds.command.version", true, new String[]{"ver"},
        null, 0, 0);
  }

  public void execute(CommandSender sender, String[] args) {
    PluginDescriptionFile pdf = Main.getInstance().getDescription();
    sender.sendMessage(
        ChatColor.RED + "Version:");
    sender.sendMessage(
        ChatColor.RED + "Guilds v" + pdf.getVersion());
    sender.sendMessage("");
    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&bCredits:"));
    sender.sendMessage(
        ChatColor.translateAlternateColorCodes('&', "&bProject Manager: &aBlockslayer22"));
    sender.sendMessage(ChatColor
        .translateAlternateColorCodes('&', "&bPast Co-Developers: &aMrFantasty & Bramhaag"));
    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
        "&bOther Helpers: &aIfna_Try_, RubbaBoy, Tom1024, & Redrield"));
  }
}
