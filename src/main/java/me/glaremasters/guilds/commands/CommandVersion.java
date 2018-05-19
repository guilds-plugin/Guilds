package me.glaremasters.guilds.commands;

import static me.glaremasters.guilds.util.ColorUtil.color;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.updater.SpigotUpdater;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

public class CommandVersion extends CommandBase {

    public CommandVersion() {
        super("version", Guilds.getInstance().getConfig().getString("commands.description.version"),
                "guilds.command.version", true, new String[]{"ver", "v"},
                null, 0, 0);
    }


    public void execute(CommandSender sender, String[] args) {
        Guilds instance = Guilds.getInstance();
        SpigotUpdater updater = new SpigotUpdater(instance, 48920);
        PluginDescriptionFile pdf = instance.getDescription();
        try {
            sender.sendMessage(
                    color("&aPlugin Info:\n&aVersion - Current : " + pdf.getVersion()
                            + " Latest : "
                            + updater.getLatestVersion() + "\n&aAuthor - blockslayer22"
                            + "\n&aSupport - https://glaremasters.me/discord"));
        } catch (Exception e) {
            return;
        }
    }
}
