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

    @Dependency private Guilds guilds;

    @Subcommand("bank balance")
    @Description("{@@descriptions.bank-balance}")
    @CommandPermission("guilds.command.bank")
    public void onBalance(Player player, Guild guild) {
        Double balance = guild.getBalance();
        player.sendMessage(color(String.valueOf(balance)));
    }

    @Subcommand("bank deposit")
    @Description("{@@descriptions.bank-deposit}")
    @CommandPermission("guilds.command.bank")
    @Syntax("<amount>")
    public void onDeposit(Player player, Guild guild, Double amount) {
        Double balance = guild.getBalance();
        guild.updateBalance(balance + amount);
    }

    @Subcommand("bank withdraw")
    @Description("{@@descriptions.bank-withdraw}")
    @CommandPermission("guilds.command.bank")
    @Syntax("<amount>")
    public void onWithdraw(Player player, Guild guild, Double amount) {
        Double balance = guild.getBalance();
        if ((guild.getBalance() < amount)) return;
        guild.updateBalance(Math.min(balance, amount));
    }

}
