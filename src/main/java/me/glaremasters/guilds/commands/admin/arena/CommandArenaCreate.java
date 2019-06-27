package me.glaremasters.guilds.commands.admin.arena;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.arena.ArenaHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandAlias(Constants.ROOT_ALIAS)
public class CommandArenaCreate extends BaseCommand {

    @Dependency ArenaHandler arenaHandler;

    /**
     * Create a new arena
     * @param player the player running the command
     * @param name name of new arena
     */
    @Subcommand("arena create")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.arena-create}")
    @Syntax("<name>")
    public void execute(Player player, String name) {
        // Create the new arena object
        Arena arena = new Arena(UUID.randomUUID(), name, null, null, false);
        // Add the arena to the handler
        arenaHandler.addArena(arena);
        // Tell the user that it has been created
        getCurrentCommandIssuer().sendInfo(Messages.ARENA__CREATED, "{arena}", name);
    }

}
