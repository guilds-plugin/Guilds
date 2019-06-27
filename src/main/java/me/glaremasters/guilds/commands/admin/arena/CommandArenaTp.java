package me.glaremasters.guilds.commands.admin.arena;

import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.arena.ArenaHandler;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

@CommandAlias(Constants.ROOT_ALIAS)
public class CommandArenaTp extends BaseCommand {

    @Dependency ArenaHandler arenaHandler;

    @Subcommand("arena tp")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.arena-tp}")
    @Syntax("<arena> <position>")
    @CommandCompletion("@arenas @locations")
    public void execute(Player player, @Values("@arenas") @Single String arena, @Values("@locations") @Single String location) {
        // Get the arena they selected
        Arena selectedArena = arenaHandler.getArena(arena);

        // Make sure it's not null
        if (selectedArena == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ARENA__NO_EXIST));

        // Make sure the selected point exist
        if (location.equalsIgnoreCase("challenger")) {
            if (selectedArena.getChallenger() == null)
                // Tell them that the point wasn't set yet
                ACFUtil.sneaky(new ExpectationNotMet(Messages.ARENA__POSITION_NOT_SET));
            // Teleport them to the location
            player.teleport(ACFBukkitUtil.stringToLocation(selectedArena.getChallenger()));
            // Make sure the selected point exist
        } else if (location.equalsIgnoreCase("defender")) {
            if (selectedArena.getDefender() == null)
                // Tell them that the point wasn't set yet
                ACFUtil.sneaky(new ExpectationNotMet(Messages.ARENA__POSITION_NOT_SET));
            // Teleport them to the location
            player.teleport(ACFBukkitUtil.stringToLocation(selectedArena.getDefender()));
        }

        // Tell them they've been teleported to the selected location
        getCurrentCommandIssuer().sendInfo(Messages.ARENA__TELEPORTED_TO_SELECTION, "{team}", location, "{arena}", arena);
    }

}
