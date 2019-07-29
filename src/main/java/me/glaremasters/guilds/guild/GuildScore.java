package me.glaremasters.guilds.guild;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}
