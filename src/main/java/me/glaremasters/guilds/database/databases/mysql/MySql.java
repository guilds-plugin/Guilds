package me.glaremasters.guilds.database.databases.mysql;

import com.sun.rowset.CachedRowSetImpl;
import com.zaxxer.hikari.HikariDataSource;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.database.Callback;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.leaderboard.Leaderboard;
import me.glaremasters.guilds.util.SneakyThrow;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import javax.sql.rowset.CachedRowSet;


public class MySql implements DatabaseProvider {

	private HikariDataSource hikari;

	@Override
	public void initialize() {
		ConfigurationSection databaseSection =
				Main.getInstance().getConfig().getConfigurationSection("database");
		if (databaseSection == null) {
			throw new IllegalStateException("MySQL database configured incorrectly, cannot continue properly");
		}

		hikari = new HikariDataSource();
		hikari.setMaximumPoolSize(databaseSection.getInt("pool-size"));

		hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		hikari.addDataSourceProperty("serverName", databaseSection.getString("host"));
		hikari.addDataSourceProperty("port", databaseSection.getInt("port"));
		hikari.addDataSourceProperty("databaseName", databaseSection.getString("database"));

		hikari.addDataSourceProperty("user", databaseSection.getString("username"));
		hikari.addDataSourceProperty("password", databaseSection.getString("password"));

		hikari.validate();
		Main.newChain().async(() -> execute(Query.CREATE_TABLE_GUILDS))
				.async(() -> execute(Query.CREATE_TABLE_MEMBERS))
				.async(() -> execute(Query.CREATE_TABLE_ALLIES))
				.async(() -> execute(Query.CREATE_TABLE_INVITED_MEMBERS))
				.sync(() -> Main.getInstance().getLogger()
						.log(Level.INFO, "Tables 'guilds', 'members', 'guild_homes', 'guild_allies', and 'invited_members' created!"))
				.execute((exception, task) -> {
					if (exception != null) {
						Main.getInstance().getLogger().log(Level.SEVERE, "An error occurred while creating MySQL tables!");
						exception.printStackTrace();
					}
				});
	}

	@Override
	public void createGuild(Guild guild, Callback<Boolean, Exception> callback) {
		Main.newChain()
				.async(() -> execute(Query.CREATE_GUILD, guild.getName(), guild.getPrefix(), "private".equals(guild.getStatus()) ? 1 : 0, 1))
				.async(() -> execute(Query.ADD_MEMBER, guild.getGuildMaster().getUniqueId().toString(), guild.getName(), 0))
				.sync(() -> callback.call(true, null))
				.execute((exception, task) -> {
					if (exception != null) {
						Main.getInstance().getLogger().log(Level.SEVERE, "An error occurred while saving a guild to the MySQL database!");
						exception.printStackTrace();
						callback.call(false, exception);
					}
				});
		Main.getInstance().getGuildHandler().addGuild(guild);
	}

	@Override
	public void removeGuild(Guild guild, Callback<Boolean, Exception> callback) {
		Main.newChain()
				.async(() -> guild.getMembers().forEach(member -> execute(Query.REMOVE_MEMBER, member.getUniqueId().toString())))
				.async(() -> execute(Query.REMOVE_GUILD, guild.getName()))
				.sync(() -> callback.call(true, null))
				.execute((exception, task) -> {
					Main.getInstance().getLogger().log(Level.SEVERE, "An error occurred while removing a guild from the MySQL database!");
					exception.printStackTrace();
					callback.call(false, exception);
				});
	}

	@Override
	public void getGuilds(Callback<Map<String, Guild>, Exception> callback) {
		Main.newChain()
				.asyncFirst(() -> {
					try (ResultSet resultSet = executeQuery(Query.GET_GUILDS)) {
						if (resultSet == null) {
							return null;
						}
						Map<String, String> guildData = new HashMap<>();
						while (resultSet.next()) {
							guildData.put(resultSet.getString("name"), resultSet.getString("prefix"));
						}
						return guildData;
					} catch (SQLException e) {
						SneakyThrow.sneaky(e);
					}
					return null;
				})
				.abortIfNull()
				.async(data -> {
					Map<String, Guild> guilds = new HashMap<>();
					for (String name : data.keySet()) {
						try (ResultSet resultSet = executeQuery(Query.GET_GUILD_MEMBERS, name)) {
							if (resultSet == null) {
								return null;
							}

							while (resultSet.next()) {
								Guild guild = new Guild(name);
								guild.setPrefix(data.get(name));
								guild.addMember(UUID.fromString(resultSet.getString("uuid")), GuildRole.getRole(resultSet.getInt("role")));
								guilds.put(name, guild);
							}
						} catch (SQLException e) {
							SneakyThrow.sneaky(e);
						}
					}
					return guilds;
				})
				.abortIfNull()
				.async(guilds -> {
					for (Map.Entry<String, Guild> entry : guilds.entrySet()) {
						try (ResultSet resultSet = executeQuery(Query.FIND_ALLY, entry.getKey())) {
							if (resultSet == null) {
								return null;
							}

							while (resultSet.next()) {
								String allyName = resultSet.getString("name");
								entry.getValue().addAlly(guilds.get(allyName));
							}
						} catch (SQLException e) {
							SneakyThrow.sneaky(e);
						}
					}
					return guilds;
				})
				.abortIfNull()
				.syncLast(data -> callback.call(data, null))
				.execute((exception, task) -> {
					if (exception != null) {
						callback.call(null, exception);
						exception.printStackTrace();
					}
				});
	}

	@Override
	public void updateGuild(Guild guild, Callback<Boolean, Exception> callback) {
		Main.newChain()
				.async(() -> {
					try (ResultSet resultSet = executeQuery(Query.GET_GUILD_MEMBERS, guild.getName())) {
						if (resultSet == null) {
							return;
						}

						while (resultSet.next()) {
							execute(Query.REMOVE_MEMBER, UUID.fromString(resultSet.getString("uuid")));
						}
					} catch (SQLException ex) {
						SneakyThrow.sneaky(ex);
					}

					guild.getMembers()
							.forEach(member -> execute(Query.ADD_MEMBER, member.getUniqueId().toString(), guild.getName(), member.getRole()));
					for (UUID invite : guild.getInvitedMembers()) {
						execute(Query.REMOVE_INVITED_MEMBER, invite.toString());
						execute(Query.ADD_INVITED_MEMBER, invite.toString(), guild.getName());
					}
					execute("UPDATE guilds SET isPrivate=? WHERE name=?", guild.getStatus().equals("private") ? 1 : 0, guild.getName());
					execute("UPDATE guilds SET tier=? WHERE name=?", guild.getTier(), guild.getName());
				})
				.sync(() -> callback.call(true, null))
				.execute((exception, task) -> {
					if (exception != null) {
						callback.call(false, exception);
						exception.printStackTrace();
					}
				});
	}

	public void updatePrefix(Guild guild, Callback<Boolean, Exception> callback) {
		Main.newChain()
				.async(() -> execute(Query.UPDATE_PREFIX, guild.getPrefix(), guild.getName()))
				.sync(() -> callback.call(true, null))
				.execute((exception, task) -> {
					if (exception != null) {
						Main.getInstance().getLogger().log(Level.SEVERE, "An error occurred while saving a guild to the MySQL database!");
						exception.printStackTrace();
						callback.call(false, exception);
					}
				});
	}

	@Override
	public void addAlly(Guild guild, Guild targetGuild, Callback<Boolean, Exception> callback) {
		Main.newChain()
				.async(() -> {
					try (ResultSet resultSet = executeQuery(Query.FIND_ALLY, guild.getName())) {
						if (resultSet != null && resultSet.next()) {
							callback.call(false, new RuntimeException("Ally already in database."));
						}
						execute(Query.ADD_ALLY, targetGuild.getName(), guild.getName());
						callback.call(true, null);
					} catch (SQLException e) {
						SneakyThrow.sneaky(e);
					}
				})
				.execute((ex, task) -> {
					if (ex != null) {
						callback.call(false, ex);
					}
				});
	}

	@Override
	public void removeAlly(Guild guild, Guild targetGuild, Callback<Boolean, Exception> callback) {
		Main.newChain()
				.async(() -> {
					try (ResultSet resultSet = executeQuery(Query.FIND_ALLY, targetGuild.getName())) {
						if (resultSet == null || !resultSet.next()) {
							return;
						}
						execute(Query.REMOVE_ALLY, targetGuild.getName(), guild.getName());
						callback.call(true, null);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				})
				.execute((ex, task) -> {
					if (ex != null) {
						callback.call(false, ex);
					}
				});
	}

	@Override
	public void createLeaderboard(Leaderboard leaderboard, Callback<Boolean, Exception> callback) {
	}

	@Override
	public void removeLeaderboard(Leaderboard leaderboard, Callback<Boolean, Exception> callback) {
	}

	@Override
	public void getLeaderboards(Callback<List<Leaderboard>, Exception> callback) {
	}

	@Override
	public void updateLeaderboard(Leaderboard leaderboard, Callback<Boolean, Exception> callback) {
	}

	private ResultSet executeQuery(String query, Object... parameters) {
		try (Connection connection = hikari.getConnection(); PreparedStatement statement = connection.prepareStatement(query)) {
			if (parameters != null) {
				for (int i = 0; i < parameters.length; i++) {
					statement.setObject(i + 1, parameters[i]);
				}
			}
			CachedRowSet resultCached = new CachedRowSetImpl();
			ResultSet resultSet = statement.executeQuery();
			resultCached.populate(resultSet);
			resultSet.close();
			return resultCached;
		} catch (SQLException ex) {
			SneakyThrow.sneaky(ex);
		}
		return null;
	}

	private void execute(String query, Object... parameters) {
		try (Connection connection = hikari.getConnection(); PreparedStatement statement = connection.prepareStatement(query)) {
			if (parameters != null && parameters.length > 0) {
				for (int i = 0; i < parameters.length; i++) {
					statement.setObject(i + 1, parameters[i]);
				}
			}
			statement.execute();
		} catch (SQLException ex) {
			SneakyThrow.sneaky(ex);
		}
	}

	private void close(Connection connection, PreparedStatement statement) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException ex) {
				SneakyThrow.sneaky(ex);
			}
		}

		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException ex) {
				SneakyThrow.sneaky(ex);
			}
		}
	}

}
