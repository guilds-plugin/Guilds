package me.glaremasters.guilds.guild

/**
 * A class representing the score of a guild.
 *
 * The score is composed of two properties: `wins` and `loses`, which represent the number of wins and losses
 * of the guild, respectively. The score can be updated by calling the `addWin` and `addLoss` methods, and reset
 * to 0 by calling the `reset` method.
 */
class GuildScore {
    /** The number of wins of the guild. */
    var wins = 0

    /** The number of losses of the guild. */
    var loses = 0

    /**
     * Increases the number of wins of the guild by 1.
     */
    fun addWin() {
        wins += 1
    }

    /**
     * Increases the number of losses of the guild by 1.
     */
    fun addLoss() {
        loses += 1
    }

    /**
     * Resets the score of the guild by setting both the number of wins and losses to 0.
     */
    fun reset() {
        wins = 0
        loses = 0
    }
}
