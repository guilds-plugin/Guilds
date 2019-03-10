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
import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Messages;
import me.glaremasters.guilds.configuration.sections.ClaimSettings;
import me.glaremasters.guilds.configuration.sections.HooksSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedDomain;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.Set;

@SuppressWarnings("unused")
@AllArgsConstructor
@CommandAlias("guild|guilds")
public class CommandClaim extends BaseCommand {

    private SettingsManager settingsManager;

    @Subcommand("claim")
    @Description("{@@descriptions.claim}")
    @CommandPermission("guilds.command.claim")
    public void onClaim(Player player, Guild guild, GuildRole role) {

        int radius = settingsManager.getProperty(ClaimSettings.RADIUS);

        if (!settingsManager.getProperty(HooksSettings.WORLDGUARD)) {
            getCurrentCommandIssuer().sendInfo(Messages.CLAIM__HOOK_DISABLED);
            return;
        }

        WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

        if (!role.isClaimLand()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        Location min = player.getLocation().subtract(radius, (player.getLocation().getY()), radius);
        Location max = player.getLocation().add(radius, (player.getWorld().getMaxHeight() - player.getLocation().getY()), radius);

        if (wrapper.getRegion(player.getWorld(), guild.getId().toString()).isPresent()) {
            getCurrentCommandIssuer().sendInfo(Messages.CLAIM__ALREADY_EXISTS);
            return;
        }

        Set<IWrappedRegion> regions = wrapper.getRegions(min, max);

        if (regions.size() > 0) {
            getCurrentCommandIssuer().sendInfo(Messages.CLAIM__OVERLAP);
            return;
        }

        wrapper.addCuboidRegion(guild.getId().toString(), min, max);

        wrapper.getRegion(player.getWorld(), guild.getId().toString()).ifPresent(region -> {
            region.getOwners().addPlayer(player.getUniqueId());

            IWrappedDomain domain = region.getMembers();

            guild.getMembers().forEach(member -> domain.addPlayer(member.getUuid()));
        });

        getCurrentCommandIssuer().sendInfo(Messages.CLAIM__SUCCESS, "{loc1}", ACFBukkitUtil.formatLocation(min), "{loc2}", ACFBukkitUtil.formatLocation(max));
    }

    @Subcommand("unclaim")
    @Description("{@@descriptions.unclaim}")
    @CommandPermission("guilds.command.unclaim")
    public void onUnClaim(Player player, Guild guild, GuildRole role) {

        if (!settingsManager.getProperty(HooksSettings.WORLDGUARD)) {
            getCurrentCommandIssuer().sendInfo(Messages.CLAIM__HOOK_DISABLED);
            return;
        }

        WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

        if (!role.isUnclaimLand()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        if (wrapper.getRegion(player.getWorld(), guild.getId().toString()).isPresent()) {
            wrapper.removeRegion(player.getWorld(), guild.getId().toString());
            getCurrentCommandIssuer().sendInfo(Messages.UNCLAIM__SUCCESS);
        } else {
            getCurrentCommandIssuer().sendInfo(Messages.UNCLAIM__NOT_FOUND);
        }

    }

}
