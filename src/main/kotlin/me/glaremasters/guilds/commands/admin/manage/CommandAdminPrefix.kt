package me.glaremasters.guilds.commands.admin.manage

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Flags
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import co.aikar.commands.annotation.Values
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandAdminPrefix : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler

    @Subcommand("admin prefix")
    @Description("{@@descriptions.admin-prefix}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild %prefix")
    fun prefix(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild, prefix: String) {
        guild.prefix = StringUtils.color(prefix)
        currentCommandIssuer.sendInfo(Messages.PREFIX__SUCCESSFUL, "{prefix}", guild.prefix)
    }
}
