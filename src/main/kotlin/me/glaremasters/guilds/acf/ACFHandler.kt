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

package me.glaremasters.guilds.acf

import ch.jalu.configme.SettingsManager
import co.aikar.commands.*
import com.google.common.reflect.ClassPath
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.actions.ActionHandler
import me.glaremasters.guilds.arena.Arena
import me.glaremasters.guilds.arena.ArenaHandler
import me.glaremasters.guilds.challenges.ChallengeHandler
import me.glaremasters.guilds.configuration.sections.PluginSettings
import me.glaremasters.guilds.cooldowns.CooldownHandler
import me.glaremasters.guilds.database.DatabaseAdapter
import me.glaremasters.guilds.guild.*
import me.glaremasters.guilds.messages.Messages
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.entity.HumanEntity
import java.util.Locale
import java.util.Objects
import java.util.UUID
import java.util.stream.Collectors
import java.util.stream.IntStream

class ACFHandler(private val plugin: Guilds, private val commandManager: PaperCommandManager) {

    fun load() {
        commandManager.usePerIssuerLocale(true, false)
        commandManager.enableUnstableAPI("help")

        loadLang()
        loadContexts(plugin.guildHandler, plugin.arenaHandler)
        loadCompletions(plugin.guildHandler, plugin.arenaHandler)
        loadDI()

        commandManager.commandReplacements.addReplacement("guilds", plugin.settingsHandler.settingsManager.getProperty(PluginSettings.PLUGIN_ALIASES))
        commandManager.commandReplacements.addReplacement("syntax", plugin.settingsHandler.settingsManager.getProperty(PluginSettings.SYNTAX_NAME))

        loadCommands()
    }

    fun loadLang() {
        plugin.dataFolder.resolve("languages").listFiles()?.filter()
        {
            it.extension.equals("yml", true)
        }?.forEach()
        {
            val locale = Locale.forLanguageTag(it.nameWithoutExtension)

            commandManager.addSupportedLanguage(locale)
            commandManager.locales.loadYamlLanguageFile(it, locale)
        }
        commandManager.locales.defaultLocale = Locale.forLanguageTag(plugin.settingsHandler.settingsManager.getProperty(PluginSettings.MESSAGES_LANGUAGE))
    }

    private fun loadContexts(guildHandler: GuildHandler, arenaHandler: ArenaHandler) {
        commandManager.commandContexts.registerIssuerAwareContext(Guild::class.java) { c: BukkitCommandExecutionContext ->
            val guild: Guild = (if (c.hasFlag("admin")) {
                guildHandler.getGuild(c.popFirstArg())
            } else {
                guildHandler.getGuild(c.player)
            })
                    ?: throw InvalidCommandArgument(Messages.ERROR__NO_GUILD)
            guild
        }
        commandManager.commandContexts.registerIssuerOnlyContext(GuildRole::class.java) { c: BukkitCommandExecutionContext ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerIssuerOnlyContext null
            guildHandler.getGuildRole(guild.getMember(c.player.uniqueId).role.level)
        }
        commandManager.commandContexts.registerContext(Arena::class.java) { c: BukkitCommandExecutionContext -> arenaHandler.getArena(c.popFirstArg()).get() }
    }

    private fun loadCompletions(guildHandler: GuildHandler, arenaHandler: ArenaHandler) {
        commandManager.commandCompletions.registerCompletion("online") { Bukkit.getOnlinePlayers().stream().map { obj: HumanEntity -> obj.name }.collect(Collectors.toList()) }
        commandManager.commandCompletions.registerCompletion("invitedTo") { c: BukkitCommandCompletionContext -> guildHandler.getInvitedGuilds(c.player) }
        commandManager.commandCompletions.registerCompletion("joinableGuilds") { c: BukkitCommandCompletionContext -> guildHandler.getJoinableGuild(c.player) }
        commandManager.commandCompletions.registerCompletion("guilds") { guildHandler.guildNames }
        commandManager.commandCompletions.registerCompletion("arenas") { arenaHandler.getArenas().stream().map(Arena::name).collect(Collectors.toList()) }
        commandManager.commandCompletions.registerCompletion("locations") { listOf("challenger", "defender") }
        commandManager.commandCompletions.registerCompletion("languages") { plugin.loadedLanguages.stream().sorted().collect(Collectors.toList<String>()) }
        commandManager.commandCompletions.registerCompletion("sources") { listOf("JSON", "MYSQL", "SQLITE", "MARIADB") }

        commandManager.commandCompletions.registerCompletion("members") { c: BukkitCommandCompletionContext ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerCompletion null
            guild.members.stream().map { m: GuildMember -> Bukkit.getOfflinePlayer(m.uuid).name }.collect(Collectors.toList())
        }
        commandManager.commandCompletions.registerCompletion("members-admin") { c: BukkitCommandCompletionContext ->
            val guild = c.getContextValue(Guild::class.java, 1) ?: return@registerCompletion null
            guild.members.stream().map { m: GuildMember -> Bukkit.getOfflinePlayer(m.uuid).name }.collect(Collectors.toList())
        }
        commandManager.commandCompletions.registerAsyncCompletion("allyInvites") { c: BukkitCommandCompletionContext ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerAsyncCompletion null
            if (!guild.hasPendingAllies()) {
                return@registerAsyncCompletion null
            }
            guild.pendingAllies.stream().map { uuid: UUID -> guildHandler.getNameById(uuid) }.collect(Collectors.toList())
        }
        commandManager.commandCompletions.registerAsyncCompletion("allies") { c: BukkitCommandCompletionContext ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerAsyncCompletion null
            if (!guild.hasAllies()) {
                return@registerAsyncCompletion null
            }
            guild.allies.stream().map { uuid: UUID -> guildHandler.getNameById(uuid) }.collect(Collectors.toList())
        }
        commandManager.commandCompletions.registerAsyncCompletion("activeCodes") { c: BukkitCommandCompletionContext ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerAsyncCompletion null
            if (guild.codes == null) {
                return@registerAsyncCompletion null
            }
            guild.codes.stream().map(GuildCode::id).collect(Collectors.toList())
        }
        commandManager.commandCompletions.registerAsyncCompletion("vaultAmount") { c: BukkitCommandCompletionContext ->
            val guild = guildHandler.getGuild(c.player) ?: return@registerAsyncCompletion null
            if (guild.vaults == null) {
                return@registerAsyncCompletion null
            }
            val list = guildHandler.cachedVaults[guild] ?: return@registerAsyncCompletion null
            IntStream.rangeClosed(1, list.size).mapToObj { o: Int -> Objects.toString(o) }.collect(Collectors.toList())
        }
    }


    private fun loadCommands() {
        val classes = ClassPath.from(this.javaClass.classLoader).getTopLevelClassesRecursive("me.glaremasters.guilds.commands").asList()
        classes.forEach {
            val clazz = it.load()
            if (BaseCommand::class.java.isAssignableFrom(clazz)) {
                commandManager.registerCommand(clazz.newInstance() as BaseCommand)
            }
        }
    }

    private fun loadDI() {
        commandManager.registerDependency(GuildHandler::class.java, plugin.guildHandler)
        commandManager.registerDependency(SettingsManager::class.java, plugin.settingsHandler.settingsManager)
        commandManager.registerDependency(ActionHandler::class.java, plugin.actionHandler)
        commandManager.registerDependency(Economy::class.java, plugin.economy)
        commandManager.registerDependency(Permission::class.java, plugin.permissions)
        commandManager.registerDependency(CooldownHandler::class.java, plugin.cooldownHandler)
        commandManager.registerDependency(ArenaHandler::class.java, plugin.arenaHandler)
        commandManager.registerDependency(ChallengeHandler::class.java, plugin.challengeHandler)
        commandManager.registerDependency(DatabaseAdapter::class.java, plugin.database)
    }
}
