/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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

package me.glaremasters.guilds.commands.admin.claims;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.ClaimUtils;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

@CommandAlias("%guilds")
public class CommandAdminClaim extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private SettingsManager settingsManager;

    /**
     * Admin command to claim land for a guild
     * @param player the player executing
     * @param guild the guild land is being claimed for
     */
    @Subcommand("admin claim")
    @Description("{@@descriptions.admin-claim}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("<name>")
    public void execute(Player player, @Flags("admin") @Values("@guilds") Guild guild) {

        if (guild == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));
        }

        if (!ClaimUtils.isEnable(settingsManager)) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED));
        }

        WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

        if (ClaimUtils.checkAlreadyExist(wrapper, guild)) {
            getCurrentCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__ALREADY_EXISTS);
            return;
        }

        if (ClaimUtils.checkOverlap(wrapper, player, settingsManager)) {
            getCurrentCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__OVERLAP);
            return;
        }

        ClaimUtils.createClaim(wrapper, guild, player, settingsManager);

        ClaimUtils.getGuildClaim(wrapper, player, guild).ifPresent(region -> {
            ClaimUtils.addOwner(region, guild);
            ClaimUtils.addMembers(region, guild);
            ClaimUtils.setEnterMessage(wrapper, region, settingsManager, guild);
            ClaimUtils.setExitMessage(wrapper, region, settingsManager, guild);
        });

        getCurrentCommandIssuer().sendInfo(Messages.CLAIM__SUCCESS,
                "{loc1}", ACFBukkitUtil.formatLocation(ClaimUtils.claimPointOne(player, settingsManager)),
                "{loc2}", ACFBukkitUtil.formatLocation(ClaimUtils.claimPointTwo(player, settingsManager)));
    }

}