package me.glaremasters.guilds.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 9/10/2018
 * Time: 6:43 PM
 */
@CommandAlias("guild|guilds")
public class CommandBank extends BaseCommand {

    @Dependency private Guilds guilds;

    /**
     * Check the bank balance of a Guild
     * @param player the player to check
     * @param guild the guild they are in
     */
    @Subcommand("bank balance")
    @Description("{@@descriptions.bank-balance}")
    @CommandPermission("guilds.command.bank")
    public void onBalance(Player player, Guild guild) {
        getCurrentCommandIssuer().sendInfo(Messages.BANK__BALANCE, "{amount}", String.valueOf(guild.getBalance()));
    }

    /**
     * Deposit money into the bank
     * @param player the player to check
     * @param guild the guild they are in
     * @param role the role of the player
     * @param amount the amount being put into the bank
     */
    @Subcommand("bank deposit")
    @Description("{@@descriptions.bank-deposit}")
    @CommandPermission("guilds.command.bank")
    @Syntax("<amount>")
    public void onDeposit(Player player, Guild guild, GuildRole role, Double amount) {
        if (!role.canDepositMoney()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        if (amount < 0) return;

        if (guilds.getEconomy().getBalance(player) < amount) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__NOT_ENOUGH_MONEY);
            return;
        }
        guilds.getEconomy().withdrawPlayer(player, amount);
        Double balance = guild.getBalance();
        guild.updateBalance(balance + amount);
        getCurrentCommandIssuer().sendInfo(Messages.BANK__DEPOSIT_SUCCESS, "{amount}", String.valueOf(amount), "{total}", String.valueOf(guild.getBalance()));
    }

    /**
     * Take money from the bank
     * @param player the player to check
     * @param guild the guild they are in
     * @param role the role of the player
     * @param amount the amount being taken out
     */
    @Subcommand("bank withdraw")
    @Description("{@@descriptions.bank-withdraw}")
    @CommandPermission("guilds.command.bank")
    @Syntax("<amount>")
    public void onWithdraw(Player player, Guild guild, GuildRole role, Double amount) {
        if (!role.canWithdrawMoney()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        if (amount < 0) return;

        Double balance = guild.getBalance();
        if ((balance < amount)) {
            getCurrentCommandIssuer().sendInfo(Messages.BANK__NOT_ENOUGH_BANK);
            return;
        }
        guild.updateBalance(balance - amount);
        getCurrentCommandIssuer().sendInfo(Messages.BANK__WITHDRAWL_SUCCESS, "{amount}", String.valueOf(amount), "{total}", String.valueOf(guild.getBalance()));
    }

}
