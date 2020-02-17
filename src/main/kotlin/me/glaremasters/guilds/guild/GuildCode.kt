package me.glaremasters.guilds.guild

import org.bukkit.entity.Player
import java.util.UUID

class GuildCode(val id: String, var uses: Int, val creator: UUID, val redeemers: MutableList<UUID>) {

    fun addRedeemer(player: Player) {
        uses -= 1
        redeemers.add(player.uniqueId)
    }

}