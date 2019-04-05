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

package me.glaremasters.guilds.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Messages;
import me.glaremasters.guilds.api.events.GuildAddAllyEvent;
import me.glaremasters.guilds.api.events.GuildRemoveAllyEvent;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@AllArgsConstructor
@CommandAlias("guild|guilds|g")
public class CommandAlly extends BaseCommand {

    private GuildHandler guildHandler;

    /**
     * Remove an ally from your list
     * @param player the player to check
     * @param guild the guild they are in
     * @param role the role of the player
     * @param name the guild you are removing from your list
     */
    @Subcommand("ally remove")
    @Description("{@@descriptions.ally-remove}")
    @CommandPermission(Constants.ALLY_PERM + "remove")
    @Syntax("<guild name>")
    public void onAllyRemove(Player player, Guild guild, GuildRole role, String name) {
        if (checkPermission(role.isRemoveAlly())) return;


        Guild targetGuild = guildHandler.getGuild(name);
        if (targetGuild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        if (!guild.getAllies().contains(targetGuild.getId())) {
            getCurrentCommandIssuer().sendInfo(Messages.ALLY__NOT_ALLIED);
            return;
        }

        GuildRemoveAllyEvent event = new GuildRemoveAllyEvent(player, guild, targetGuild);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        guildHandler.removeAlly(guild, targetGuild);

        guild.sendMessage(getCurrentCommandManager(), Messages.ALLY__CURRENT_REMOVE, "{guild}", targetGuild.getName());
        targetGuild.sendMessage(getCurrentCommandManager(), Messages.ALLY__TARGET_REMOVE, "{guild}", guild.getName());
    }

    /**
     * Checks a permission
     * Sends a no permission message if he has no permission
     *
     * @param hasPermission the boolean if role has permission
     * @return true if message was sent
     */
    private boolean checkPermission(boolean hasPermission) {
        if (hasPermission) return false;
        getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
        return true;
    }

}
