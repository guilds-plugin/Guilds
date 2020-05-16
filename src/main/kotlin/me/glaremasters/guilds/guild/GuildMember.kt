package me.glaremasters.guilds.guild

import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

class GuildMember(val uuid: UUID, var role: GuildRole) {
    var joinDate: Long = 0
    var lastLogin: Long = 0

    val isOnline: Boolean
        get() = asOfflinePlayer.isOnline

    val asOfflinePlayer: OfflinePlayer
        get() = Bukkit.getOfflinePlayer(uuid)

    val asPlayer: Player?
        get() = Bukkit.getPlayer(uuid)

    val name: String?
        get() = asOfflinePlayer.name
}
