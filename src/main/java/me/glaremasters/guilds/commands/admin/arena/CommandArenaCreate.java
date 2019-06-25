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
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandAlias(Constants.ROOT_ALIAS)
public class CommandArenaCreate extends BaseCommand {

    @Dependency ArenaHandler arenaHandler;

    @Subcommand("arena create")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.arena-create}")
    @Syntax("<name>")
    public void execute(Player player, String name) {
        Arena.ArenaBuilder ab = Arena.builder();
        ab.id(UUID.randomUUID());
        ab.name(name);
        ab.challenger("");
        ab.defender("");
        Arena arena = ab.build();
        arenaHandler.addArena(arena);
        // Send message about arena
    }

}
