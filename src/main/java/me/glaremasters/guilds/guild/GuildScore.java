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
