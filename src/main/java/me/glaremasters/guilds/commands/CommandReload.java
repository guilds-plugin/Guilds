package me.glaremasters.guilds.commands;

import java.io.IOException;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;

public class CommandReload extends CommandBase {


    public CommandReload() {
        super("reload", Guilds.getInstance().getConfig().getString("commands.description.reload"),
                "guilds.command.reload", true, null,
                null, 0, 0);
    }

    public void execute(CommandSender sender, String[] args) {
        try {
            Guilds.getInstance().yaml.load(Guilds.getInstance().languageYamlFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        Guilds.getInstance().reloadConfig();
        Guilds.getInstance().setDatabaseType();
        Guilds.getInstance().getGuildHandler().disable();
        Guilds.getInstance().getGuildHandler().enable();

        Guilds.PREFIX =
                ChatColor.translateAlternateColorCodes('&',
                        Guilds.getInstance().getConfig().getString("plugin-prefix"))
                        + ChatColor.RESET + " ";
        Message.sendMessage(sender, Message.COMMAND_RELOAD_RELOADED);
    }
}
