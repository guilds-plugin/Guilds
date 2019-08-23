package me.glaremasters.guilds.database.cooldowns;

import me.glaremasters.guilds.cooldowns.Cooldown;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CooldownRowMapper implements RowMapper<Cooldown> {
    @Override
    public Cooldown map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Cooldown(
                Cooldown.Type.getByTypeName(rs.getString("type")),
                UUID.fromString(rs.getString("owner")),
                rs.getTimestamp("expiry").getTime()
        );
    }
}