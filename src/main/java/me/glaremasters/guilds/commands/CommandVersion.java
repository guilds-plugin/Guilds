package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.updater.SpigotUpdater;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import static me.glaremasters.guilds.utils.ConfigUtils.color;

/**
 * Created by GlareMasters
 * Date: 7/19/2018
 * Time: 5:02 PM
 */
public class CommandVersion extends CommandBase {

    private Guilds guilds;

    public CommandVersion(Guilds guilds) {
        super(guilds, "version", true, new String[]{"ver", "v"}, null, 0, 0);
        this.guilds = guilds;
    }

    public void execute(CommandSender sender, String[] args) {
        Guilds guilds = Guilds.getGuilds();
        SpigotUpdater updater = new SpigotUpdater(guilds, 48920);
        PluginDescriptionFile pdf = guilds.getDescription();
        try {
            String message;
            if (updater.getLatestVersion().equalsIgnoreCase(pdf.getVersion())) {
                message = "";
            } else {
                message = "\n&8» &7An update has been found! &f- " + updater.getResourceURL();
            }
            sender.sendMessage(
                    color("&8&m--------------------------------------------------"
                            + "\n&8» &7Name - &a"
                            + pdf.getName() + "\n&8» &7Version - &a" + pdf.getVersion()
                            + "\n&8» &7Author - &a" + pdf.getAuthors() + "\n&8» &7Support - &a"
                            + pdf.getWebsite() + message
                            + "\n&8&m--------------------------------------------------"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
