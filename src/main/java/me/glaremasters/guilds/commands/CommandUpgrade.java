package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.handlers.TitleHandler;
import me.glaremasters.guilds.message.Message;
import me.glaremasters.guilds.util.ConfirmAction;
import org.bukkit.Bukkit;
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
        final FileConfiguration c = Guilds.getInstance().getConfig();
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

        if (guild.getTier() >= c.getInt("max-number-of-tiers")) {
            Message.sendMessage(player, Message.COMMAND_UPGRADE_TIER_MAX);
            return;
        }

        if (guild.getMemebersRankupRequire() != 0 && guild.getMembers().size() < guild
                .getMemebersRankupRequire()) {
            Message.sendMessage(player, Message.COMMAND_UPGRADE_NOT_ENOUGH_MEMBERS
                    .replace("{amount}", String.valueOf(guild.getMemebersRankupRequire())));
            return;
        }
        double balance = guild.getBankBalance();
        double tierUpgradeCost = guild.getTierCost();
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
                if (!c.getBoolean("carry-over-perms")) {
                    guild.getMembers().stream()
                            .map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()))
                            .forEach(member -> {
                                for (String perms : guild.getGuildPerms()) {
                                    Guilds.getPermissions().playerRemove(null, member, perms);
                                }
                            });
                }
                Guilds.getInstance().guildTiersConfig.set(guild.getName(), tier + 1);
                Guilds.getInstance().guildBanksConfig
                        .set(guild.getName(), balance - tierUpgradeCost);
                Guilds.getInstance().saveGuildData();
                guild.getMembers().stream()
                        .map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()))
                        .forEach(member -> {
                            for (String perms : guild.getGuildPerms()) {
                                Guilds.getPermissions().playerAdd(null, member, perms);
                            }
                        });
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

