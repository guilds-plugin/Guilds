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
import java.util.UUID;

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

    public boolean cooldownExists(Cooldown.Type cooldownType, UUID cooldownOwner) throws IOException {
        return provider.cooldownExists(sqlTablePrefix, cooldownType.getTypeName(), cooldownOwner.toString());
    }

    public void createCooldown(Cooldown.Type cooldownType, UUID cooldownOwner, Long cooldownExpiry) throws IOException {
        provider.createCooldown(sqlTablePrefix, cooldownType.getTypeName(), cooldownOwner.toString(), cooldownExpiry);
    }

    public void deleteCooldown(Cooldown.Type cooldownType, UUID cooldownOwner) throws IOException {
        provider.deleteCooldown(sqlTablePrefix, cooldownType.getTypeName(), cooldownOwner.toString());
    }

    public void saveCooldowns(List<Cooldown> cooldowns) throws IOException {
        List<Cooldown> knownCooldowns = getAllCooldowns();

        // First let's comb through the passed in cooldowns and check to see if we even know about them.
        // If not, let's create them.
        for (Cooldown cooldown : cooldowns) {
            if (!knownCooldowns.contains(cooldown)) {
                createCooldown(cooldown.getCooldownType(), cooldown.getCooldownOwner(), cooldown.getCooldownExpiry());
            }
        }

        // Now we have to reload the cooldowns so we have an accurate picture of the database.
        knownCooldowns = getAllCooldowns();

        // We mutate knownCooldowns to contain only those cooldowns not also contained in the passed in cooldowns
        // The remaining cooldowns will be those that are expired and were removed in the handler.
        boolean mustPerformDeletions = knownCooldowns.removeAll(cooldowns);
        if (mustPerformDeletions) {
            for (Cooldown cooldown : knownCooldowns) {
                deleteCooldown(cooldown.getCooldownType(), cooldown.getCooldownOwner());
            }
        }
    }
}
