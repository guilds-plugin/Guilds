package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

import static me.glaremasters.guilds.utils.ConfigUtils.color;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 10:22 PM
 */
public class CommandBank extends CommandBase {

    public CommandBank() {
        super("bank", "",
                "guilds.command.bank", false, null,
                "deposit <amount> | withdraw <amount> | balance", 1, 2);
    }

    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        Double balance = guild.getBalance();
        if (args[0].equalsIgnoreCase("balance")) {
            player.sendMessage(color(String.valueOf(balance)));
        }
        if (args[0].equalsIgnoreCase("deposit")) {
            if (args.length != 2) return;
            try {
                Double.parseDouble(args[1]);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
            guild.updateBalance(balance + Double.valueOf(args[1]));
        }
        if (args[0].equalsIgnoreCase("withdraw")) {
            if (args.length != 2) return;
            try {
                Double.parseDouble(args[1]);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
            if ((guild.getBalance() < Double.parseDouble(args[1]))) return;
            guild.updateBalance(balance - Double.valueOf(args[1]));
        }
    }

}
