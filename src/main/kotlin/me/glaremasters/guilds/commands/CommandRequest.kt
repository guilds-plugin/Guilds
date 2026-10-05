package me.glaremasters.guilds.commands

import ch.jalu.configme.SettingsManager
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
import java.util.concurrent.TimeUnit
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.CooldownSettings
import me.glaremasters.guilds.cooldowns.Cooldown
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandRequest : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var cooldownHandler: CooldownHandler
    @Dependency lateinit var settingsManager: SettingsManager

    @Subcommand("request")
    @Description("{@@descriptions.request}")
    @CommandPermission(Constants.BASE_PERM + "request")
    @CommandCompletion("@guilds")
    @Syntax("%guild")
    fun request(@Conditions("NoGuild") player: Player, @Flags("other") target: Guild) {
        val cooldown = Cooldown.Type.Request.name
        val id = player.uniqueId

        if (cooldownHandler.hasCooldown(cooldown, id)) {
            throw ExpectationNotMet(Messages.REQUEST__COOLDOWN, "{time}", cooldownHandler.getRemaining(cooldown, id).toString())
        }

        cooldownHandler.addCooldown(player, cooldown, settingsManager.getProperty(CooldownSettings.REQUEST), TimeUnit.SECONDS)
        guildHandler.pingOnlineInviters(target, guilds.commandManager, player)
        currentCommandIssuer.sendInfo(Messages.REQUEST__SUCCESS, "{guild}", target.name)
    }
}
