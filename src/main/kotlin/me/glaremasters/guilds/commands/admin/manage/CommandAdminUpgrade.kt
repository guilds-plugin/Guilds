package me.glaremasters.guilds.commands.admin.manage

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.*
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.PluginSettings
import me.glaremasters.guilds.configuration.sections.TierSettings
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import net.milkbowl.vault.permission.Permission

@CommandAlias("%guilds")
internal class CommandAdminUpgrade : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var permission: Permission
    @Dependency lateinit var settingsManager: SettingsManager

    @Subcommand("admin upgrade")
    @Description("{@@descriptions.admin-upgrade}")
    @CommandPermission(Constants.ADMIN_PERM)
    @CommandCompletion("@guilds")
    @Syntax("%guild")
    fun upgrade(issuer: CommandIssuer, @Flags("other") @Values("@guilds") guild: Guild) {
        // Resolve the target tier before touching permissions. upgradeTier refuses when there is no
        // tier above this one, so there is nothing left to guard against here.
        if (guildHandler.getNextGuildTier(guild) == null) {
            throw ExpectationNotMet(Messages.UPGRADE__TIER_MAX)
        }

        val previousTier = guild.tier
        guildHandler.upgradeTier(guild)
        guildHandler.applyTierPerms(permission, guild, previousTier)

        currentCommandIssuer.sendInfo(Messages.ADMIN__ADMIN_UPGRADE, "{guild}", guild.name)
        guild.sendMessage(currentCommandManager, Messages.ADMIN__ADMIN_GUILD_UPGRADE)
    }
}
