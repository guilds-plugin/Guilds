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

@CommandAlias("%guilds")
public class CommandArenaChallenger extends BaseCommand {

    @Dependency ArenaHandler arenaHandler;

    /**
     * Set the challenger spawn point for an arena
     * @param player the player running the command
     * @param arena the arena being modified
     */
    @Subcommand("arena set challenger")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.arena-challenger}")
    @Syntax("<arena>")
    @CommandCompletion("@arenas")
    public void execute(Player player, @Values("@arenas") @Single String arena) {
        // Get the arena they selected
        Arena selectedArena = arenaHandler.getArena(arena);

        // Make sure it's not null
        if (selectedArena == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ARENA__NO_EXIST));

        // Set the challenger location to the player's location
        selectedArena.setChallenger(ACFBukkitUtil.fullLocationToString(player.getLocation()));

        // Send them message saying it was set
        getCurrentCommandIssuer().sendInfo(Messages.ARENA__CHALLENGER_SET, "{arena}", arena);
    }
}
