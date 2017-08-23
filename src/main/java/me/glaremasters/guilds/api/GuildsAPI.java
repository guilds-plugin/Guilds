package me.glaremasters.guilds.api;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.leaderboard.Leaderboard;
import me.glaremasters.guilds.util.SneakyThrow;

public class GuildsAPI {

    /*public static Leaderboard createLeaderboard(String name, Leaderboard.LeaderboardType leaderboardType, Leaderboard.SortType sortType) {
        Leaderboard leaderboard = Main.getInstance().getLeaderboardHandler().getLeaderboards().stream().filter(l -> l.getName().equals(name) && l.getLeaderboardType() == leaderboardType).findFirst().orElse(null);

        if(leaderboard == null) {
            leaderboard = new Leaderboard(name, leaderboardType, sortType, new ArrayList<>());
            Main.getInstance().getDatabaseProvider().createLeaderboard(leaderboard, (result, exception) -> {
                if (!result && exception != null) {
                    SneakyThrow.sneaky(exception);
                }
            });
        }

        return leaderboard;
    }*/

    public static Leaderboard createLeaderboard(Leaderboard leaderboard) {
        Leaderboard existingLeaderboard = Main.getInstance().getLeaderboardHandler()
                .getLeaderboard(leaderboard.getName(), leaderboard.getLeaderboardType());

        if (existingLeaderboard == null) {
            Main.getInstance().getDatabaseProvider()
                    .createLeaderboard(leaderboard, (result, exception) -> {
                        if (!result && exception != null) {
                            SneakyThrow.sneaky(exception);
                        }
                    });

            return leaderboard;
        }

        return existingLeaderboard;
    }


    public static void removeLeaderboard(String name, Leaderboard.LeaderboardType leaderboardType) {
        Main.getInstance().getDatabaseProvider()
                .removeLeaderboard(Leaderboard.getLeaderboard(name, leaderboardType),
                        (result, exception) -> {
                            if (!result && exception != null) {
                                SneakyThrow.sneaky(exception);
                            }
                        });
    }

    public static Leaderboard getLeaderboard(String name,
            Leaderboard.LeaderboardType leaderboardType) {
        return Main.getInstance().getLeaderboardHandler().getLeaderboard(name, leaderboardType);
    }
}
