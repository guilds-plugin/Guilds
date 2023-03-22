/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
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
import com.cryptomorin.xseries.XPotion
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.PaginatedGui
import me.glaremasters.guilds.api.events.GuildBuffEvent
import me.glaremasters.guilds.conf.GuildBuffSettings
import me.glaremasters.guilds.conf.objects.GuildBuff
import me.glaremasters.guilds.cooldowns.Cooldown
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.exte.addBottom
import me.glaremasters.guilds.guild.Guild
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.EconomyUtils
import me.glaremasters.guilds.utils.GuiUtils
import me.glaremasters.guilds.utils.StringUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.concurrent.TimeUnit

class BuffGUI(private val buffConfig: SettingsManager, private val cooldownHandler: CooldownHandler) {

    fun get(player: Player, guild: Guild, manager: PaperCommandManager): PaginatedGui {
        val name = buffConfig.getProperty(GuildBuffSettings.GUI_NAME)
        val gui = PaginatedGui( 6, 45, StringUtils.color(name))

        gui.setDefaultClickAction { event ->
            event.isCancelled = true
        }

        setBuffItem(gui, player, guild, manager)
        addBottom(gui)
        createButtons(gui)

        return gui
    }

    private fun createButtons(gui: PaginatedGui) {
        val nav = buffConfig.getProperty(GuildBuffSettings.NAVIGATION) ?: return

        val next = GuiItem(GuiUtils.createItem(nav.next.material, nav.next.name, emptyList()))
        next.setAction {
            gui.next()
        }

        val back = GuiItem(GuiUtils.createItem(nav.previous.material, nav.previous.name, emptyList()))
        back.setAction {
            gui.previous()
        }

        gui.setItem(6, 9, next)
        gui.setItem(6, 1, back)
    }

    private fun setBuffItem(gui: PaginatedGui, player: Player, guild: Guild, manager: PaperCommandManager) {
        val buffs = buffConfig.getProperty(GuildBuffSettings.BUFFS) ?: return
        val cooldownName = Cooldown.Type.Buffs.name
        buffs.forEach { buff ->
            val access = player.hasPermission(buff.permission)
            val item = if (access) GuiUtils.createItem(buff.unlocked.material, buff.unlocked.name, buff.unlocked.lore) else GuiUtils.createItem(buff.locked.material, buff.locked.name, buff.locked.lore)
            val cost = buff.price
            val guiItem = GuiItem(item)
            guiItem.setAction { event ->
                val buffEvent = GuildBuffEvent(player, guild, buff)
                Bukkit.getPluginManager().callEvent(buffEvent)

                if (buffEvent.isCancelled) {
                    return@setAction
                }

                event.isCancelled = true

                if (!access) {
                    manager.getCommandIssuer(player).sendInfo(Messages.ERROR__BUFF_NO_PERMISSION)
                    return@setAction
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
                getBuffEffects(buff.effects).forEach { effect ->
                    guild.addPotion(effect)
                }
                cooldownHandler.addCooldown(guild, cooldownName, buffConfig.getProperty(GuildBuffSettings.COOLDOWN), TimeUnit.SECONDS)
                runCommands(buff.clicker.enabled, buff.clicker.commands, listOf(player), player, buff)
                runCommands(buff.guild.enabled, buff.guild.commands, guild.onlineAsPlayers, player, buff)
            }
            gui.addItem(guiItem)
        }
    }

    private fun getBuffEffects(effects: List<String>): Set<PotionEffect> {
        val potions = mutableSetOf<PotionEffect>()
        effects.forEach {
            val split = it.split(";")
            val type = XPotion.matchXPotion(split[0]).get().potionEffectType ?: PotionEffectType.WATER_BREATHING
            val amp = Integer.parseInt(split[1])
            val length = Integer.parseInt(split[2])
            potions.add(PotionEffect(type, (length * 20), amp))
        }
        return potions
    }

    private fun runCommands(run: Boolean, commands: List<String>, players: List<Player>, buyer: Player, buff: GuildBuff) {
        if (!run) {
            return
        }
        players.forEach { player ->
            commands.forEach {
                val update = it.replace("{player}", player.name).replace("{buyer}", buyer.name).replace("{buff_name}", buff.unlocked.name)
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), update)
            }
        }
    }
}
