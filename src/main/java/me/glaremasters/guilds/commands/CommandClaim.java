package me.glaremasters.guilds.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.WrappedRegion;

import java.util.Optional;
import java.util.Set;

@CommandAlias("guild|guilds")
public class CommandClaim extends BaseCommand {

    @Dependency private Guilds guilds;
    private WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

    @Subcommand("claim")
    @Description("{@@descriptions.claim}")
    @CommandPermission("guilds.command.claim")
    public void onClaim(Player player, Guild guild, GuildRole role) {

        if (!role.canClaimLand()) {
            getCurrentCommandIssuer().sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        Location min = player.getLocation().subtract(25, 0, 25);
        Location max = player.getLocation().add(25, 0, 25);

        if (wrapper.getRegion(player.getWorld(), guild.getName()).isPresent()) {
            getCurrentCommandIssuer().sendInfo(Messages.CLAIM__ALREADY_EXISTS);
            return;
        }

        Set<WrappedRegion> regions = wrapper.getRegions(min, max);

        if (regions.size() > 0) {
            getCurrentCommandIssuer().sendInfo(Messages.CLAIM__OVERLAP);
            return;
        }
        wrapper.addCuboidRegion(guild.getName(), min, max);

        Optional<WrappedRegion> guildRegion = wrapper.getRegion(player.getWorld(), guild.getName());

        guildRegion.get().getOwners().addPlayer(player.getUniqueId());

        guild.getMembers()
                .stream().map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()))
                .forEach(member -> guildRegion.get().getMembers().addPlayer(member.getUniqueId()));

        getCurrentCommandIssuer().sendInfo(Messages.CLAIM__SUCCESS);
    }

}
