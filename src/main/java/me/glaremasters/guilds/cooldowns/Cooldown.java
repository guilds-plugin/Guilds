package me.glaremasters.guilds.cooldowns;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.UUID;

/**
 * A class representing a cooldown for a specific action.
 */
public class Cooldown {
    private final UUID cooldownId;
    private final Type cooldownType;
    private final UUID cooldownOwner;
    private final Long cooldownExpiry;

    /**
     * Constructs a new `Cooldown` instance.
     *
     * @param cooldownId     the unique ID of the cooldown
     * @param cooldownType   the type of cooldown (e.g., "home", "buffs", etc.)
     * @param cooldownOwner  the UUID of the owner of the cooldown
     * @param cooldownExpiry the time when the cooldown will expire
     * @throws NullPointerException if `cooldownType` or `cooldownOwner` is `null`
     */
    public Cooldown(UUID cooldownId, Type cooldownType, UUID cooldownOwner, Long cooldownExpiry) {
        Preconditions.checkNotNull(cooldownType, "cooldown type");
        Preconditions.checkNotNull(cooldownOwner, "cooldown owner");
        this.cooldownId = cooldownId;
        this.cooldownType = cooldownType;
        this.cooldownOwner = cooldownOwner;
        this.cooldownExpiry = cooldownExpiry;
    }

    /**
     * Gets the unique ID of the cooldown.
     *
     * @return the unique ID of the cooldown
     */
    public UUID getCooldownId() {
        return cooldownId;
    }

    /**
     * Gets the type of cooldown.
     *
     * @return the type of cooldown
     */
    public Type getCooldownType() {
        return cooldownType;
    }

    /**
     * Gets the UUID of the owner of the cooldown.
     *
     * @return the UUID of the owner of the cooldown
     */
    public UUID getCooldownOwner() {
        return cooldownOwner;
    }

    /**
     * Gets the time when the cooldown will expire.
     *
     * @return the time when the cooldown will expire
     */
    public Long getCooldownExpiry() {
        return cooldownExpiry;
    }

    /**
     * Enum representing the types of cooldowns.
     */
    public enum Type {
        /**
         * Represents the "request" cooldown type.
         */
        Request("request"),

        /**
         * Represents the "sethome" cooldown type.
         */
        SetHome("sethome"),

        /**
         * Represents the "home" cooldown type.
         */
        Home("home"),

        /**
         * Represents the "buffs" cooldown type.
         */
        Buffs("buffs"),

        /**
         * Represents the "join" cooldown type.
         */
        Join("join");

        private final String typeName;

        /**
         * Constructs a new `Type` enum value with the specified type name.
         *
         * @param typeName The name of the type.
         */
        Type(String typeName) {
            this.typeName = typeName;
        }

        /**
         * Returns the name of the type.
         *
         * @return The name of the type.
         */
        public String getTypeName() {
            return typeName;
        }

        /**
         * Returns the `Type` enum value with the specified type name.
         *
         * @param typeName The name of the type.
         * @return The `Type` enum value with the specified type name, or `null` if not found.
         */
        public static Type getByTypeName(String typeName) {
            return Arrays.stream(values()).filter(c -> c.getTypeName().equalsIgnoreCase(typeName)).findFirst().orElse(null);
        }
    }
}
