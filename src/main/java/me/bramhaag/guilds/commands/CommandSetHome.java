package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.message.Message;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 6/11/2017.
 */
public class CommandSetHome extends CommandBase {

    public CommandSetHome() {
        super("sethome", "Set your guild's home!", "guilds.command.sethome", false, null, null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {

        String world = player.getWorld().getName();
        double xloc = player.getLocation().getX();
        double yloc = player.getLocation().getY();
        double zloc = player.getLocation().getZ();
        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();

        Main.getInstance().guildhomesconfig.set(Guild.getGuild(player.getUniqueId()).getName(), world + ":" + xloc + ":" + yloc + ":" + zloc + ":" + yaw + ":" + pitch);
        Main.getInstance().saveGuildhomes();
        Message.sendMessage(player, Message.COMMAND_CREATE_GUILD_HOME);
    }
}