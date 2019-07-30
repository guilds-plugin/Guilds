package me.glaremasters.guilds.commands.admin.arena;

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
public class CommandArenaDelete extends BaseCommand {

    @Dependency ArenaHandler arenaHandler;

    /**
     * Remove an arena from the server
     * @param player the player running the command
     * @param arena the arena being removed
     */
    @Subcommand("arena delete")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.arena-delete}")
    @CommandCompletion("@arenas")
    @Syntax("<name>")
    public void execute(Player player, @Values("@arenas") @Single String arena) {
        // Get the arena
        Arena selectedArena = arenaHandler.getArena(arena);

        // Make sure it's not null
        if (selectedArena == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ARENA__NO_EXIST));

        // Remove the arena from the existence of time
        arenaHandler.removeArena(selectedArena);

        // Tell the user that it has been created
        getCurrentCommandIssuer().sendInfo(Messages.ARENA__DELETED, "{arena}", arena);
    }

}
