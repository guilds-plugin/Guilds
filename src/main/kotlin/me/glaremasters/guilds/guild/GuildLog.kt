package me.glaremasters.guilds.guild

import org.bukkit.entity.Player

class GuildLog(val id: String,  val content: MutableList<GuildLogEntry>) {
    fun addEntry(entry: GuildLogEntry) = content.add(entry)

    fun filterByType(type: GuildLogType) = content.filter { it.type == type }

    fun filterByPlayer(player: Player) = content.filter { it.player == player.name }

    fun filterByTime(startTime: Long, endTime: Long) = content.filter { it.time in startTime..endTime }

    companion object {
        private val logsMap = mutableMapOf<String, GuildLog>()

        fun getOrCreateLog(id: String): GuildLog {
            return logsMap[id] ?: GuildLog(id, mutableListOf<GuildLogEntry>()).also {
                logsMap[id] = it
            }
        }
    }
}

data class GuildLogEntry(val type: GuildLogType, val player: String, val amount: Double, val time: Long)

enum class GuildLogType {
    DEPOSIT,
    WITHDRAW,
}