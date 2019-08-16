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

package me.glaremasters.guilds.commands.member;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.api.events.GuildLeaveEvent;
import me.glaremasters.guilds.api.events.GuildRemoveEvent;
import me.glaremasters.guilds.configuration.sections.CooldownSettings;
import me.glaremasters.guilds.configuration.sections.PluginSettings;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.ClaimUtils;
import me.glaremasters.guilds.utils.Constants;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

import java.util.concurrent.TimeUnit;

/**
 * Created by Glare
 * Date: 4/5/2019
 * Time: 11:25 PM
 */
@CommandAlias("%guilds")
public class CommandLeave extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private ActionHandler actionHandler;
    @Dependency private Permission permission;
    @Dependency private SettingsManager settingsManager;
    @Dependency private CooldownHandler cooldownHandler;

    /**
     * Leave a guild
     * @param player the player leaving the guild
     * @param guild the guild being left
     */
    @Subcommand("leave|exit")
    @Description("{@@descriptions.leave}")
    @CommandPermission(Constants.BASE_PERM + "leave")
    public void execute(Player player, Guild guild) {

        if (guildHandler.isMigrating()) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__MIGRATING));
        }

        if (guild.isMaster(player))
            getCurrentCommandIssuer().sendInfo(Messages.LEAVE__WARNING_GUILDMASTER);
        else
            getCurrentCommandIssuer().sendInfo(Messages.LEAVE__WARNING);

        actionHandler.addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                GuildLeaveEvent event = new GuildLeaveEvent(player, guild, GuildLeaveEvent.Cause.PLAYER_LEFT);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled())
                    return;

                if (guild.isMaster(player)) {
                    GuildRemoveEvent removeEvent = new GuildRemoveEvent(player, guild, GuildRemoveEvent.Cause.MASTER_LEFT);
                    Bukkit.getPluginManager().callEvent(removeEvent);

                    if (removeEvent.isCancelled())
                        return;

                    guild.sendMessage(getCurrentCommandManager(), Messages.LEAVE__GUILDMASTER_LEFT,
                            "{player}", player.getName());

                    getCurrentCommandIssuer().sendInfo(Messages.LEAVE__SUCCESSFUL);

                    guildHandler.removePermsFromAll(permission, guild, settingsManager.getProperty(PluginSettings.RUN_VAULT_ASYNC));

                    guildHandler.removeAlliesOnDelete(guild);

                    guildHandler.notifyAllies(guild, getCurrentCommandManager());

                    cooldownHandler.addCooldown(player, Cooldown.TYPES.Join.name(), settingsManager.getProperty(CooldownSettings.JOIN), TimeUnit.SECONDS);

                    ClaimUtils.deleteWithGuild(player, guild, settingsManager);

                    guildHandler.removeGuild(guild);

                } else {

                    guildHandler.removePerms(permission, player, settingsManager.getProperty(PluginSettings.RUN_VAULT_ASYNC));

                    cooldownHandler.addCooldown(player, Cooldown.TYPES.Join.name(), settingsManager.getProperty(CooldownSettings.JOIN), TimeUnit.SECONDS);

                    if (ClaimUtils.isEnable(settingsManager)) {
                        WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();
                        ClaimUtils.getGuildClaim(wrapper, player, guild).ifPresent(region -> {
                            ClaimUtils.removeMember(region, player);
                        });
                    }

                    guild.removeMember(player);



                    getCurrentCommandIssuer().sendInfo(Messages.LEAVE__SUCCESSFUL);

                    guild.sendMessage(getCurrentCommandManager(), Messages.LEAVE__PLAYER_LEFT,
                            "{player}", player.getName());
                }

                actionHandler.removeAction(player);
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.LEAVE__CANCELLED);
                actionHandler.removeAction(player);
            }
        });
    }

}