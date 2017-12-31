package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 8/19/2017.
 */
public class CommandBank extends CommandBase {


    public CommandBank() {
        super("bank", Main.getInstance().getConfig().getString("commands.description.bank"),
                "guilds.command.bank", false,
                null, "deposit <amount> | withdraw <amount> | balance", 1, 2);
    }

    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        double balance = guild.getBankBalance();

        if (args[0].equalsIgnoreCase("balance")) {
            Message.sendMessage(player,
                    Message.COMMAND_BANK_BALANCE.replace("{amount}", Double.toString(balance)));
        }

        if (args[0].equalsIgnoreCase("deposit")) {
            GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
            if (!role.canDepositMoney()) {
                Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
                return;
            }
            if (args.length != 2) {
                Message.sendMessage(player, Message.COMMAND_ERROR_ARGS);
                return;
            }

            try {
                Double.parseDouble(args[1]);
            } catch (NumberFormatException exception) {
                Message.sendMessage(player, Message.COMMAND_ERROR_ARGS);
                return;
            }

            if (balance + Double.valueOf(args[1]) > guild.getMaxBankBalance()) {
                Message.sendMessage(player, Message.COMMAND_BANK_BANK_BALANCE_LIMIT);
                return;
            }
            if (Main.getInstance().getEconomy().getBalance(player.getName()) < Double
                    .valueOf(args[1])) {
                Message.sendMessage(player, Message.COMMAND_BANK_DEPOSIT_FAILURE);
                return;
            }

            EconomyResponse response =
                    Main.getInstance().getEconomy().withdrawPlayer(player, Double.valueOf(args[1]));
            if (!response.transactionSuccess()) {
                Message.sendMessage(player, Message.COMMAND_BANK_DEPOSIT_FAILURE);
                return;
            }

            Main.getInstance().guildBanksConfig
                    .set(guild.getName(), balance + Double.valueOf(args[1]));
            Message.sendMessage(player, Message.COMMAND_BANK_DEPOSIT_SUCCESS
                    .replace("{amount}", String.valueOf(Double.valueOf(args[1])), "{balance}",
                            String.valueOf(balance + Double.valueOf(args[1]))));

            Main.getInstance().saveGuildData();
            guild.updateGuild("");

        }

        if (args[0].equalsIgnoreCase("withdraw")) {
            GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
            if (!role.canWithdrawMoney()) {
                Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
                return;
            }
            if (args.length != 2) {
                Message.sendMessage(player, Message.COMMAND_ERROR_ARGS);
                return;
            }

            try {
                Double.parseDouble(args[1]);
            } catch (NumberFormatException exception) {
                Message.sendMessage(player, Message.COMMAND_ERROR_ARGS);
                return;
            }

            if (balance < Double.valueOf(args[1])) {
                Message.sendMessage(player, Message.COMMAND_BANK_WITHDRAW_FAILURE);
                return;
            }

            Message.sendMessage(player, Message.COMMAND_BANK_WITHDRAW_SUCCESS
                    .replace("{amount}", String.valueOf(Double.valueOf(args[1])), "{balance}",
                            String.valueOf(balance - Double.valueOf(args[1]))));

            EconomyResponse response =
                    Main.getInstance().getEconomy().depositPlayer(player, Double.valueOf(args[1]));
            if (!response.transactionSuccess()) {
                Message.sendMessage(player, Message.COMMAND_BANK_WITHDRAW_FAILURE);
                return;
            }

            Main.getInstance().guildBanksConfig
                    .set(guild.getName(), balance - Double.valueOf(args[1]));
            Main.getInstance().saveGuildData();
            guild.updateGuild("");
        }


    }

}


