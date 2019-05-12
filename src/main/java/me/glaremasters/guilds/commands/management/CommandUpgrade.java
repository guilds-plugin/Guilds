/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.glaremasters.guilds.commands.management;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.configuration.sections.TierSettings;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.guild.GuildTier;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import me.glaremasters.guilds.utils.EconomyUtils;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/5/2019
 * Time: 10:38 PM
 */
@AllArgsConstructor @CommandAlias(Constants.ROOT_ALIAS)
public class CommandUpgrade extends BaseCommand {

    private GuildHandler guildHandler;
    private ActionHandler actionHandler;
    private SettingsManager settingsManager;
    private Permission permission;

    /**
     * Upgrade a guild
     * @param player the command executor
     * @param guild the guild being upgraded
     * @param role the player's role
     */
    @Subcommand("upgrade")
    @Description("{@@descriptions.upgrade}")
    @CommandPermission(Constants.BASE_PERM + "upgrade")
    public void execute(Player player, Guild guild, GuildRole role) {
        if (!role.isUpgradeGuild())
            ACFUtil.sneaky(new InvalidPermissionException());

        GuildTier tier = guildHandler.getGuildTier(guild.getTier().getLevel() + 1);
        double balance = guild.getBalance();
        double upgradeCost = tier.getCost();

        if (guildHandler.isMaxTier(guild))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.UPGRADE__TIER_MAX));

        if (guildHandler.memberCheck(guild))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.UPGRADE__NOT_ENOUGH_MEMBERS,
                    "{amount}", String.valueOf(tier.getMembersToRankup())));

        if (!EconomyUtils.hasEnough(balance, upgradeCost))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.UPGRADE__NOT_ENOUGH_MONEY, "{needed}", String.valueOf(upgradeCost - balance)));

        getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__MONEY_WARNING, "{amount}", String.valueOf(upgradeCost));

        actionHandler.addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                if (!EconomyUtils.hasEnough(balance, upgradeCost))
                    ACFUtil.sneaky(new ExpectationNotMet(Messages.UPGRADE__NOT_ENOUGH_MONEY, "{needed}", String.valueOf(upgradeCost - balance)));

                guild.setBalance(balance - upgradeCost);

                if (!settingsManager.getProperty(TierSettings.CARRY_OVER))
                    guildHandler.removePermsFromAll(permission, guild);


                guildHandler.upgradeTier(guild);

                guildHandler.addPermsToAll(permission, guild);

                getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__SUCCESS);
                actionHandler.removeAction(player);
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.UPGRADE__CANCEL);
                actionHandler.removeAction(player);
            }
        });

    }

}