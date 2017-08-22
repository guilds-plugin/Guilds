package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters on 8/7/2017.
 */
public class CommandUpgrade extends CommandBase {

    public CommandUpgrade() {
        super("upgrade", Main.getInstance().getConfig().getString("commands.description.upgrade"),
                "guilds.command.upgrade", false, null,
                null, 0, 0);
    }

    @Override
    public void execute(Player player, String[] args) {
        final FileConfiguration config = Main.getInstance().getConfig();
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

        if (guild.getTier() >= Main.getInstance().getConfig().getInt("max-number-of-tiers")) {
            Message.sendMessage(player, Message.COMMAND_UPGRADE_TIER_MAX);
            return;
        }

        double tierUpgradeCost = guild.getTierCost();

        if (Main.vault && tierUpgradeCost != -1) {
            if (Main.getInstance().getEconomy().getBalance(player) < tierUpgradeCost) {
                Message.sendMessage(player, Message.COMMAND_ERROR_NOT_ENOUGH_MONEY);
                return;
            }

            Message.sendMessage(player, Message.COMMAND_UPGRADE_MONEY_WARNING
                    .replace("{amount}", String.valueOf(tierUpgradeCost)));
        } else {
            Message.sendMessage(player, Message.COMMAND_CREATE_WARNING);
        }

        Main.getInstance().getCommandHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                Message.sendMessage(player, Message.COMMAND_UPGRADE_SUCCESS);
                Main.getInstance().guildTiersConfig.set(guild.getName(), tier + 1);
                Main.getInstance().saveGuildTiers();
                EconomyResponse response =
                        Main.getInstance().getEconomy().withdrawPlayer(player, tierUpgradeCost);
                if (!response.transactionSuccess()) {
                    Message.sendMessage(player, Message.COMMAND_UPGRADE_NOT_ENOUGH_MONEY);
                    return;
                }
                if (config.getBoolean("titles.enabled")) {
                    try {
                        String creation = "titles.events.guild-tier-upgrade";
                        guild.sendTitle(ChatColor
                                        .translateAlternateColorCodes('&',
                                                config.getString(creation + ".title").replace("{tier}",
                                                        Integer.toString(guild.getTier()))),
                                ChatColor.translateAlternateColorCodes('&',
                                        config.getString(creation + ".sub-title").replace("{tier}",
                                                Integer.toString(guild.getTier()))),
                                config.getInt(creation + ".fade-in") * 20,
                                config.getInt(creation + ".stay") * 20,
                                config.getInt(creation + ".fade-out") * 20);
                    } catch (NoSuchMethodError error) {
                        String creation = "titles.events.guild-tier-upgrade";
                        guild.sendTitleOld(ChatColor.translateAlternateColorCodes('&',
                                config.getString(creation + ".title")
                                        .replace("{tier}", Integer.toString(guild.getTier()))),
                                ChatColor.translateAlternateColorCodes('&',
                                        config.getString(creation + ".sub-title")
                                                .replace("{tier}",
                                                        Integer.toString(guild.getTier()))));
                    }

                }
                guild.updateGuild("");
            }


            @Override
            public void decline() {
                Message.sendMessage(player, Message.COMMAND_UPGRADE_CANCEL);
                Main.getInstance().getCommandHandler().removeAction(player);
            }
        });


    }

}
