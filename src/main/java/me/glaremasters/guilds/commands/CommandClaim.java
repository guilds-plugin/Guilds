package me.glaremasters.guilds.commands;

import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedDomain;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.util.Set;

@CommandAlias("guild|guilds")
public class CommandClaim extends BaseCommand {

    @Dependency private Guilds guilds;

    @Subcommand("claim")
    @Description("{@@descriptions.claim}")
    @CommandPermission("guilds.command.claim")
    public void onClaim(Player player, Guild guild, GuildRole role) {

        int radius = guilds.getConfig().getInt("claim-radius");

        if (!guilds.getConfig().getBoolean("main-hooks.worldguard-claims")) {
            getCurrentCommandIssuer().sendInfo(Messages.CLAIM__HOOK_DISABLED);
            return;
        }

        WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

        if (!role.canClaimLand()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        Location min = player.getLocation().subtract(radius, 255, radius);
        Location max = player.getLocation().add(radius, 255, radius);

        if (wrapper.getRegion(player.getWorld(), guild.getName()).isPresent()) {
            getCurrentCommandIssuer().sendInfo(Messages.CLAIM__ALREADY_EXISTS);
            return;
        }

        Set<IWrappedRegion> regions = wrapper.getRegions(min, max);

        if (regions.size() > 0) {
            getCurrentCommandIssuer().sendInfo(Messages.CLAIM__OVERLAP);
            return;
        }

        wrapper.addCuboidRegion(guild.getName(), min, max);

        wrapper.getRegion(player.getWorld(), guild.getName()).ifPresent(region -> {
            region.getOwners().addPlayer(player.getUniqueId());

            IWrappedDomain domain = region.getMembers();

            guild.getMembers().forEach(member -> domain.addPlayer(member.getUniqueId()));
        });

        getCurrentCommandIssuer().sendInfo(Messages.CLAIM__SUCCESS, "{loc1}", ACFBukkitUtil.formatLocation(min), "{loc2}", ACFBukkitUtil.formatLocation(max));
    }

    @Subcommand("unclaim")
    @Description("{@@descriptions.unclaim}")
    @CommandPermission("guilds.command.unclaim")
    public void onUnClaim(Player player, Guild guild, GuildRole role) {

        if (!guilds.getConfig().getBoolean("main-hooks.worldguard-claims")) {
            getCurrentCommandIssuer().sendInfo(Messages.CLAIM__HOOK_DISABLED);
            return;
        }

        WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

        if (!role.canUnclaimLand()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        if (wrapper.getRegion(player.getWorld(), guild.getName()).isPresent()) {
            wrapper.removeRegion(player.getWorld(), guild.getName());
            getCurrentCommandIssuer().sendInfo(Messages.UNCLAIM__SUCCESS);
        } else {
            getCurrentCommandIssuer().sendInfo(Messages.UNCLAIM__NOT_FOUND);
        }

    }

}
