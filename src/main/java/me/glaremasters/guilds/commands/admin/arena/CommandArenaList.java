package me.glaremasters.guilds.commands.admin.arena;

import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.arena.ArenaHandler;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@CommandAlias(Constants.ROOT_ALIAS)
public class CommandArenaList extends BaseCommand {

    @Dependency ArenaHandler arenaHandler;

    /**
     * List all the arenas on the server
     * @param player the player running the command
     */
    @Subcommand("arena list")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.arena-list}")
    public void execute(Player player) {

        List<String> arenas = arenaHandler.getArenas().stream().filter(Objects::nonNull).map(Arena::getName).collect(Collectors.toList());

        if (arenas.isEmpty())
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ARENA__LIST_EMPTY));

        getCurrentCommandIssuer().sendInfo(Messages.ARENA__LIST, "{arenas}", String.join(", ", arenas));
    }

}
