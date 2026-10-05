package me.glaremasters.guilds.database.cooldowns;

import me.glaremasters.guilds.cooldowns.Cooldown;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Stores cooldowns in whichever backend is configured.
 */
public interface CooldownProvider {

    /** Creates the cooldowns table, or the cooldown data folder and file on the JSON backend. */
    void createContainer(@Nullable String tablePrefix) throws IOException;

    /** Cooldowns are addressed by type and owner, both passed as strings. */
    boolean cooldownExists(@Nullable String tablePrefix, @NotNull String cooldownType, @NotNull String cooldownOwner) throws IOException;

    List<Cooldown> getAllCooldowns(@Nullable String tablePrefix) throws IOException;

    /**
     * Generates the id here and then drops it, so a cooldown saved this way can only be found
     * again by type and owner. Pass an id yourself if you need to address the record directly.
     */
    default void createCooldown(@Nullable String tablePrefix, @NotNull String cooldownType, @NotNull String cooldownOwner, @NotNull Timestamp cooldownExpiry) throws IOException {
        createCooldown(tablePrefix, UUID.randomUUID().toString(), cooldownType, cooldownOwner, cooldownExpiry);
    }

    /** Stores a new cooldown; type and owner are the string forms the Cooldown record holds. */
    void createCooldown(@Nullable String tablePrefix, @NotNull String id, @NotNull String cooldownType, @NotNull String cooldownOwner, @NotNull Timestamp cooldownExpiry) throws IOException;

    void deleteCooldown(@Nullable String tablePrefix, @NotNull String cooldownType, @NotNull String cooldownOwner) throws IOException;
}
