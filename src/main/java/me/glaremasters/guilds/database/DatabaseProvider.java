package me.glaremasters.guilds.database;

import java.util.HashMap;
import java.util.List;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.leaderboard.Leaderboard;

public interface DatabaseProvider {

    void initialize();

    void createGuild(Guild guild, Callback<Boolean, Exception> callback);

    void removeGuild(Guild guild, Callback<Boolean, Exception> callback);

    void getGuilds(Callback<HashMap<String, Guild>, Exception> callback);

    void updateGuild(Guild guild, Callback<Boolean, Exception> callback);

    void updatePrefix(Guild guild, Callback<Boolean, Exception> callback);

    void addAlly(Guild guild, Guild targetGuild, Callback<Boolean, Exception> callback);

    void removeAlly(Guild guild, Guild targetguild, Callback<Boolean, Exception> callback);

    void createLeaderboard(Leaderboard leaderboard,
            Callback<Boolean, Exception> callback);

    void removeLeaderboard(Leaderboard leaderboard,
            Callback<Boolean, Exception> callback);

    void getLeaderboards(Callback<List<Leaderboard>, Exception> callback);

    void updateLeaderboard(Leaderboard leaderboard,
            Callback<Boolean, Exception> callback);
}
