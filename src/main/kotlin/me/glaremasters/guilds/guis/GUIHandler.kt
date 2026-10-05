package me.glaremasters.guilds.guis

import ch.jalu.configme.SettingsManager
import co.aikar.commands.PaperCommandManager
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.guild.GuildHandler

/**
 * Created by Glare
 * Date: 5/15/2019
 * Time: 10:58 AM
 */
class GUIHandler(guilds: Guilds, settingsManager: SettingsManager, guildHandler: GuildHandler, commandManager: PaperCommandManager, cooldownHandler: CooldownHandler) {
    val buffs = BuffGUI(guilds.settingsHandler.buffConf, cooldownHandler)
    val list = ListGUI(guilds, settingsManager, guildHandler)
    val info = InfoGUI(guilds, settingsManager, guildHandler, cooldownHandler, commandManager)
    val members = MembersGUI(guilds, settingsManager, guildHandler)
    val vaults = VaultGUI(guilds, settingsManager, guildHandler)
}
