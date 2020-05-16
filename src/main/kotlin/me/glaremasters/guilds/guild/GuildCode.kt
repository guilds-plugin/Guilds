package me.glaremasters.guilds.guild

import java.util.UUID
import org.bukkit.entity.Player

class GuildCode(val id: String, var uses: Int, val creator: UUID, val redeemers: MutableList<UUID>) {

    fun addRedeemer(player: Player) {
        uses -= 1
        redeemers.add(player.uniqueId)
    }
}
