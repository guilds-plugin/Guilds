package me.glaremasters.guilds.leaderboard;

public class Score {

    private String owner;
    private long value;

    public Score(String owner, long value) {
        this.owner = owner;
        this.value = value;
    }

    public String getOwner() {
        return owner;
    }

    public long getValue() {
        return value;
    }
}
