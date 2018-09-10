package me.glaremasters.guilds.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

import static me.glaremasters.guilds.utils.ConfigUtils.color;

/**
 * Created by GlareMasters
 * Date: 9/10/2018
 * Time: 6:43 PM
 */
@CommandAlias("guild|guilds")
public class CommandBank extends BaseCommand {

    private Guilds guilds;

    public CommandBank(Guilds guilds) {
        this.guilds = guilds;
    }

    @Subcommand("bank balance")
    @Description("Get the balance of your Guild's Bank")
    @CommandPermission("guilds.command.bank")
    public void onBalance(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        Double balance = guild.getBalance();
        player.sendMessage(color(String.valueOf(balance)));
    }

    @Subcommand("bank deposit")
    @Description("Put money in your Guild bank")
    @CommandPermission("guilds.command.bank")
    @Syntax("<amount>")
    public void onDeposit(Player player, String amount) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        Double balance = guild.getBalance();
        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            return;
        }
        guild.updateBalance(balance + Double.valueOf(amount));
    }

    @Subcommand("bank withdraw")
    @Description("Take money from your Guild bank")
    @CommandPermission("guilds.command.bank")
    @Syntax("<amount>")
    public void onWithdraw(Player player, String amount) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        Double balance = guild.getBalance();
        try {
            Double.parseDouble(amount);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            return;
        }
        if ((guild.getBalance() < Double.parseDouble(amount))) return;
        guild.updateBalance(balance - Double.valueOf(amount));
    }

}
