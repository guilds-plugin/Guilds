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

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Messages;
import me.glaremasters.guilds.configuration.sections.CodeSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildCode;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.utils.Constants;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by GlareMasters
 * Date: 3/15/2019
 * Time: 2:17 PM
 */
@AllArgsConstructor
@CommandAlias("guild|guilds|g")
public class CommandCodes extends BaseCommand {

    private GuildHandler guildHandler;
    private SettingsManager settingsManager;

    /**
     * Create an invite code for your guild
     * @param player the player creating the invite code
     * @param guild the guild the invite is being created for
     * @param role the guild role of the user
     */
    @Subcommand("code create")
    @Description("{@@descriptions.code-create}")
    @Syntax("<uses>")
    @CommandPermission(Constants.CODE_PERM + "create")
    public void onCreate(Player player, Guild guild, GuildRole role, @Optional @Default("1") Integer uses) {

        if (!role.isCreateCode()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        if (guild.getActiveCodes().size() >= settingsManager.getProperty(CodeSettings.ACTIVE_CODE_AMOUNT)) {
            getCurrentCommandIssuer().sendInfo(Messages.CODES__MAX);
            return;
        }

        String code = RandomStringUtils.randomAlphabetic(settingsManager.getProperty(CodeSettings.CODE_LENGTH));

        guild.addCode(code, uses, player);

        getCurrentCommandIssuer().sendInfo(Messages.CODES__CREATED, "{code}", code, "{amount}", String.valueOf(uses));

    }

    /**
     * Delete an invite code from the guild
     * @param player the player deleting the invite code
     * @param guild the guild the invite is being deleted from
     * @param role the role of the user
     */
    @Subcommand("code delete")
    @Description("{@@descriptions.code-delete}")
    @CommandPermission(Constants.CODE_PERM + "delete")
    @CommandCompletion("@activeCodes")
    public void onDelete(Player player, Guild guild, GuildRole role, @Values("@activeCodes") @Single String code) {

        if (!role.isDeleteCode()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        guild.getCodes().removeIf(s -> s.getId().equals(code));

        getCurrentCommandIssuer().sendInfo(Messages.CODES__DELETED);

    }

    /**
     * List all the current invite codes in your guild
     * @param player the player fetching the list
     * @param guild the guild the player is in
     */
    @Subcommand("code list")
    @Description("{@@descriptions.code-list}")
    @CommandPermission(Constants.CODE_PERM + "list")
    public void onList(Player player, Guild guild) {

        if (guild.getCodes() == null) {
            getCurrentCommandIssuer().sendInfo(Messages.CODES__EMPTY);
            return;
        }

        getCurrentCommandIssuer().sendInfo(Messages.CODES__LIST_HEADER);
        if (settingsManager.getProperty(CodeSettings.LIST_INACTIVE_CODES)) {
            guild.getCodes().forEach(c -> getCurrentCommandIssuer().sendInfo(Messages.CODES__LIST_ITEM, "{code}", c.getId(), "{amount}", String.valueOf(c.getUses()), "{creator}", Bukkit.getOfflinePlayer(c.getCreator()).getName()));
        } else {
            guild.getActiveCodes().forEach(c -> getCurrentCommandIssuer().sendInfo(Messages.CODES__LIST_ITEM, "{code}", c.getId(), "{amount}", String.valueOf(c.getUses()), "{creator}", Bukkit.getOfflinePlayer(c.getCreator()).getName()));
        }

    }

    /**
     * Redeem an invite code to join a guild
     * @param player the player redeeming the code
     * @param code the code being redeemed
     */
    @Subcommand("code redeem")
    @Description("{@@descriptions.code-redeem}")
    @CommandPermission(Constants.CODE_PERM + "redeem")
    public void onRedeem(Player player, String code) {

        if (guildHandler.getGuild(player) != null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }

        Guild guild = guildHandler.getGuildByCode(code);

        if (guild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.CODES__INVALID_CODE);
            return;
        }

        GuildCode gc = guild.getCode(code);

        if (gc.getUses() <= 0) {
            getCurrentCommandIssuer().sendInfo(Messages.CODES__OUT);
            return;
        }

        gc.addRedeemer(player);

        guild.addMemberByCode(new GuildMember(player.getUniqueId(), guildHandler.getLowestGuildRole()));
        getCurrentCommandIssuer().sendInfo(Messages.CODES__JOINED, "{guild}", guild.getName());

    }

    @Subcommand("code info")
    @Description("{@@descriptions.code-info")
    @CommandPermission(Constants.CODE_PERM + "info")
    @Syntax("<code>")
    @CommandCompletion("@activeCodes")
    public void onInfo(Player player, Guild guild, GuildRole role, @Values("@activeCodes") @Single String code) {

        if (code == null) return;

        if (!role.isSeeCodeRedeemers()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        GuildCode gc = guild.getCode(code);

        getCurrentCommandIssuer().sendInfo(Messages.CODES__INFO, "{code}", gc.getId(), "{amount}", String.valueOf(gc.getUses()), "{creator}", Bukkit.getOfflinePlayer(gc.getCreator()).getName(), "{redeemers}", guild.getRedeemers(code));

    }

}
