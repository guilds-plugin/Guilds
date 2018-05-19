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
            String message;
            if (updater.getLatestVersion().equalsIgnoreCase(pdf.getVersion())) {
                message = null;
            }
            else {
                message = "";
            }
            sender.sendMessage(
                    color("&aPlugin Info:\n&aName - " + pdf.getName() + "\n&aVersion - Current : " + pdf.getVersion()
                            + " Latest : "
                            + updater.getLatestVersion() + "\n&aAuthor - " + pdf.getAuthors()
                            + "\n&aSupport - " + pdf.getWebsite() + message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
