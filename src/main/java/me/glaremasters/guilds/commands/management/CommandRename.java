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

package me.glaremasters.guilds.commands.management;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import me.glaremasters.guilds.api.events.GuildRenameEvent;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.ClaimUtils;
import me.glaremasters.guilds.utils.Constants;
import me.glaremasters.guilds.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

/**
 * Created by Glare
 * Date: 4/5/2019
 * Time: 11:46 PM
 */
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandRename extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private SettingsManager settingsManager;

    /**
     * Rename a guild
     * @param player the player renaming this guild
     * @param guild the guild being renamed
     * @param role the role of the player
     * @param name new name of guild
     */
    @Subcommand("rename")
    @Description("{@@descriptions.rename}")
    @CommandPermission(Constants.BASE_PERM + "rename")
    @Syntax("<name>")
    public void execute(Player player, Guild guild, GuildRole role, String name) {
        if (!role.isChangeName())
            ACFUtil.sneaky(new InvalidPermissionException());

        if (guildHandler.checkGuildNames(name))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.CREATE__GUILD_NAME_TAKEN));

        if (!guildHandler.nameCheck(name, settingsManager))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.CREATE__REQUIREMENTS));

        if (settingsManager.getProperty(GuildSettings.BLACKLIST_TOGGLE)) {
            if (guildHandler.blacklistCheck(name, settingsManager))
                ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__BLACKLIST));
        }

        GuildRenameEvent event = new GuildRenameEvent(player, guild);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        guild.setName(StringUtils.color(name));

         if (ClaimUtils.isEnable(settingsManager)) {
             WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();
             if (ClaimUtils.checkAlreadyExist(wrapper, player, guild)) {
                 ClaimUtils.getGuildClaim(wrapper, player, guild).ifPresent(region -> {
                     ClaimUtils.setEnterMessage(wrapper, region, settingsManager, guild);
                     ClaimUtils.setExitMessage(wrapper, region, settingsManager, guild);
                 });
             }
         }

        getCurrentCommandIssuer().sendInfo(Messages.RENAME__SUCCESSFUL,
                "{name}", name);
    }

}