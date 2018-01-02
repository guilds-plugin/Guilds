package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.handlers.TitleHandler;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 8/7/2017.
 */
public class CommandUpgrade extends CommandBase {

    public CommandUpgrade() {
        super("upgrade", Guilds.getInstance().getConfig().getString("commands.description.upgrade"),
                "guilds.command.upgrade", false, null,
                null, 0, 0);
    }

    TitleHandler TitleHandler = new TitleHandler(Guilds.getInstance());

    @Override
    public void execute(Player player, String[] args) {
        final FileConfiguration config = Guilds.getInstance().getConfig();
        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }

        int tier = guild.getTier();
        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canUpgradeGuild()) {
            Message.sendMessage(player, Message.COMMAND_ERROR_ROLE_NO_PERMISSION);
            return;
        }

        if (guild.getTier() >= Guilds.getInstance().getConfig().getInt("max-number-of-tiers")) {
            Message.sendMessage(player, Message.COMMAND_UPGRADE_TIER_MAX);
            return;
        }
        double balance = guild.getBankBalance();
        double tierUpgradeCost = guild.getTierCost();
        if (Guilds.getInstance().getConfig().getBoolean("use-bank-balance")) {
            if (balance < tierUpgradeCost) {
                double needed = (tierUpgradeCost - balance);
                Message.sendMessage(player, Message.COMMAND_UPGRADE_NOT_ENOUGH_MONEY
                        .replace("{needed}", Double.toString(needed)));
                return;
            }
            Message.sendMessage(player, Message.COMMAND_UPGRADE_MONEY_WARNING
                    .replace("{amount}", String.valueOf(tierUpgradeCost)));
            Guilds.getInstance().getCommandHandler().addAction(player, new ConfirmAction() {
                @Override
                public void accept() {
                    if (balance < tierUpgradeCost) {
                        double needed = (tierUpgradeCost - balance);
                        Message.sendMessage(player, Message.COMMAND_UPGRADE_NOT_ENOUGH_MONEY
                                .replace("{needed}", Double.toString(needed)));
                        return;
                    }
                    Message.sendMessage(player, Message.COMMAND_UPGRADE_SUCCESS);
                    Guilds.getInstance().guildTiersConfig.set(guild.getName(), tier + 1);
                    Guilds.getInstance().guildBanksConfig
                            .set(guild.getName(), balance - tierUpgradeCost);
                    Guilds.getInstance().saveGuildData();
                    TitleHandler.tierTitles(player);
                    guild.updateGuild("");
                }


                @Override
                public void decline() {
                    Message.sendMessage(player, Message.COMMAND_UPGRADE_CANCEL);
                    Guilds.getInstance().getCommandHandler().removeAction(player);
                }
            });
        } else {
            if (Guilds.vault && tierUpgradeCost != -1) {
                if (Guilds.getInstance().getEconomy().getBalance(player) < tierUpgradeCost) {
                    double needed = (tierUpgradeCost - Guilds.getInstance().getEconomy()
                            .getBalance(player));
                    Message.sendMessage(player, Message.COMMAND_UPGRADE_NOT_ENOUGH_MONEY
                            .replace("{needed}", Double.toString(needed)));
                    return;
                }

                Message.sendMessage(player, Message.COMMAND_UPGRADE_MONEY_WARNING
                        .replace("{amount}", String.valueOf(tierUpgradeCost)));
            } else {
                Message.sendMessage(player, Message.COMMAND_CREATE_WARNING);
            }

            Guilds.getInstance().getCommandHandler().addAction(player, new ConfirmAction() {
                @Override
                public void accept() {
                    if (Guilds.getInstance().getEconomy().getBalance(player) < tierUpgradeCost) {
                        Message.sendMessage(player, Message.COMMAND_UPGRADE_NOT_ENOUGH_MONEY);
                        return;
                    }
                    Message.sendMessage(player, Message.COMMAND_UPGRADE_SUCCESS);
                    Guilds.getInstance().guildTiersConfig.set(guild.getName(), tier + 1);
                    Guilds.getInstance().saveGuildData();
                    EconomyResponse response =
                            Guilds.getInstance().getEconomy().withdrawPlayer(player, tierUpgradeCost);
                    if (!response.transactionSuccess()) {
                        Message.sendMessage(player, Message.COMMAND_UPGRADE_NOT_ENOUGH_MONEY);
                        return;
                    }

                    TitleHandler.tierTitles(player);
                    guild.updateGuild("");
                }


                @Override
                public void decline() {
                    Message.sendMessage(player, Message.COMMAND_UPGRADE_CANCEL);
                    Guilds.getInstance().getCommandHandler().removeAction(player);
                }
            });


        }
    }
}
