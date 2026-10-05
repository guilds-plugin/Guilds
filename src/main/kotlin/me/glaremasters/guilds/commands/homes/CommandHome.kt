package me.glaremasters.guilds.commands.homes

import ch.jalu.configme.SettingsManager
import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Conditions
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Syntax
import java.util.concurrent.TimeUnit
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.api.events.GuildSetHomeEvent
import me.glaremasters.guilds.configuration.sections.CooldownSettings
import me.glaremasters.guilds.configuration.sections.CostSettings
import me.glaremasters.guilds.cooldowns.Cooldown
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import me.glaremasters.guilds.utils.EconomyUtils
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandHome : BaseCommand() {
    @Dependency
    lateinit var guilds: Guilds
    @Dependency
    lateinit var guildHandler: GuildHandler
    @Dependency
    lateinit var settingsManager: SettingsManager
    @Dependency
    lateinit var cooldownHandler: CooldownHandler
    @Dependency
    lateinit var economy: Economy

    @Subcommand("delhome")
    @Description("{@@descriptions.delhome}")
    @CommandPermission(Constants.BASE_PERM + "delhome")
    @Syntax("")
    fun delete(player: Player, @Conditions("perm:perm=CHANGE_HOME") guild: Guild) {
        guild.delHome()
        currentCommandIssuer.sendInfo(Messages.SETHOME__DELETED)
    }

    @Subcommand("home")
    @Description("{@@descriptions.home}")
    @CommandPermission(Constants.BASE_PERM + "home")
    @Syntax("")
    fun home(player: Player, guild: Guild) {
        val home = guild.home ?: throw ExpectationNotMet(Messages.HOME__NO_HOME_SET)
        val cooldown = Cooldown.Type.Home.name
        val id = player.uniqueId

        if (cooldownHandler.hasCooldown(cooldown, id)) {
            throw ExpectationNotMet(Messages.HOME__COOLDOWN, "{amount}", cooldownHandler.getRemaining(cooldown, id).toString())
        }

        cooldownHandler.addCooldown(player, cooldown, settingsManager.getProperty(CooldownSettings.HOME), TimeUnit.SECONDS)

        if (settingsManager.getProperty(CooldownSettings.WU_HOME_ENABLED) && !player.hasPermission("guilds.warmup.bypass")) {
            val loc = player.location
            val wait = settingsManager.getProperty(CooldownSettings.WU_HOME)
            currentCommandIssuer.sendInfo(Messages.HOME__WARMUP, "{amount}", wait.toString())
            Guilds.newChain<Any>().delay(wait, TimeUnit.SECONDS).sync {
                val curr = player.location
                if (loc.distance(curr) > 1) {
                    guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.HOME__CANCELLED)
                } else {
                    player.teleport(home.asLocation)
                    guilds.commandManager.getCommandIssuer(player).sendInfo(Messages.HOME__TELEPORTED)
                }
            }.execute()
        } else {
            player.teleport(home.asLocation)
            currentCommandIssuer.sendInfo(Messages.HOME__TELEPORTED)
        }
    }

    @Subcommand("sethome")
    @Description("{@@descriptions.sethome}")
    @CommandPermission(Constants.BASE_PERM + "sethome")
    @Syntax("")
    fun set(player: Player, @Conditions("perm:perm=CHANGE_HOME") guild: Guild) {
        val cooldown = Cooldown.Type.SetHome.name
        val id = player.uniqueId

        if (cooldownHandler.hasCooldown(cooldown, id)) {
            throw ExpectationNotMet(Messages.SETHOME__COOLDOWN, "{amount}", cooldownHandler.getRemaining(cooldown, id).toString())
        }

        val cost = settingsManager.getProperty(CostSettings.SETHOME)

        if (!EconomyUtils.hasEnough(guild.balance, cost)) {
            throw ExpectationNotMet(Messages.BANK__NOT_ENOUGH_BANK)
        }

        val event = GuildSetHomeEvent(player, guild, player.location)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        cooldownHandler.addCooldown(player, cooldown, settingsManager.getProperty(CooldownSettings.SETHOME), TimeUnit.SECONDS)
        guild.setNewHome(player)
        guild.balance = guild.balance - cost
        currentCommandIssuer.sendInfo(Messages.SETHOME__SUCCESSFUL)
    }
}
