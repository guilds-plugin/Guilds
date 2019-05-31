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

package me.glaremasters.guilds.commands.admin.motd;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 5/22/2019
 * Time: 11:06 PM
 */
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandAdminMotdSet extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private SettingsManager settingsManager;

    /**
     * Set the MOTD of a guild
     * @param player the player running the command
     * @param guild the guild to modify
     * @param motd the new motd for the guild
     */
    @Subcommand("admin motd set")
    @Description("{@@descriptions.admin-motd-set}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    public void execute(Player player, @Single String guild, String motd) {
        // Get the target guild
        Guild targetGuild = guildHandler.getGuild(guild);
        // Check if target guild is null, throw error
        if (targetGuild == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__GUILD_NO_EXIST));
        // Set the MOTD of the guild
        targetGuild.setMotd(ACFBukkitUtil.color(motd));
        // Tell the player that they set the motd
        getCurrentCommandIssuer().sendInfo(Messages.ADMIN__MOTD_SUCCESS, "{guild}", targetGuild.getName(), "{motd}", targetGuild.getMotd());
    }

}