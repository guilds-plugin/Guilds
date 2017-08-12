package me.glaremasters.guilds.database;

import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.leaderboard.Leaderboard;

import java.util.HashMap;
import java.util.List;

public interface DatabaseProvider {

  public void initialize();

  public void createGuild(Guild guild, Callback<Boolean, Exception> callback);

  public void removeGuild(Guild guild, Callback<Boolean, Exception> callback);

  public void getGuilds(Callback<HashMap<String, Guild>, Exception> callback);

  public void updateGuild(Guild guild, Callback<Boolean, Exception> callback);

  public void updatePrefix(Guild guild, Callback<Boolean, Exception> callback);

  public void addAlly(Guild guild, Guild targetGuild, Callback<Boolean, Exception> callback);

  public void createLeaderboard(Leaderboard leaderboard,
      Callback<Boolean, Exception> callback);

  public void removeLeaderboard(Leaderboard leaderboard,
      Callback<Boolean, Exception> callback);

  public void getLeaderboards(Callback<List<Leaderboard>, Exception> callback);

  public void updateLeaderboard(Leaderboard leaderboard,
      Callback<Boolean, Exception> callback);
}
