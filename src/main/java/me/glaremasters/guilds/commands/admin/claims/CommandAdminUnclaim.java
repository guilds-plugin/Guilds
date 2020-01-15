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
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
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

/**
 * Created by Glare
 * Date: 5/29/2019
 * Time: 9:34 AM
 */
@CommandAlias("%guilds")
public class CommandAdminUnclaim extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private SettingsManager settingsManager;

    /**
     * Admin command to unclaim land for a guild
     * @param player the player running the command
     * @param guild the name of the guild that land is being unclaimed from
     */
    @Subcommand("admin unclaim")
    @Description("{@@descriptions.admin-unclaim}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("<%syntax>")
    public void execute(Player player, @Flags("admin") @Values("@guilds") Guild guild) {

        if (guild == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));
        }

        if (!ClaimUtils.isEnable(settingsManager)) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.CLAIM__HOOK_DISABLED));
        }

        WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

        if (!ClaimUtils.checkAlreadyExist(wrapper, guild)) {
            getCurrentCommandManager().getCommandIssuer(player).sendInfo(Messages.UNCLAIM__NOT_FOUND);
            return;
        }

        ClaimUtils.removeClaim(wrapper, guild);
        getCurrentCommandIssuer().sendInfo(Messages.UNCLAIM__SUCCESS);
    }

}