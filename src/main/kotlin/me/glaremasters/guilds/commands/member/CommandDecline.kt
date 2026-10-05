package me.glaremasters.guilds.commands.member

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Flags
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandDecline : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler

    @Subcommand("decline")
    @Description("{@@descriptions.decline}")
    @CommandPermission(Constants.BASE_PERM + "decline")
    @CommandCompletion("@invitedTo")
    @Syntax("%guild")
    fun decline(@Conditions("NoGuild") player: Player, @Flags("other") guild: Guild) {
        if (!guild.checkIfInvited(player)) {
            throw ExpectationNotMet(Messages.ACCEPT__NOT_INVITED)
        }

        guild.removeInvitedMember(player.uniqueId)
        currentCommandIssuer.sendInfo(Messages.DECLINE__SUCCESS)
    }
}
