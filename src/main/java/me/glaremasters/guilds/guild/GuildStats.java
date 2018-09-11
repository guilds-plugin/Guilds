package me.glaremasters.guilds.guild;

import com.google.gson.annotations.Expose;

/**
 * Created by GlareMasters
 * Date: 9/10/2018
 * Time: 9:33 PM
 */
public class GuildStats {

    @Expose
    private int mob_kills, player_kills, deaths, fish_caught;

    public GuildStats(int mob_kills, int player_kills, int deaths, int fish_caught) {
        this.mob_kills = mob_kills;
        this.player_kills = player_kills;
        this.deaths = deaths;
        this.fish_caught = fish_caught;
    }

    public int getMob_kills() {
        return mob_kills;
    }

    public void setMob_kills(int mob_kills) {
        this.mob_kills = mob_kills;
    }

    public int getPlayer_kills() {
        return player_kills;
    }

    public void setPlayer_kills(int player_kills) {
        this.player_kills = player_kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getFish_caught() {
        return fish_caught;
    }

    public void setFish_caught(int fish_caught) {
        this.fish_caught = fish_caught;
    }
}
