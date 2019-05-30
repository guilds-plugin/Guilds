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

package me.glaremasters.guilds.commands.motd;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 5/22/2019
 * Time: 11:02 PM
 */
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandMotdSet extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private SettingsManager settingsManager;

    /**
     * Set the motd of a guild
     * @param player the player running the command
     * @param guild the guild of the player
     * @param role the role of the player
     * @param motd the new motd for the guild
     */
    @Subcommand("motd set")
    @Description("{@@descriptions.motd-set}")
    @CommandPermission(Constants.MOTD_PERM + "modify")
    public void execute(Player player, Guild guild, GuildRole role, String motd) {
        // Check if user can modify motd
        if (!role.isModifyMotd())
            ACFUtil.sneaky(new InvalidPermissionException());
        // Set the motd
        guild.setMotd(ACFBukkitUtil.color(motd));
        // Tell the user they set the motd
        getCurrentCommandIssuer().sendInfo(Messages.MOTD__SUCCESS, "{motd}", guild.getMotd());
    }

}