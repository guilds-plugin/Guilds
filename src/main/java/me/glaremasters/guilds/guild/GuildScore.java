/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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

package me.glaremasters.guilds.guild;

public class GuildScore {

    private int wins;
    private int loses;

    public GuildScore() {
        this.wins = 0;
        this.loses = 0;
    }

    /**
     * Add a win to a guild
     */
    public void addWin() {
        setWins(getWins() + 1);
    }

    /**
     * Add a loss to a guild
     */
    public void addLoss() {
        setLoses(getLoses() + 1);
    }

    public int getWins() {
        return this.wins;
    }

    public int getLoses() {
        return this.loses;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setLoses(int loses) {
        this.loses = loses;
    }
}
