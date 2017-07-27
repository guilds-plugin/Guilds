package me.glaremasters.guilds.database;

import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.leaderboard.Leaderboard;

import java.util.HashMap;
import java.util.List;

public abstract class DatabaseProvider {

  public abstract void initialize();

  public abstract void createGuild(Guild guild, Callback<Boolean, Exception> callback);

  public abstract void removeGuild(Guild guild, Callback<Boolean, Exception> callback);

  public abstract void getGuilds(Callback<HashMap<String, Guild>, Exception> callback);

  public abstract void updateGuild(Guild guild, Callback<Boolean, Exception> callback);

  public abstract void updatePrefix(Guild guild, Callback<Boolean, Exception> callback);

  public abstract void createLeaderboard(Leaderboard leaderboard,
      Callback<Boolean, Exception> callback);

  public abstract void removeLeaderboard(Leaderboard leaderboard,
      Callback<Boolean, Exception> callback);

  public abstract void getLeaderboards(Callback<List<Leaderboard>, Exception> callback);

  public abstract void updateLeaderboard(Leaderboard leaderboard,
      Callback<Boolean, Exception> callback);
}
