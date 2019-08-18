package me.glaremasters.guilds.database.cooldowns;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.cooldowns.Cooldown;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CooldownRowMapper implements RowMapper<Cooldown> {
    @Override
    public Cooldown map(ResultSet rs, StatementContext ctx) throws SQLException {
        return Guilds.getGson().fromJson(rs.getString("data"), Cooldown.class);
    }
}
