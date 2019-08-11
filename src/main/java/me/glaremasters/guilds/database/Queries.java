package me.glaremasters.guilds.database;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;
import me.glaremasters.guilds.guild.Guild;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Queries {

    public void createTable(HikariDataSource hikari) {
        try {
            PreparedStatement guildsTable = hikari.getConnection().prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `guilds` (\n" +
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

    private PreparedStatement getGuilds(HikariDataSource hikari) throws SQLException {
        return hikari.getConnection().prepareStatement("SELECT * FROM guilds");
    }

    public void loadGuilds(Gson gson, HikariDataSource hikari, List<Guild> guilds) throws SQLException {
        PreparedStatement preparedStatement = getGuilds(hikari);
        ResultSet set = preparedStatement.executeQuery();
        while (set.next()) {
            guilds.add(gson.fromJson(set.getString("data"), Guild.class));
        }
    }

    private boolean guildExist(HikariDataSource hikari, String id) throws SQLException {
        PreparedStatement preparedStatement = hikari.getConnection().prepareStatement("SELECT * FROM guilds WHERE id='" + id + "'");
        ResultSet set = preparedStatement.executeQuery();
        return set.next();
    }

    private void saveNewGuild(Gson gson, Guild guild, HikariDataSource hikari) throws SQLException {
        PreparedStatement save = hikari.getConnection().prepareStatement("INSERT INTO `guilds` (id, data) VALUES (?, ?)");
        save.setString(1, guild.getId().toString());
        save.setString(2, gson.toJson(guild));
        save.execute();
    }

    private void saveExistingGuild(Gson gson, Guild guild, HikariDataSource hikari) throws SQLException {
        PreparedStatement update = hikari.getConnection().prepareStatement("UPDATE `guilds` SET data = ? WHERE id = ?");
        update.setString(1, gson.toJson(guild));
        update.setString(2, guild.getId().toString());
        update.execute();
    }

    public void saveGuild(Gson gson, Guild guild, HikariDataSource hikari) throws SQLException {
        if (guildExist(hikari, guild.getId().toString())) {
            saveExistingGuild(gson, guild, hikari);
        } else {
            saveNewGuild(gson, guild, hikari);
        }
    }

}
