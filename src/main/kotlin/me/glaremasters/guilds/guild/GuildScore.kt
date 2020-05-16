package me.glaremasters.guilds.guild

class GuildScore {
    var wins = 0
    var loses = 0

    fun addWin() {
        wins += 1
    }

    fun addLoss() {
        loses += 1
    }

    fun reset() {
        wins = 0
        loses = 0
    }
}
