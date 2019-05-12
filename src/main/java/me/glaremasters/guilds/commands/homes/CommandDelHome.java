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

package me.glaremasters.guilds.commands.homes;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 11:00 PM
 */
@AllArgsConstructor @CommandAlias(Constants.ROOT_ALIAS)
public class CommandDelHome extends BaseCommand {

    /**
     * Remove a guild home
     * @param player the player removing the guild home
     * @param guild the guild that the home is being removed
     * @param role role of player
     */
    @Subcommand("delhome")
    @Description("{@@descriptions.delhome}")
    @CommandPermission(Constants.BASE_PERM + "delhome")
    public void execute(Player player, Guild guild, GuildRole role) {
        if (!role.isChangeHome())
            ACFUtil.sneaky(new InvalidPermissionException());

        guild.delHome();

        getCurrentCommandIssuer().sendInfo(Messages.SETHOME__DELETED);
    }

}