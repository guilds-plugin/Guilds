package me.bramhaag.guilds.leaderboard;

import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.bramhaag.guilds.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public abstract class Leaderboard {

    @Expose
    private String name;

    @Expose
    private LeaderboardType leaderboardType;

    @Expose
    private SortType sortType;

    private List<Score> scores;

    public Leaderboard(String name, LeaderboardType leaderboardType, SortType sortType) {
        this(name, leaderboardType, sortType, new ArrayList<Score>());
    }

    public Leaderboard(String name, LeaderboardType leaderboardType, SortType sortType, List<Score> scores) {
        this.name = name;
        this.leaderboardType = leaderboardType;
        this.sortType = sortType;
        this.scores = scores;
    }

    public String getName() {
        return name;
    }

    public LeaderboardType getLeaderboardType() {
        return leaderboardType;
    }

    public SortType getSortType() {
        return sortType;
    }

    public List<Score> getScores() {
        return scores;
    }

    public List<Score> getSortedScores() {
        return sortType == SortType.ASCENDING ? scores : Lists.reverse(scores);
    }

    public void addScore(Score score) {
        scores.stream().filter(s -> s.getOwner().equals(score.getOwner())).forEach(s -> scores.remove(score));

        for (int i = 0; i < scores.size(); i++) {
            Score s = scores.get(i);

            if (score.getValue() < s.getValue()) {
                scores.add(i, score);
                break;
            }
        }

        Main.getInstance().getDatabaseProvider().updateLeaderboard(this, (result, exception) -> {
            if (!result && exception != null) {
                Main.getInstance().getLogger().log(Level.SEVERE, "Something went wrong while saving score for leaderboard " + this.name);
                exception.printStackTrace();
            }
        });
    }

    public void removeScore(int position) {
        scores.remove(position);

        Main.getInstance().getDatabaseProvider().updateLeaderboard(this, (result, exception) -> {
            if (!result && exception != null) {
                Main.getInstance().getLogger().log(Level.SEVERE, "Something went wrong while removing score from leaderboard " + this.name);
                exception.printStackTrace();
            }
        });
    }

    public void removeScore(String owner) {
        removeScore(getScore(owner));

        Main.getInstance().getDatabaseProvider().updateLeaderboard(this, (result, exception) -> {
            if (!result && exception != null) {
                Main.getInstance().getLogger().log(Level.SEVERE, "Something went wrong while removing score from leaderboard " + this.name);
                exception.printStackTrace();
            }
        });
    }

    public void removeScore(Score score) {
        scores.remove(score);

        Main.getInstance().getDatabaseProvider().updateLeaderboard(this, (result, exception) -> {
            if (!result && exception != null) {
                Main.getInstance().getLogger().log(Level.SEVERE, "Something went wrong while removing score from leaderboard " + this.name);
                exception.printStackTrace();
            }
        });
    }

    public Score getScore(String owner) {
        return scores.stream().filter(s -> s.getOwner().equals(owner)).findFirst().orElse(null);
    }

    public static Leaderboard getLeaderboard(String name, LeaderboardType leaderboardType) {
        return Main.getInstance().getLeaderboardHandler().getLeaderboard(name, leaderboardType);
    }

    public void show(CommandSender sender) {
        if (scores.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "This scoreboard is empty!");
            return;
        }

        sender.sendMessage(ChatColor.AQUA + "Leaderboard " + name);

        for (int i = 0; i < scores.size(); i++) {
            Score score = scores.get(i);

            if (leaderboardType == LeaderboardType.PLAYER) {
                sender.sendMessage(String.format("%d. %s - %d", i + 1, Bukkit.getPlayer(UUID.fromString(score.getOwner())), score.getValue()));
            } else if (leaderboardType == LeaderboardType.GUILD) {
                sender.sendMessage(String.format("%d. %s - %d", i + 1, score.getOwner(), score.getValue()));
            }
        }
    }

    @SuppressWarnings("unused")
    public enum LeaderboardType {
        @SerializedName("PLAYER")
        PLAYER,

        @SerializedName("GUILD")
        GUILD
    }

    @SuppressWarnings("unused")
    public enum SortType {
        @SerializedName("ASCENDING")
        ASCENDING,

        @SerializedName("DESCENDING")
        DESCENDING
    }
}
