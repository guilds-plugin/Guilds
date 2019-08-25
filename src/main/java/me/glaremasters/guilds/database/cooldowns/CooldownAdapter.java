package me.glaremasters.guilds.database.cooldowns;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.database.cooldowns.provider.CooldownJsonProvider;
import me.glaremasters.guilds.utils.LoggingUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public List<Cooldown> getAllCooldowns() throws IOException {
        return provider.getAllCooldowns(sqlTablePrefix);
    }

    public boolean cooldownExists(Cooldown cooldown) throws IOException {
        return cooldownExists(cooldown.getCooldownType(), cooldown.getCooldownOwner());
    }

    public boolean cooldownExists(Cooldown.Type cooldownType, UUID cooldownOwner) throws IOException {
        return provider.cooldownExists(sqlTablePrefix, cooldownType.getTypeName(), cooldownOwner.toString());
    }

    public void createCooldown(Cooldown cooldown) throws IOException {
        createCooldown(cooldown.getCooldownType(), cooldown.getCooldownOwner(), cooldown.getCooldownExpiry());
    }

    public void createCooldown(Cooldown.Type cooldownType, UUID cooldownOwner, Long cooldownExpiry) throws IOException {
        provider.createCooldown(sqlTablePrefix, cooldownType.getTypeName(), cooldownOwner.toString(), new Timestamp(cooldownExpiry));
    }

    public void deleteCooldown(Cooldown cooldown) throws IOException {
        deleteCooldown(cooldown.getCooldownType(), cooldown.getCooldownOwner());
    }

    public void deleteCooldown(Cooldown.Type cooldownType, UUID cooldownOwner) throws IOException {
        provider.deleteCooldown(sqlTablePrefix, cooldownType.getTypeName(), cooldownOwner.toString());
    }

    public void saveCooldowns(Collection<Cooldown> cooldowns) {
        try {
            for (Cooldown cooldown : cooldowns) {
                if (!cooldownExists(cooldown)) {
                    createCooldown(cooldown);
                }
            }
        } catch (IOException ex) {
            LoggingUtils.warn("Failed to save cooldowns");
        }
    }
}
