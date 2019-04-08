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
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.actions.ActionHandler;
import me.glaremasters.guilds.configuration.sections.GuiSettings;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.guild.GuildTier;
import me.glaremasters.guilds.utils.Constants;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//todo rewrite lol -> this has been rewritten mostly there are still quite a lot of todos due to things being unclear
// or not being added yet, make sure to fix these todos and commented out code and then we can start cleaning the warnings.
// xx lemmo.
@SuppressWarnings("unused")
@AllArgsConstructor
@CommandAlias("guild|guilds|g")
public class CommandGuilds extends BaseCommand {

    private Guilds guilds;
    private GuildHandler guildHandler;
    public final static Inventory guildList = null;
    public final static Map<UUID, Integer> playerPages = new HashMap<>();
    //todo give me explanation pls @Glare
    public final List<Player> home = new ArrayList<>();
    public final List<Player> setHome = new ArrayList<>();
    public final Map<Player, Location> warmUp = new HashMap<>();
    private SettingsManager settingsManager;
    private ActionHandler actionHandler;
    private Economy economy;

    /**
     * Request an invite
     *
     * @param player the player requesting
     * @param name   the name of the guild
     */
    @Subcommand("request")
    @Description("{@@descriptions.request}")
    @CommandPermission(Constants.BASE_PERM + "request")
    @CommandCompletion("@guilds")
    @Syntax("<guild name>")
    public void onRequest(Player player, @Values("@guilds") @Single String name) {
        Guild guild = guildHandler.getGuild(player);
        if (guild != null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ALREADY_IN_GUILD);
            return;
        }

        Guild targetGuild = guildHandler.getGuild(name);
        if (targetGuild == null) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__GUILD_NO_EXIST);
            return;
        }

        for (GuildMember member : targetGuild.getMembers()) {
            GuildRole role = member.getRole();
            if (role.isInvite()) {
                OfflinePlayer guildPlayer = Bukkit.getOfflinePlayer(member.getUuid());
                if (guildPlayer.isOnline()) {
                    getCurrentCommandManager().getCommandIssuer(guildPlayer).sendInfo(Messages.REQUEST__INCOMING_REQUEST, "{player}", player.getName());
                }
            }
        }
        getCurrentCommandIssuer().sendInfo(Messages.REQUEST__SUCCESS, "{guild}", targetGuild.getName());
    }

}
