package me.glaremasters.guilds.acf

import co.aikar.commands.ACFUtil
import co.aikar.commands.BukkitCommandIssuer
import co.aikar.commands.CommandHelp
import co.aikar.commands.CommandHelpFormatter
import co.aikar.commands.CommandIssuer
import co.aikar.commands.HelpEntry
import co.aikar.commands.PaperCommandManager
import me.glaremasters.guilds.Guilds
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.ConsoleCommandSender
import java.util.HashMap


class HelpFormatter(val guilds: Guilds, val manager: PaperCommandManager) : CommandHelpFormatter(manager) {
    var loaded = false
    private var color = "#00ADFF"

    override fun printDetailedHelpHeader(help: CommandHelp, issuer: CommandIssuer, entry: HelpEntry) {
        issuer.send(replacePlaceholders("<color:$color>=====<white>[</white> {commandprefix}{command} <white>Detailed Help ]</white>=====", arrayToMap(getHeaderFooterFormatReplacements(help)), false, null))
    }

    override fun printSearchHeader(help: CommandHelp, issuer: CommandIssuer) {
        issuer.send(replacePlaceholders("<color:$color>=====<white>[</white> {commandprefix}{command} <italic>{search}</italic> <white>Search Results ]</white>=====", arrayToMap(getHeaderFooterFormatReplacements(help)), false, null))
    }

    override fun printHelpHeader(help: CommandHelp, issuer: CommandIssuer) {
        issuer.send(replacePlaceholders("<color:$color>=====<white>[</white> {commandprefix}{command} <white>Help ]</white>=====", arrayToMap(getHeaderFooterFormatReplacements(help)), false, null))
    }

    private fun getFooter(help: CommandHelp): String {
        val builder = StringBuilder()
        if (help.page > 1) {
            builder.append("<color:$color><bold><click:run_command:/guild help ${listToSpaceSeparatedString(help.search)} ${help.page - 1}><hover:show_text:'<italic>Click for previous page'><<</bold></click></hover> </color:$color>")
        }
        builder.append("Page <color:$color>{page}</color:$color> of <color:$color>{totalpages}</color:$color> (<color:$color>{results} results<white>)</white> ============")
        if (help.page < help.totalPages && !help.isOnlyPage) {
            builder.append("<white><bold><click:run_command:/guild help ${listToSpaceSeparatedString(help.search)} ${help.page + 1}><hover:show_text:'<italic>Click for next page'> >></bold></click></hover></white>")
        }
        return builder.toString()
    }

    override fun printSearchFooter(help: CommandHelp, issuer: CommandIssuer) {
        val msg = listOf(replacePlaceholders(getFooter(help), arrayToMap(getHeaderFooterFormatReplacements(help)), false, null), "")
        issuer.send(msg)
    }

    override fun printHelpFooter(help: CommandHelp, issuer: CommandIssuer) {
        printSearchFooter(help, issuer)
    }

    override fun printDetailedHelpCommand(help: CommandHelp, issuer: CommandIssuer, entry: HelpEntry) {
        var finalMessage = entry.description
        finalMessage = ACFUtil.replaceStrings(finalMessage, *getEntryFormatReplacements(help, entry))
        finalMessage = manager.commandReplacements.replace(finalMessage)
        finalMessage = manager.locales.replaceI18NStrings(finalMessage)
        finalMessage = manager.defaultFormatter.format(finalMessage)
        issuer.send(replacePlaceholders(" <white><click:suggest_command:/{command} ><hover:show_text:'<italic>Click to suggest'>/</white><color:$color>{command}</color:$color> <gray>{parameters}</gray></hover></click> <color:$color>{separator}</color:$color> {description}", arrayToMap(getEntryFormatReplacements(help, entry)), false, finalMessage))
    }

    override fun printHelpCommand(help: CommandHelp, issuer: CommandIssuer, entry: HelpEntry) {
        printDetailedHelpCommand(help, issuer, entry)
    }

    override fun printSearchEntry(help: CommandHelp, issuer: CommandIssuer, entry: HelpEntry) {
        printDetailedHelpCommand(help, issuer, entry)
    }

    private fun CommandIssuer.send(message: String) {
        if (this is BukkitCommandIssuer && loaded) {
            if (this.isPlayer || this.issuer is ConsoleCommandSender) {
                guilds.audiences.audience(issuer).sendMessage(MiniMessage.get().parse(message))
            }
        }
    }

    private fun CommandIssuer.send(messages: List<String>) {
        for (m in messages) {
            send(m)
        }
    }

    private fun listToSpaceSeparatedString(strings: List<String>?): String {
        val b = StringBuilder()
        if (strings != null) {
            for ((index, s) in strings.withIndex()) {
                b.append(s)
                if (index != strings.size - 1) {
                    b.append(" ")
                }
            }
        }
        return b.toString()
    }

    private fun arrayToMap(list: Array<String>): Map<String, String> {
        val map = HashMap<String, String>()
        var entry = ""
        var first = true
        for (r in list) {
            if (first) {
                entry = r
                first = false
            } else {
                map[entry] = r
                first = true
            }
        }
        return map
    }

    private fun replacePlaceholders(message: String, placeholders: Map<String, String>, curlyBrackets: Boolean, descriptionFix: String?): String {
        return run {
            var finalMessage = message
            for (placeholder in placeholders.entries) {
                var key = placeholder.key
                if (curlyBrackets) {
                    key = "{$key}"
                }
                finalMessage = finalMessage.replace(key, placeholder.value)
            }
            if (descriptionFix != null) {
                finalMessage = finalMessage.replace("{description}", descriptionFix)
            }
            finalMessage
        }
    }

    override fun getEntryFormatReplacements(help: CommandHelp, entry: HelpEntry): Array<String> {
        return arrayOf(
                "{command}", entry.command,
                "{commandprefix}", help.commandPrefix,
                "{parameters}", entry.parameterSyntax,
                "{separator}", if (entry.description.isEmpty()) "" else "-"
        )
    }

    init {
        Bukkit.getScheduler().runTaskLaterAsynchronously(guilds, Runnable { loaded = true }, 10L)
    }
}