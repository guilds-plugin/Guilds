package me.glaremasters.guilds.database.cooldowns;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.database.cooldowns.provider.CooldownJsonProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CooldownAdapter {
    private final CooldownProvider provider;
    private String sqlTablePrefix;

    public CooldownAdapter(Guilds guilds, DatabaseAdapter adapter) {
        DatabaseBackend backend = adapter.getBackend();
        switch (backend) {
            default:
            case JSON:
                File fileDataFolder = new File(guilds.getDataFolder(), "cooldowns");
                provider = new CooldownJsonProvider(fileDataFolder);
                break;
            case MYSQL:
            case SQLITE:
                sqlTablePrefix = adapter.getSqlTablePrefix();
                provider = adapter.getDatabaseManager().getJdbi().onDemand(backend.getCooldownProvider());
                break;
        }
    }

    public void createContainer() throws IOException {
        provider.createContainer(sqlTablePrefix);
    }

    public boolean cooldownExists(@NotNull String name) throws IOException {
        return provider.cooldownExists(sqlTablePrefix, name);
    }

    public List<Cooldown> getAllCooldowns() throws IOException {
        return provider.getAllCooldowns(sqlTablePrefix);
    }

    public Cooldown getCooldown(@NotNull String name) throws  IOException {
        return  provider.getCooldown(sqlTablePrefix, name);
    }
}
