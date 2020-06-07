package me.glaremasters.guilds.commands.editor

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Subcommand
import com.google.gson.GsonBuilder
import me.glaremasters.guilds.guild.GuildHandler
import me.glaremasters.guilds.http.HasteBinClient
import me.rayzr522.jsonmessage.JSONMessage
import org.bukkit.entity.Player

@CommandAlias("%guilds")
internal class CommandEditor : BaseCommand() {
    @Dependency lateinit var guildHandler: GuildHandler

    @Subcommand("editor tiers")
    fun buff(player: Player) {
        val tiers = guildHandler.tiers
        var id = ""
        try {
            id = HasteBinClient.post(GsonBuilder().disableHtmlEscaping().setPrettyPrinting().excludeFieldsWithModifiers().create().toJson(tiers))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }


        JSONMessage.create(id).openURL(id).send(player)

    }
}