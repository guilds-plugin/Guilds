package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;

public class CommandReload extends CommandBase {


  public CommandReload() {
    super("reload", Main.getInstance().getConfig().getString("commands.description.reload"),
        "guilds.command.reload", true, null,
        null, 0, 0);
  }

  public void execute(CommandSender sender, String[] args) {
    try {
      Main.getInstance().yaml.load(Main.getInstance().languageYamlFile);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InvalidConfigurationException e) {
      e.printStackTrace();
    }
    Main.getInstance().reloadConfig();
    Main.getInstance().setDatabaseType();
    Main.getInstance().getGuildHandler().disable();
    Main.getInstance().getGuildHandler().enable();

    Main.PREFIX =
        ChatColor.translateAlternateColorCodes('&',
            Main.getInstance().getConfig().getString("plugin-prefix"))
            + ChatColor.RESET + " ";
    Message.sendMessage(sender, Message.COMMAND_RELOAD_RELOADED);
  }
}
