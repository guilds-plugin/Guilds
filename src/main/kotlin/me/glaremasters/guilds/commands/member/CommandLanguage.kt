package me.glaremasters.guilds.commands.member

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Single
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import java.util.Locale
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandLanguage : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler

    @Subcommand("language")
    @Description("{@@descriptions.language}")
    @CommandPermission(Constants.BASE_PERM + "language")
    @Syntax("%language")
    @CommandCompletion("@languages")
    fun language(player: Player, @Values("@languages") @Single language: String) {
        guilds.commandManager.setIssuerLocale(player, Locale.forLanguageTag(language))
        currentCommandIssuer.sendInfo(Messages.LANGUAGES__SET, "{language}", language)
    }
}
