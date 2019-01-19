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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by GlareMasters
 * Date: 9/10/2018
 * Time: 6:49 PM
 */
@AllArgsConstructor
@CommandAlias("guild|guilds")
public class CommandAlly extends BaseCommand {

    private GuildHandler guildHandler;

    /**
     * List all the allies of your guild
     * @param player the player to check
     * @param guild the guild they are in
     */
    @Subcommand("ally list")
    @Description("{@@descriptions.ally-list}")
    @CommandPermission("guilds.command.ally")
    public void onAllyList(Player player, Guild guild) {
        if (guild.getAllies().size() < 1) {
            getCurrentCommandIssuer().sendInfo(Messages.ALLY__NONE);
            return;
        }
        getCurrentCommandIssuer().sendInfo(Messages.ALLY__LIST, "{ally-list}", String.join(",", guild.getAllies()));
    }

    /**
     * Accept a guild ally request
     * @param player the player to check
     * @param guild the guild they are in
     * @param role the role of the player
     * @param name the guild name they are accepting
     */
    @Subcommand("ally accept")
    @Description("{@@descriptions.ally-accept}")
    @CommandPermission("guilds.command.ally")
    @Syntax("<guild name>")
    public void onAllyAccept(Player player, Guild guild, GuildRole role, String name) {
        if (!role.canAddAlly()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        Guild targetGuild = utils.getGuild(name);
        if (targetGuild == null) return;
        if (!guild.getPendingAllies().contains(targetGuild.getName())) return;
        utils.removePendingAlly(guild, targetGuild);
        utils.addAlly(guild, targetGuild);
        utils.addAlly(targetGuild, guild);
        utils.sendMessage(guild, Messages.ALLY__CURRENT_ACCEPTED, "{guild}", targetGuild.getName());
        utils.sendMessage(targetGuild, Messages.ALLY__TARGET_ACCEPTED, "{guild}", guild.getName());
    }

    /**
     * Decline a guild ally request
     * @param player the player to check
     * @param guild the guild they are in
     * @param role the role of the player
     * @param name the guild name they are declining
     */
    @Subcommand("ally decline")
    @Description("{@@descriptions.ally-decline}")
    @CommandPermission("guilds.command.ally")
    @Syntax("<guild name>")
    public void onAllyDecline(Player player, Guild guild, GuildRole role, String name) {
        if (!role.canRemoveAlly()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        Guild targetGuild = utils.getGuild(name);
        if (targetGuild == null) return;

        if (!guild.getPendingAllies().contains(targetGuild.getName())) return;
        utils.removePendingAlly(guild, targetGuild);
        utils.sendMessage(guild, Messages.ALLY__CURRENT_DECLINED, "{guild}", targetGuild.getName());
        utils.sendMessage(targetGuild, Messages.ALLY__TARGET_DECLINED, "{guild}", guild.getName());
    }

    /**
     * Send a guild ally request
     * @param player the player to check
     * @param guild the guild they are in
     * @param role the role of the player
     * @param name the guild the request is being sent to
     */
    @Subcommand("ally add")
    @Description("{@@descriptions.ally-add}")
    @CommandPermission("guilds.command.ally")
    @Syntax("<guild name>")
    public void onAllyAdd(Player player, Guild guild, GuildRole role, String name) {
        if (!role.canAddAlly()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        Guild targetGuild = utils.getGuild(name);

        if (targetGuild == null) return;

        if (targetGuild.getPendingAllies().contains(guild.getName())) {
            getCurrentCommandIssuer().sendInfo(Messages.ALLY__ALREADY_REQUESTED);
            return;
        }

        if (guild.getAllies().contains(targetGuild.getName())) {
            getCurrentCommandIssuer().sendInfo(Messages.ALLY__ALREADY_ALLY);
            return;
        }

        if (guild.getName().equalsIgnoreCase(targetGuild.getName())) {
            getCurrentCommandIssuer().sendInfo(Messages.ALLY__SAME_GUILD);
            return;
        }

        GuildAddAllyEvent event = new GuildAddAllyEvent(player, guild, targetGuild);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        getCurrentCommandIssuer().sendInfo(Messages.ALLY__INVITE_SENT, "{guild}", targetGuild.getName());
        utils.sendMessage(targetGuild, Messages.ALLY__INCOMING_INVITE, "{guild}", guild.getName());
        utils.addPendingAlly(targetGuild, guild);
    }

    /**
     * Remove an ally from your list
     * @param player the player to check
     * @param guild the guild they are in
     * @param role the role of the player
     * @param name the guild you are removing from your list
     */
    @Subcommand("ally remove")
    @Description("{@@descriptions.ally-remove}")
    @CommandPermission("guilds.command.ally")
    @Syntax("<guild name>")
    public void onAllyRemove(Player player, Guild guild, GuildRole role, String name) {
        if (!role.canRemoveAlly()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        Guild targetGuild = utils.getGuild(name);
        if (targetGuild == null) return;

        if (!guild.getAllies().contains(targetGuild.getName())) {
            getCurrentCommandIssuer().sendInfo(Messages.ALLY__NOT_ALLIED);
            return;
        }

        GuildRemoveAllyEvent event = new GuildRemoveAllyEvent(player, guild, targetGuild);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        utils.removeAlly(guild, targetGuild);
        utils.removeAlly(targetGuild, guild);

        utils.sendMessage(guild, Messages.ALLY__CURRENT_REMOVE, "{guild}", targetGuild.getName());
        utils.sendMessage(targetGuild, Messages.ALLY__TARGET_REMOVE, "{guild}", guild.getName());
    }

}
