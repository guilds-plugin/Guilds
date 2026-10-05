package me.glaremasters.guilds.commands.actions

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.actions.ActionHandler
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import org.bukkit.command.CommandSender

@CommandAlias("%guilds")
internal class CommandActions : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var actionHandler: ActionHandler

    @Subcommand("cancel")
    @Description("{@@descriptions.cancel}")
    @CommandPermission(Constants.BASE_PERM + "cancel")
    fun cancel(player: CommandIssuer) {
        val action = actionHandler.getAction(player.getIssuer<CommandSender>()) ?: throw ExpectationNotMet(Messages.CANCEL__ERROR)
        currentCommandIssuer.sendInfo(Messages.CANCEL__SUCCESS)
        action.decline()
    }

    @Subcommand("confirm")
    @Description("{@@descriptions.confirm}")
    @CommandPermission(Constants.BASE_PERM + "confirm")
    fun confirm(player: CommandIssuer) {
        val action = actionHandler.getAction(player.getIssuer<CommandSender>()) ?: throw ExpectationNotMet(Messages.CONFIRM__ERROR)
        currentCommandIssuer.sendInfo(Messages.CONFIRM__SUCCESS)
        action.accept()
    }
}
