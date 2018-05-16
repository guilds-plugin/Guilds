package me.glaremasters.guilds.commands;

import static me.glaremasters.guilds.util.ColorUtil.color;
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
        Guilds instance = Guilds.getInstance();
        try {
            instance.yaml.load(instance.languageYamlFile);
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
        instance.reloadConfig();
        instance.setDatabaseType();
        instance.getGuildHandler().disable();
        instance.getGuildHandler().enable();

        Guilds.PREFIX = color(instance.getConfig().getString("plugin-prefix")) + ChatColor.RESET + " ";
        Message.sendMessage(sender, Message.COMMAND_RELOAD_RELOADED);
    }
}
