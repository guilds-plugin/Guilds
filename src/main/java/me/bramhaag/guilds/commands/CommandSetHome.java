package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.message.Message;
import org.bukkit.entity.Player;

import java.io.IOException;

/**
 * Created by GlareMasters on 6/11/2017.
 */
public class CommandSetHome extends CommandBase {

    public CommandSetHome() {
        super("sethome", "Set your guild's home!", "guilds.command.sethome", false, null, null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {
        Main.getInstance().guildhomesconfig.set(player.getWorld().getName() + "." + Main.getInstance().getGuildHandler() + "." + "X", player.getLocation().getBlockX());
        Main.getInstance().guildhomesconfig.set(player.getWorld().getName() + "." + player.getName() + "." + "Y", player.getLocation().getBlockY());
        Main.getInstance().guildhomesconfig.set(player.getWorld().getName() + "." + player.getName() + "." + "Z", player.getLocation().getBlockZ());

        try {
            Main.getInstance().guildhomesconfig.save(Main.getInstance().guildhomes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Message.sendMessage(player, Message.COMMAND_CREATE_GUILD_HOME);
    }
}
