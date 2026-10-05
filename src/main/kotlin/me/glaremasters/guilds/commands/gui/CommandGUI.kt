package me.glaremasters.guilds.commands.gui

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import dev.triumphteam.gui.guis.PaginatedGui
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.exceptions.InvalidTierException
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandGUI : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds

    @Subcommand("buff")
    @Description("{@@descriptions.buff}")
    @Syntax("")
    @CommandPermission(Constants.BASE_PERM + "buff")
    fun buff(player: Player, @Conditions("perm:perm=ACTIVATE_BUFF") guild: Guild) {
        if (!guild.tier.isUseBuffs) {
            throw InvalidTierException()
        }

        guilds.guiHandler.buffs.get(player, guild, guilds.commandManager).open(player)
    }

    @Subcommand("info")
    @Description("{@@descriptions.info}")
    @Syntax("")
    @CommandPermission(Constants.BASE_PERM + "info")
    fun info(player: Player, guild: Guild) {
        guilds.guiHandler.info.get(guild, player).open(player)
    }

    @Subcommand("list")
    @Description("{@@descriptions.list}")
    @Syntax("")
    @CommandPermission(Constants.BASE_PERM + "list")
    fun list(player: Player) {
        val chain = Guilds.newChain<Any>()
        chain.async {
            chain.setTaskData("data", guilds.guiHandler.list.get(player))
        } .sync {
            (chain.getTaskData<Any>("data") as PaginatedGui).open(player)
        }.execute()
    }

    @Subcommand("members")
    @Description("{@@descriptions.members}")
    @Syntax("")
    @CommandPermission(Constants.BASE_PERM + "members")
    fun members(player: Player, guild: Guild) {
        guilds.guiHandler.members.get(guild, player).open(player)
    }

    @Subcommand("vault")
    @Description("{@@descriptions.vault}")
    @Syntax("%optional %vault-number")
    @CommandPermission(Constants.BASE_PERM + "vault")
    fun vault(player: Player, @Conditions("perm:perm=OPEN_VAULT") guild: Guild, @Optional vaultNumber: Int?) {
        if (vaultNumber == null) {
            guilds.guiHandler.vaults.get(guild, player).open(player)
            return
        }

        if (!guilds.guiHandler.vaults.open(guild, player, vaultNumber)) {
            throw ExpectationNotMet(Messages.VAULTS__MAXED)
        }
    }
}
