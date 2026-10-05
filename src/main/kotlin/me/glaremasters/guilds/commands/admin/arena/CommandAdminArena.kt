package me.glaremasters.guilds.commands.admin.arena

import co.aikar.commands.ACFBukkitUtil
import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Single
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import java.util.UUID
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.arena.Arena
import me.glaremasters.guilds.arena.ArenaHandler
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandAdminArena : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var arenaHandler: ArenaHandler

    @Subcommand("arena set challenger")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.arena-challenger}")
    @Syntax("%arena")
    @CommandCompletion("@arenas")
    fun challenger(player: Player, @Values("@arenas") arena: Arena) {
        arena.challenger = ACFBukkitUtil.fullLocationToString(player.location)
        currentCommandIssuer.sendInfo(Messages.ARENA__CHALLENGER_SET, "{arena}", arena.name)
    }

    @Subcommand("arena create")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.arena-create}")
    @Syntax("%name")
    fun create(issuer: CommandIssuer, name: String) {
        val arena = Arena(UUID.randomUUID(), name, null, null, false)
        arenaHandler.addArena(arena)
        currentCommandIssuer.sendInfo(Messages.ARENA__CREATED, "{arena}", name)
    }

    @Subcommand("arena set defender")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.arena-defender}")
    @Syntax("%arena")
    @CommandCompletion("@arenas")
    fun defender(player: Player, @Values("@arenas") arena: Arena) {
        arena.defender = ACFBukkitUtil.fullLocationToString(player.location)
        currentCommandIssuer.sendInfo(Messages.ARENA__DEFENDER_SET, "{arena}", arena.name)
    }

    @Subcommand("arena delete")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.arena-delete}")
    @CommandCompletion("@arenas")
    @Syntax("%name")
    fun delete(issuer: CommandIssuer, @Values("@arenas") arena: Arena) {
        val name = arena.name
        arenaHandler.removeArena(arena)
        currentCommandIssuer.sendInfo(Messages.ARENA__DELETED, "{arena}", name)
    }

    @Subcommand("arena list")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.arena-list}")
    fun list(issuer: CommandIssuer) {
        val arenas = arenaHandler.getArenas()

        if (arenas.isEmpty()) {
            throw ExpectationNotMet(Messages.ARENA__LIST_EMPTY)
        }

        currentCommandIssuer.sendInfo(Messages.ARENA__LIST, "{arenas}", arenaHandler.arenaNames().joinToString(", "))
    }

    @Subcommand("arena tp")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.arena-tp}")
    @Syntax("%arena %position")
    @CommandCompletion("@arenas @locations")
    fun teleport(player: Player, @Values("@arenas") arena: Arena, @Values("@locations") @Single location: String) {
        if (location.equals("challenger", ignoreCase = true)) {
            if (arena.challenger == null) {
                throw ExpectationNotMet(Messages.ARENA__POSITION_NOT_SET)
            }
            player.teleport(ACFBukkitUtil.stringToLocation(arena.challenger))
        } else if (location.equals("defender", ignoreCase = true)) {
            if (arena.defender == null) {
                throw ExpectationNotMet(Messages.ARENA__POSITION_NOT_SET)
            }
            player.teleport(ACFBukkitUtil.stringToLocation(arena.defender))
        }
        currentCommandIssuer.sendInfo(Messages.ARENA__TELEPORTED_TO_SELECTION, "{team}", location, "{arena}", arena.name)
    }
}
