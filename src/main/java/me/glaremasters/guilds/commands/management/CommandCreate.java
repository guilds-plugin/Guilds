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

package me.glaremasters.guilds.commands.management;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.actions.ConfirmAction;
import me.glaremasters.guilds.api.events.GuildCreateEvent;
import me.glaremasters.guilds.configuration.sections.CostSettings;
import me.glaremasters.guilds.configuration.sections.GuildListSettings;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildSkull;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import me.glaremasters.guilds.utils.EconomyUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Glare
 * Date: 4/5/2019
 * Time: 12:59 AM
 */
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandCreate extends BaseCommand {

    @Dependency private Guilds guilds;
    @Dependency private GuildHandler guildHandler;
    @Dependency private SettingsManager settingsManager;
    @Dependency private ActionHandler actionHandler;
    @Dependency private Economy economy;
    @Dependency private Permission permission;

    /**
     * Create a guild
     * @param player the player executing this command
     * @param name name of guild
     * @param prefix prefix of guild
     */
    @Subcommand("create")
    @Description("{@@descriptions.create}")
    @CommandPermission(Constants.BASE_PERM + "create")
    @Syntax("<name> (optional) <prefix>")
    public void execute(Player player, String name, @Optional String prefix) {

        double cost = settingsManager.getProperty(CostSettings.CREATION);

        if (guildHandler.getGuild(player) != null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__ALREADY_IN_GUILD));

        if (guildHandler.checkGuildNames(name))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.CREATE__GUILD_NAME_TAKEN));

        if (!guildHandler.nameCheck(name, settingsManager))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.CREATE__REQUIREMENTS));

        if (prefix != null) {
            if (!guildHandler.prefixCheck(prefix, settingsManager)) {
                ACFUtil.sneaky(new ExpectationNotMet(Messages.CREATE__REQUIREMENTS));
            }
        } else {
            if (!guildHandler.prefixCheck(name, settingsManager)) {
                ACFUtil.sneaky(new ExpectationNotMet(Messages.CREATE__NAME_TOO_LONG));
            }
        }

        if (!EconomyUtils.hasEnough(getCurrentCommandManager(), economy, player, cost))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__NOT_ENOUGH_MONEY));

        getCurrentCommandIssuer().sendInfo(Messages.CREATE__WARNING, "{amount}", String.valueOf(cost));

        actionHandler.addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                if (!EconomyUtils.hasEnough(getCurrentCommandManager(), economy, player, cost))
                    ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__NOT_ENOUGH_MONEY));

                Guild.GuildBuilder gb = Guild.builder();
                gb.id(UUID.randomUUID());
                gb.name(ACFBukkitUtil.color(name));
                if (prefix == null)
                    gb.prefix(name);
                else
                    gb.prefix(prefix);
                gb.status(Guild.Status.Private);
                GuildMember master = new GuildMember(player.getUniqueId(), guildHandler.getGuildRole(0));
                gb.guildMaster(master);

                List<GuildMember> members = new ArrayList<>();
                members.add(master);
                gb.members(members);
                gb.home(null);
                gb.balance(0);
                gb.tier(guildHandler.getGuildTier(1));

                gb.invitedMembers(new ArrayList<>());
                gb.allies(new ArrayList<>());
                gb.pendingAllies(new ArrayList<>());

                gb.vaults(new ArrayList<>());
                gb.codes(new ArrayList<>());

                Guild guild = gb.build();

                GuildCreateEvent event = new GuildCreateEvent(player, guild);
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled()) return;

                guildHandler.addGuild(guild);

                economy.withdrawPlayer(player, cost);

                getCurrentCommandIssuer().sendInfo(Messages.CREATE__SUCCESSFUL, "{guild}", guild.getName());

                actionHandler.removeAction(player);

                Guilds.newChain().async(() -> {
                    try {
                        guild.setGuildSkull(new GuildSkull(player));
                    } catch (Exception ex) {
                        guild.setGuildSkull(new GuildSkull(settingsManager.getProperty(GuildListSettings.GUILD_LIST_HEAD_DEFAULT_URL)));
                    }
                }).execute();
            }

            @Override
            public void decline() {
                getCurrentCommandIssuer().sendInfo(Messages.CREATE__CANCELLED);
                actionHandler.removeAction(player);
            }
        });




    }

}