package me.glaremasters.guilds.commands.admin

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants

@CommandAlias("%guilds")
internal class CommandReload : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager

    @Subcommand("reload")
    @Description("{@@descriptions.reload}")
    @CommandPermission(Constants.ADMIN_PERM)
    fun reload(issuer: CommandIssuer) {
        settingsManager.reload()
        guilds.settingsHandler.buffConf.reload()
        guilds.acfHandler.loadLang()
        currentCommandIssuer.sendInfo(Messages.RELOAD__RELOADED)
    }
}
