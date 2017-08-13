package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;


/**
 * Created by GlareMasters on 6/11/2017.
 */
public class CommandBugReport extends CommandBase {

  public CommandBugReport() {

    super("bugreport", Main.getInstance().getConfig().getString("commands.description.bugreport"),
        "guilds.command.bugreport", false, null, null, 0,
        0);
  }

  @Override
  public void execute(Player player, String[] args) {
    PluginDescriptionFile pdf = Main.getInstance().getDescription();
    player.sendMessage(ChatColor.GREEN + "Details:");
    player.sendMessage("");
    player
        .sendMessage(ChatColor.GREEN + "Plugin Version: " + ChatColor.GRAY + pdf.getVersion());
    player.sendMessage(ChatColor.GREEN + "Java Version: " + ChatColor.GRAY + System
        .getProperty("java.version"));
    player.sendMessage(ChatColor.GREEN + "Username: " + ChatColor.GRAY + player.getName());
    player.sendMessage("");
    player.sendMessage(ChatColor.GRAY
        + "When submitting a bug report, please include a screenshot of this information, along with the bug you are reporting. Clicking the following link will take you to the Guild's Information page where you can contact the developer - https://glaremasters.me/support/");
    player.sendMessage(
        ChatColor.AQUA + "Need faster support? Join our discord! https://discord.gg/pDT2ZBS");
  }
}
