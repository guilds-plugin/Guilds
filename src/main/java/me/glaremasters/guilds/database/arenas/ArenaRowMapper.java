package me.glaremasters.guilds.database.arenas;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.arena.Arena;
import org.jdbi.v3.core.statement.StatementContext;

import org.jdbi.v3.core.mapper.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ArenaRowMapper implements RowMapper<Arena> {
    @Override
    public Arena map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Guilds.getGson().fromJson(rs.getString("data"), Arena.class);
    }
}
