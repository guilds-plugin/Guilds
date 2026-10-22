package me.glaremasters.guilds.commands.management

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import java.util.UUID
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.actions.ActionHandler
import me.glaremasters.guilds.actions.ConfirmAction
import me.glaremasters.guilds.api.events.GuildCreateEvent
import me.glaremasters.guilds.configuration.sections.CostSettings
import me.glaremasters.guilds.configuration.sections.GuildSettings
import me.glaremasters.guilds.configuration.sections.PluginSettings
import me.glaremasters.guilds.cooldowns.Cooldown
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.guild.GuildMember
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.EconomyUtils
import me.glaremasters.guilds.utils.GuildInputValidator
import me.glaremasters.guilds.utils.StringUtils
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandCreate : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var guildHandler: GuildHandler
    @Dependency lateinit var settingsManager: SettingsManager
    @Dependency lateinit var actionHandler: ActionHandler
    @Dependency lateinit var economy: Economy
    @Dependency lateinit var permission: Permission
    @Dependency lateinit var cooldownHandler: CooldownHandler

    @Subcommand("create")
    @Description("{@@descriptions.create}")
    @CommandPermission(Constants.BASE_PERM + "create")
    @Syntax("%name %optional %prefix")
    @Conditions("NotMigrating")
    fun create(@Conditions("NoGuild") player: Player, name: String, @Optional prefix: String?) {
        val cooldown = Cooldown.Type.Join.name
        val id = player.uniqueId

        if (cooldownHandler.hasCooldown(cooldown, id)) {
            throw ExpectationNotMet(Messages.ACCEPT__COOLDOWN, "{amount}", cooldownHandler.getRemaining(cooldown, id).toString())
        }

        val cost = settingsManager.getProperty(CostSettings.CREATION)

        checkCreationAllowed(player, name, prefix, cost)

        currentCommandIssuer.sendInfo(Messages.CREATE__WARNING, "{amount}", EconomyUtils.format(cost))
        actionHandler.addAction(player, object : ConfirmAction {
            override fun accept() {
                // The world can change between the prompt and this confirmation: the player can join
                // another guild, another guild can take the name, the owner can reload the config
                // and change the requirements. Everything the command checked is therefore checked
                // again here, with the same validators and the same messages, so a refusal reads
                // exactly like the one the command would have given.
                try {
                    checkCreationAllowed(player, name, prefix, cost)
                } catch (refused: ExpectationNotMet) {
                    // The offer is void once it has been refused. Leaving the action registered
                    // would let a later /guild confirm create the guild, and charge for it, long
                    // after the player was told it could not be created.
                    actionHandler.removeAction(player)
                    throw refused
                }

                val gb = Guild.builder()
                gb.id(UUID.randomUUID())
                gb.name(StringUtils.color(name))

                if (!settingsManager.getProperty(GuildSettings.DISABLE_PREFIX)) {
                    if (prefix == null) gb.prefix(StringUtils.color(name)) else gb.prefix(StringUtils.color(prefix))
                } else {
                    gb.prefix("")
                }

                gb.status(Guild.Status.Private)
                val master = GuildMember(id, guildHandler.getGuildRole(0)!!)
                master.joinDate = System.currentTimeMillis()
                gb.guildMaster(master)

                val members = mutableListOf<GuildMember>()
                members.add(master)
                gb.members(members)
                gb.home(null)
                gb.balance(0.0)
                gb.tier(guildHandler.getGuildTier(1))

                gb.invitedMembers(arrayListOf())
                gb.allies(arrayListOf())
                gb.pendingAllies(arrayListOf())

                gb.vaults(arrayListOf())
                gb.codes(arrayListOf())

                val guild = gb.build()
                guild.creationDate = System.currentTimeMillis()

                val event = GuildCreateEvent(player, guild)
                Bukkit.getPluginManager().callEvent(event)

                if (event.isCancelled) {
                    actionHandler.removeAction(player)
                    return
                }

                guildHandler.addGuild(guild)
                economy.withdrawPlayer(player, cost)
                currentCommandIssuer.sendInfo(Messages.CREATE__SUCCESSFUL, "{guild}", guild.name)

                guildHandler.addToMemberCache(player.uniqueId, guild.id)
                guildHandler.addGuildPerms(permission, player)
                guildHandler.addRolePerm(permission, player)

                guild.updateGuildSkull(player, settingsManager)

                actionHandler.removeAction(player)
            }

            override fun decline() {
                currentCommandIssuer.sendInfo(Messages.CREATE__CANCELLED)
                actionHandler.removeAction(player)
            }
        })
    }

    /**
     * Runs every check a guild creation has to pass.
     *
     * This runs twice for one creation: once when the command is issued, and again when the pending
     * action is confirmed. Between the prompt and the confirmation the world can change - the player
     * can join another guild, another guild can take the name, the owner can reload the config and
     * change the requirements - and the confirmation used to assume the command's answers still
     * held. The same validators and the same message keys are used in both places, so a refusal at
     * confirmation time reads exactly like the refusal the command would have given.
     *
     * The cost is the one the player was quoted, so the amount charged is the amount they were
     * shown. Whether they can still afford it is what is re-checked, since they may have spent it
     * since the prompt.
     *
     * @param player the player the guild would be created for
     * @param name the proposed guild name
     * @param prefix the proposed guild prefix, or null to use the name as the prefix
     * @param cost the creation cost the player was quoted
     */
    private fun checkCreationAllowed(player: Player, name: String, prefix: String?, cost: Double) {
        // The command is guarded by the NoGuild condition, but that only holds for the moment the
        // command ran: by the time the action is confirmed the player can be in a guild already.
        if (guildHandler.getGuild(player) != null) {
            throw ExpectationNotMet(Messages.ERROR__ALREADY_IN_GUILD)
        }

        if (GuildInputValidator.isNameTaken(name, guildHandler.guilds.values)) {
            throw ExpectationNotMet(Messages.CREATE__GUILD_NAME_TAKEN)
        }

        if (settingsManager.getProperty(GuildSettings.BLACKLIST_TOGGLE) && guildHandler.blacklistCheck(name, settingsManager)) {
            throw ExpectationNotMet(Messages.ERROR__BLACKLIST)
        }

        if (!GuildInputValidator.isValidName(name, settingsManager)) {
            throw ExpectationNotMet(Messages.CREATE__REQUIREMENTS)
        }

        if (!settingsManager.getProperty(GuildSettings.DISABLE_PREFIX)) {
            if (prefix != null) {
                if (!GuildInputValidator.isValidPrefix(prefix, settingsManager)) {
                    throw ExpectationNotMet(Messages.CREATE__PREFIX_TOO_LONG)
                }
            } else {
                if (!GuildInputValidator.isValidPrefix(name, settingsManager)) {
                    throw ExpectationNotMet(Messages.CREATE__NAME_TOO_LONG)
                }
            }
        }

        if (!EconomyUtils.hasEnough(currentCommandManager, economy, player, cost)) {
            throw ExpectationNotMet(Messages.ERROR__NOT_ENOUGH_MONEY)
        }
    }
}
