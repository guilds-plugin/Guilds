package me.bramhaag.guilds.database.databases.mysql;

import co.aikar.taskchain.TaskChain;
import com.sun.rowset.CachedRowSetImpl;
import com.zaxxer.hikari.HikariDataSource;
import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.database.Callback;
import me.bramhaag.guilds.database.DatabaseProvider;
import me.bramhaag.guilds.guild.Guild;
import me.bramhaag.guilds.guild.GuildRole;
import me.bramhaag.guilds.leaderboard.Leaderboard;
import org.bukkit.configuration.ConfigurationSection;
import org.spigotmc.SneakyThrow;

import javax.sql.rowset.CachedRowSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;


public class MySql extends DatabaseProvider {
    private HikariDataSource hikari;

    @Override
    public void initialize() {
        ConfigurationSection databaseSection = Main.getInstance().getConfig().getConfigurationSection("database");
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

        Main.newChain()
                .async(() -> execute(Query.CREATE_TABLE_GUILDS))
                .async(() -> execute(Query.CREATE_TABLE_MEMBERS))
                .async(() -> execute(Query.CREATE_TABLE_INVITED_MEMBERS))
                .sync(() -> Main.getInstance().getLogger().log(Level.INFO, "Tables 'guilds', 'members' and 'invited_members' created!"))
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
                .async(() -> execute(Query.CREATE_GUILD, guild.getName(), guild.getPrefix()))
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
    public void getGuilds(Callback<HashMap<String, Guild>, Exception> callback) {
        TaskChain<?> chain = Main.newChain();

        chain
                .async(() -> {
                    ResultSet resultSet = executeQuery(Query.GET_GUILDS);
                    if (resultSet == null) {
                        return;
                    }

                    HashMap<String, String> guildData = new HashMap<>();
                    try {
                        while (resultSet.next()) {
                            guildData.put(resultSet.getString("name"), resultSet.getString("prefix"));
                        }
                    } catch (SQLException ex) {
                        SneakyThrow.sneaky(ex);
                    }

                    chain.setTaskData("guild_data", guildData);
                })
                .async(() -> {
                    HashMap<String, Guild> guilds = new HashMap<>();
                    HashMap<String, String> guildData = chain.getTaskData("guild_data");

                    for (String name : guildData.keySet()) {
                        ResultSet resultSet = executeQuery(Query.GET_GUILD_MEMBERS, name);

                        if (resultSet == null) {
                            return;
                        }

                        try {
                            while (resultSet.next()) {
                                Guild guild = new Guild(name);
                                guild.setPrefix(guildData.get(name));

                                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                                GuildRole role = GuildRole.getRole(resultSet.getInt("role"));

                                guild.addMember(uuid, role);

                                guilds.put(name, guild);
                            }
                        } catch (SQLException ex) {
                            SneakyThrow.sneaky(ex);
                        }

                        chain.setTaskData("guilds", guilds);
                    }
                })
                .sync(() -> callback.call(chain.getTaskData("guilds"), null))
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
                    ResultSet resultSet = executeQuery(Query.GET_GUILD_MEMBERS, guild.getName());

                    if (resultSet == null) {
                        return;
                    }

                    try {
                        while (resultSet.next()) {
                            execute(Query.REMOVE_MEMBER, UUID.fromString(resultSet.getString("uuid")));
                        }
                    } catch (SQLException ex) {
                        SneakyThrow.sneaky(ex);
                    }

                    guild.getMembers().forEach(member -> execute(Query.ADD_MEMBER, member.getUniqueId().toString(), guild.getName(), member.getRole()));
                })
                .sync(() -> callback.call(true, null))
                .execute((exception, task) -> {
                    if (exception != null) {
                        callback.call(false, exception);
                        exception.printStackTrace();
                    }
                });
    }

    @Override
    public void updateGuildRank(GuildRole role, Callback<Boolean, Exception> callback) {

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

    private void execute(String query, Object... parameters) {

        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = hikari.getConnection();
            statement = connection.prepareStatement(query);

            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i + 1, parameters[i]);
                }
            }

            statement.execute();
        } catch (SQLException ex) {
            SneakyThrow.sneaky(ex);
        } finally {
            close(connection, statement);
        }
    }

    private ResultSet executeQuery(String query, Object... parameters) {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = hikari.getConnection();
            statement = connection.prepareStatement(query);

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
        } finally {
            close(connection, statement);
        }

        return null;
    }

    @SuppressWarnings("Duplicates")
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
