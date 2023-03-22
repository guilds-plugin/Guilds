/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
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
