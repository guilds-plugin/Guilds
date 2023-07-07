package me.glaremasters.guilds.guild

import me.glaremasters.guilds.Guilds

class GuildLogManager(private val guilds: Guilds) {
    private val guildLogs: MutableMap<String, GuildLog> = mutableMapOf()

    fun getOrCreateGuildLog(id: String): GuildLog {
        return guildLogs.getOrPut(id) { GuildLog(id, mutableListOf()) }
    }

    fun getAllGuildLogs(): List<GuildLog> {
        return guildLogs.values.toList()
    }

    fun removeGuildLog(id: String) {
        guildLogs.remove(id)
    }

    fun saveData() {
        guilds.database.logAdapter.saveLogs(guildLogs.values.toList())
    }
}