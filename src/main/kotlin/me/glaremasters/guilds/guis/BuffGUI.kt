/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.glaremasters.guilds.guis

import ch.jalu.configme.SettingsManager
import co.aikar.commands.PaperCommandManager
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.configuration.sections.CooldownSettings
import me.glaremasters.guilds.configuration.sections.GuildBuffSettings
import me.glaremasters.guilds.cooldowns.Cooldown
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.exte.addBackground
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.EconomyUtils
import me.glaremasters.guilds.utils.GuiBuilder
import me.glaremasters.guilds.utils.GuiUtils
import me.mattstudios.mfgui.gui.guis.Gui
import me.mattstudios.mfgui.gui.guis.GuiItem
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

class BuffGUI(private val guilds: Guilds, private val buffConfig: SettingsManager, private val mainConfig: SettingsManager, private val guildHandler: GuildHandler, private val manager: PaperCommandManager, private val cooldownHandler: CooldownHandler) {

    val buffGUI: Gui
        get() {
            val name = buffConfig.getProperty(GuildBuffSettings.GUILD_BUFF_NAME)
            val gui = GuiBuilder(guilds).setName(name).setRows(1).disableGlobalClicking().build()


            // Haste
            addBuffItem(gui, manager,
                    buffConfig.getProperty(GuildBuffSettings.HASTE_TYPE),
                    buffConfig.getProperty(GuildBuffSettings.HASTE_TIME),
                    buffConfig.getProperty(GuildBuffSettings.HASTE_AMPLIFIER),
                    buffConfig.getProperty(GuildBuffSettings.HASTE_PRICE),
                    buffConfig.getProperty(GuildBuffSettings.HASTE_ICON),
                    buffConfig.getProperty(GuildBuffSettings.HASTE_NAME),
                    buffConfig.getProperty(GuildBuffSettings.HASTE_LORE),
                    buffConfig.getProperty(GuildBuffSettings.HASTE_SLOT),
                    buffConfig.getProperty(GuildBuffSettings.HASTE_DISPLAY),
                    buffConfig.getProperty(GuildBuffSettings.HASTE_CLICKER_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.HASTE_CLICKER_COMMANDS),
                    buffConfig.getProperty(GuildBuffSettings.HASTE_GUILD_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.HASTE_GUILD_COMMANDS
                    ))

            // Speed
            addBuffItem(gui, manager,
                    buffConfig.getProperty(GuildBuffSettings.SPEED_TYPE),
                    buffConfig.getProperty(GuildBuffSettings.SPEED_TIME),
                    buffConfig.getProperty(GuildBuffSettings.SPEED_AMPLIFIER),
                    buffConfig.getProperty(GuildBuffSettings.SPEED_PRICE),
                    buffConfig.getProperty(GuildBuffSettings.SPEED_ICON),
                    buffConfig.getProperty(GuildBuffSettings.SPEED_NAME),
                    buffConfig.getProperty(GuildBuffSettings.SPEED_LORE),
                    buffConfig.getProperty(GuildBuffSettings.SPEED_SLOT),
                    buffConfig.getProperty(GuildBuffSettings.SPEED_DISPLAY),
                    buffConfig.getProperty(GuildBuffSettings.SPEED_CLICKER_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.SPEED_CLICKER_COMMANDS),
                    buffConfig.getProperty(GuildBuffSettings.SPEED_GUILD_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.SPEED_GUILD_COMMANDS
                    ))

            // Fire
            addBuffItem(gui, manager,
                    buffConfig.getProperty(GuildBuffSettings.FR_TYPE),
                    buffConfig.getProperty(GuildBuffSettings.FR_TIME),
                    buffConfig.getProperty(GuildBuffSettings.FR_AMPLIFIER),
                    buffConfig.getProperty(GuildBuffSettings.FR_PRICE),
                    buffConfig.getProperty(GuildBuffSettings.FR_ICON),
                    buffConfig.getProperty(GuildBuffSettings.FR_NAME),
                    buffConfig.getProperty(GuildBuffSettings.FR_LORE),
                    buffConfig.getProperty(GuildBuffSettings.FR_SLOT),
                    buffConfig.getProperty(GuildBuffSettings.FR_DISPLAY),
                    buffConfig.getProperty(GuildBuffSettings.FR_CLICKER_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.FR_CLICKER_COMMANDS),
                    buffConfig.getProperty(GuildBuffSettings.FR_GUILD_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.FR_GUILD_COMMANDS
                    ))

            // Night
            addBuffItem(gui, manager,
                    buffConfig.getProperty(GuildBuffSettings.NV_TYPE),
                    buffConfig.getProperty(GuildBuffSettings.NV_TIME),
                    buffConfig.getProperty(GuildBuffSettings.NV_AMPLIFIER),
                    buffConfig.getProperty(GuildBuffSettings.NV_PRICE),
                    buffConfig.getProperty(GuildBuffSettings.NV_ICON),
                    buffConfig.getProperty(GuildBuffSettings.NV_NAME),
                    buffConfig.getProperty(GuildBuffSettings.NV_LORE),
                    buffConfig.getProperty(GuildBuffSettings.NV_SLOT),
                    buffConfig.getProperty(GuildBuffSettings.NV_DISPLAY),
                    buffConfig.getProperty(GuildBuffSettings.NV_CLICKER_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.NV_CLICKER_COMMANDS),
                    buffConfig.getProperty(GuildBuffSettings.NV_GUILD_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.NV_GUILD_COMMANDS
                    ))

            // Invisibility
            addBuffItem(gui, manager,
                    buffConfig.getProperty(GuildBuffSettings.INVISIBILITY_TYPE),
                    buffConfig.getProperty(GuildBuffSettings.INVISIBILITY_TIME),
                    buffConfig.getProperty(GuildBuffSettings.INVISIBILITY_AMPLIFIER),
                    buffConfig.getProperty(GuildBuffSettings.INVISIBILITY_PRICE),
                    buffConfig.getProperty(GuildBuffSettings.INVISIBILITY_ICON),
                    buffConfig.getProperty(GuildBuffSettings.INVISIBILITY_NAME),
                    buffConfig.getProperty(GuildBuffSettings.INVISIBILITY_LORE),
                    buffConfig.getProperty(GuildBuffSettings.INVISIBILITY_SLOT),
                    buffConfig.getProperty(GuildBuffSettings.INVISIBILITY_DISPLAY),
                    buffConfig.getProperty(GuildBuffSettings.INVISIBILITY_CLICKER_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.INVISIBILITY_CLICKER_COMMANDS),
                    buffConfig.getProperty(GuildBuffSettings.INVISIBILITY_GUILD_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.INVISIBILITY_GUILD_COMMANDS
                    ))

            // Strength
            addBuffItem(gui, manager,
                    buffConfig.getProperty(GuildBuffSettings.STRENGTH_TYPE),
                    buffConfig.getProperty(GuildBuffSettings.STRENGTH_TIME),
                    buffConfig.getProperty(GuildBuffSettings.STRENGTH_AMPLIFIER),
                    buffConfig.getProperty(GuildBuffSettings.STRENGTH_PRICE),
                    buffConfig.getProperty(GuildBuffSettings.STRENGTH_ICON),
                    buffConfig.getProperty(GuildBuffSettings.STRENGTH_NAME),
                    buffConfig.getProperty(GuildBuffSettings.STRENGTH_LORE),
                    buffConfig.getProperty(GuildBuffSettings.STRENGTH_SLOT),
                    buffConfig.getProperty(GuildBuffSettings.STRENGTH_DISPLAY),
                    buffConfig.getProperty(GuildBuffSettings.STRENGTH_CLICKER_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.STRENGTH_CLICKER_COMMANDS),
                    buffConfig.getProperty(GuildBuffSettings.STRENGTH_GUILD_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.STRENGTH_GUILD_COMMANDS
                    ))

            // Jump
            addBuffItem(gui, manager,
                    buffConfig.getProperty(GuildBuffSettings.JUMP_TYPE),
                    buffConfig.getProperty(GuildBuffSettings.JUMP_TIME),
                    buffConfig.getProperty(GuildBuffSettings.JUMP_AMPLIFIER),
                    buffConfig.getProperty(GuildBuffSettings.JUMP_PRICE),
                    buffConfig.getProperty(GuildBuffSettings.JUMP_ICON),
                    buffConfig.getProperty(GuildBuffSettings.JUMP_NAME),
                    buffConfig.getProperty(GuildBuffSettings.JUMP_LORE),
                    buffConfig.getProperty(GuildBuffSettings.JUMP_SLOT),
                    buffConfig.getProperty(GuildBuffSettings.JUMP_DISPLAY),
                    buffConfig.getProperty(GuildBuffSettings.JUMP_CLICKER_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.JUMP_CLICKER_COMMANDS),
                    buffConfig.getProperty(GuildBuffSettings.JUMP_GUILD_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.JUMP_GUILD_COMMANDS
                    ))

            // Water
            addBuffItem(gui, manager,
                    buffConfig.getProperty(GuildBuffSettings.WB_TYPE),
                    buffConfig.getProperty(GuildBuffSettings.WB_TIME),
                    buffConfig.getProperty(GuildBuffSettings.WB_AMPLIFIER),
                    buffConfig.getProperty(GuildBuffSettings.WB_PRICE),
                    buffConfig.getProperty(GuildBuffSettings.WB_ICON),
                    buffConfig.getProperty(GuildBuffSettings.WB_NAME),
                    buffConfig.getProperty(GuildBuffSettings.WB_LORE),
                    buffConfig.getProperty(GuildBuffSettings.WB_SLOT),
                    buffConfig.getProperty(GuildBuffSettings.WB_DISPLAY),
                    buffConfig.getProperty(GuildBuffSettings.WB_CLICKER_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.WB_CLICKER_COMMANDS),
                    buffConfig.getProperty(GuildBuffSettings.WB_GUILD_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.WB_GUILD_COMMANDS
                    ))

            // Regen
            addBuffItem(gui, manager,
                    buffConfig.getProperty(GuildBuffSettings.REGENERATION_TYPE),
                    buffConfig.getProperty(GuildBuffSettings.REGENERATION_TIME),
                    buffConfig.getProperty(GuildBuffSettings.REGENERATION_AMPLIFIER),
                    buffConfig.getProperty(GuildBuffSettings.REGENERATION_PRICE),
                    buffConfig.getProperty(GuildBuffSettings.REGENERATION_ICON),
                    buffConfig.getProperty(GuildBuffSettings.REGENERATION_NAME),
                    buffConfig.getProperty(GuildBuffSettings.REGENERATION_LORE),
                    buffConfig.getProperty(GuildBuffSettings.REGENERATION_SLOT),
                    buffConfig.getProperty(GuildBuffSettings.REGENERATION_DISPLAY),
                    buffConfig.getProperty(GuildBuffSettings.REGENERATION_CLICKER_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.REGENERATION_CLICKER_COMMANDS),
                    buffConfig.getProperty(GuildBuffSettings.REGENERATION_GUILD_COMMAND_CHECK),
                    buffConfig.getProperty(GuildBuffSettings.REGENERATION_GUILD_COMMANDS
                    ))

            addBackground(gui)

            return gui
        }


    private fun addBuffItem(gui: Gui, manager: PaperCommandManager, type: String, length: Int, amplifier: Int, cost: Double, icon: String, name: String, lore: List<String>, x: Int, check: Boolean, clickerCheck: Boolean, clickerCommands: List<String>, guildCheck: Boolean, guildCommands: List<String>) {
        val item = GuiItem(GuiUtils.createItem(icon, name, lore))
        item.setAction { event ->
            event.isCancelled = true
            val player = event.whoClicked as Player
            val cooldownName = Cooldown.Type.Buffs.name
            if (guildHandler.getGuild(player) == null) {
                return@setAction
            }
            val guild = guildHandler.getGuild(player)
            if (buffConfig.getProperty(GuildBuffSettings.PER_BUFF_PERMISSIONS)) {
                if (!player.hasPermission("guilds.buff." + type.toLowerCase())) {
                    manager.getCommandIssuer(player).sendInfo(Messages.ERROR__BUFF_NO_PERMISSION)
                    return@setAction
                }
            }
            if (cooldownHandler.hasCooldown(cooldownName, guild.id)) {
                manager.getCommandIssuer(player).sendInfo(Messages.ERROR__BUFF_COOLDOWN, "{amount}", cooldownHandler.getRemaining(cooldownName, guild.id).toString())
                return@setAction
            }
            if (!EconomyUtils.hasEnough(guild.balance, cost)) {
                manager.getCommandIssuer(player).sendInfo(Messages.BANK__NOT_ENOUGH_BANK)
                return@setAction
            }
            if (!buffConfig.getProperty(GuildBuffSettings.BUFF_STACKING) && !player.activePotionEffects.isEmpty()) {
                return@setAction
            }
            guild.balance = guild.balance - cost
            guild.addPotion(type, (length * 20), amplifier)
            cooldownHandler.addCooldown(guild, cooldownName, mainConfig.getProperty(CooldownSettings.BUFF), TimeUnit.SECONDS)

            executeClickerCommands(clickerCheck, clickerCommands, player)
            executeGuildCommands(guildCheck, guildCommands, guild)
        }

        if (check) {
            gui.setItem(1, (x + 1), item)
        }
    }

    private fun executeClickerCommands(check: Boolean, commands: List<String>, player: Player) {
        if (check) {
            commands.forEach(Consumer { c: String ->
                val update = c.replace("{player}", player.name)
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), update)
            })
        }
    }

    private fun executeGuildCommands(check: Boolean, commands: List<String>, guild: Guild) {
        if (check) {
            guild.onlineAsPlayers.forEach(Consumer { p: Player ->
                commands.forEach(Consumer { c: String ->
                    val update = c.replace("{player}", p.name)
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), update)
                })
            })
        }
    }
}