package me.glaremasters.guilds.database;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;
import me.glaremasters.guilds.guild.Guild;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Queries {

    public void createTable(HikariDataSource hikari, String prefix) {
        try {
            PreparedStatement guildsTable = hikari.getConnection().prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `" + prefix + "guilds` (\n" +
                            "  `id` VARCHAR(36) NOT NULL,\n" +
                            "  `data` JSON NOT NULL,\n" +
                            "  PRIMARY KEY (`id`),\n" +
                            "  UNIQUE (`id`));"
            );
            guildsTable.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private PreparedStatement getGuilds(Connection connection, String prefix) throws SQLException {
        return connection.prepareStatement("SELECT * FROM `" + prefix + "guilds`");
    }

    public void loadGuilds(Gson gson, Connection connection, List<Guild> guilds, String prefix) throws SQLException {
        PreparedStatement preparedStatement = getGuilds(connection, prefix);
        ResultSet set = preparedStatement.executeQuery();
        while (set.next()) {
            guilds.add(gson.fromJson(set.getString("data"), Guild.class));
        }
    }

    private boolean guildExist(Connection connection, String id, String prefix) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `" + prefix + "guilds` WHERE id='" + id + "'");
        ResultSet set = preparedStatement.executeQuery();
        return set.isBeforeFirst();
    }

    private void saveNewGuild(Gson gson, Guild guild, Connection connection, String prefix) throws SQLException {
        PreparedStatement save = connection.prepareStatement("INSERT INTO `" + prefix + "guilds` (id, data) VALUES (?, ?)");
        save.setString(1, guild.getId().toString());
        save.setString(2, gson.toJson(guild));
        save.execute();
    }

    private void saveExistingGuild(Gson gson, Guild guild, Connection connection, String prefix) throws SQLException {
        PreparedStatement update = connection.prepareStatement("UPDATE `" + prefix + "guilds` SET data = ? WHERE id = ?");
        update.setString(1, gson.toJson(guild));
        update.setString(2, guild.getId().toString());
        update.execute();
    }

    public List<String> getGuildIDs(Connection connection, String prefix) throws SQLException {
        List<String> ids = new ArrayList<>();
        PreparedStatement update = connection.prepareStatement("SELECT id FROM `" + prefix + "guilds`");
        ResultSet set = update.executeQuery();
        while (set.next()) {
            ids.add(set.getString("id"));
        }
        return ids;
    }

    public void deleteGuilds(Connection connection, List<String> guilds, String prefix) throws SQLException {
        for (String guild : guilds) {
            PreparedStatement delete = connection.prepareStatement("DELETE FROM `" + prefix + "guilds` WHERE id = ?");
            delete.setString(1, guild);
            delete.execute();
        }
    }

    public void saveGuild(Gson gson, Guild guild, Connection connection, String prefix) throws SQLException {
        if (guildExist(connection, guild.getId().toString(), prefix)) {
            saveExistingGuild(gson, guild, connection, prefix);
        } else {
            saveNewGuild(gson, guild, connection, prefix);
        }
    }

}
