/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.glaremasters.guilds.cooldowns;

import co.aikar.commands.lib.expiringmap.ExpirationPolicy;
import co.aikar.commands.lib.expiringmap.ExpiringMap;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Handles cooldowns for players and guilds.
 */
public class CooldownHandler {
    private final ExpiringMap<UUID, Cooldown> cooldowns = ExpiringMap.builder().variableExpiration().expirationListener((key, cooldown) -> {
    }).build();
    private final Guilds guilds;

    /**
     * Creates a new instance of the {@code CooldownHandler} class.
     *
     * @param guilds the {@code Guilds} instance.
     */
    public CooldownHandler(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Loads all the cooldowns from the database when the plugin first enables.
     */
    public void loadCooldowns() {
        try {
            final List<Cooldown> saved = guilds.getDatabase().getCooldownAdapter().getAllCooldowns();
            for (final Cooldown cooldown : saved) {
                if (cooldown.getCooldownExpiry() < System.currentTimeMillis()) {
                    guilds.getDatabase().getCooldownAdapter().deleteCooldown(cooldown);
                } else {
                    addCooldown(cooldown);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves all the cooldowns to the database.
     */
    public void saveCooldowns() {
        guilds.getDatabase().getCooldownAdapter().saveCooldowns(cooldowns.values());
    }

    /**
     * Gets a cooldown from the list.
     *
     * @param cooldownType  the type of cooldown.
     * @param cooldownOwner the owner UUID of the cooldown.
     * @return the {@code Cooldown} object if found, otherwise {@code null}.
     */
    public Cooldown getCooldown(@NotNull final Cooldown.Type cooldownType, @NotNull final UUID cooldownOwner) {
        return cooldowns.values().stream().filter(c -> c.getCooldownType() == cooldownType && c.getCooldownOwner().equals(cooldownOwner)).findFirst().orElse(null);
    }

    /**
     * Checks if a cooldown is still valid.
     *
     * @param cooldownType  the name of the cooldown type.
     * @param cooldownOwner the owner UUID of the cooldown.
     * @return {@code true} if the cooldown is still valid, otherwise {@code false}.
     */
    public boolean hasCooldown(@NotNull final String cooldownType, @NotNull final UUID cooldownOwner) {
        return hasCooldown(Cooldown.Type.getByTypeName(cooldownType), cooldownOwner);
    }

    /**
     * Check if a cooldown is still valid
     *
     * @param cooldownType  The type of cooldown to check.
     * @param cooldownOwner The owner UUID of the cooldown.
     * @return `true` if the cooldown is still valid, `false` otherwise.
     */
    public boolean hasCooldown(@NotNull final Cooldown.Type cooldownType, @NotNull final UUID cooldownOwner) {
        final Cooldown cooldown = getCooldown(cooldownType, cooldownOwner);
        return cooldown != null && cooldown.getCooldownExpiry() > System.currentTimeMillis();
    }

    /**
     * Get the remaining time of the cooldown.
     *
     * @param cooldownType  The string representation of the type of the cooldown.
     * @param cooldownOwner The UUID of the owner of the cooldown.
     * @return The remaining time of the cooldown in seconds.
     */
    public int getRemaining(@NotNull final String cooldownType, @NotNull final UUID cooldownOwner) {
        return getRemaining(Cooldown.Type.getByTypeName(cooldownType), cooldownOwner);
    }

    /**
     * Get the remaining time of the cooldown in seconds.
     *
     * @param cooldownType  The type of cooldown to check.
     * @param cooldownOwner The owner UUID of the cooldown.
     * @return The remaining time in seconds.
     */
    public int getRemaining(@NotNull final Cooldown.Type cooldownType, @NotNull final UUID cooldownOwner) {
        final int int1 = Integer.parseInt(String.format("%d", TimeUnit.MILLISECONDS.toSeconds(getCooldown(cooldownType, cooldownOwner).getCooldownExpiry())));
        final int int2 = Integer.parseInt(String.format("%d", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())));
        return int1 - int2;
    }

    /**
     * Adds a player to the cooldown.
     *
     * @param player   The player to add to the cooldown.
     * @param type     The type of cooldown to add.
     * @param length   The length of the cooldown in the specified time unit.
     * @param timeUnit The time unit of the `length` parameter.
     */
    public void addCooldown(@NotNull final OfflinePlayer player, @NotNull final String type, final int length, @NotNull final TimeUnit timeUnit) {
        addCooldown(Cooldown.Type.getByTypeName(type), player.getUniqueId(), length, timeUnit);
    }

    /**
     * Adds a guild to the cooldown.
     *
     * @param guild    The guild to add to the cooldown.
     * @param type     The type of cooldown to add.
     * @param length   The length of the cooldown in the specified time unit.
     * @param timeUnit The time unit of the `length` parameter.
     */
    public void addCooldown(@NotNull final Guild guild, @NotNull final String type, final int length, @NotNull final TimeUnit timeUnit) {
        addCooldown(Cooldown.Type.getByTypeName(type), guild.getId(), length, timeUnit);
    }

    /**
     * Adds a cooldown.
     *
     * @param cooldownType  The type of cooldown to add.
     * @param cooldownOwner The owner UUID of the cooldown.
     * @param length        The length of the cooldown in the specified time unit.
     * @param timeUnit      The time unit of the `length` parameter.
     */
    public void addCooldown(@NotNull final Cooldown.Type cooldownType, @NotNull final UUID cooldownOwner, final int length, @NotNull final TimeUnit timeUnit) {
        final Cooldown cooldown = new Cooldown(UUID.randomUUID(), cooldownType, cooldownOwner, (System.currentTimeMillis() + timeUnit.toMillis(length)));
        cooldowns.put(cooldown.getCooldownId(), cooldown, ExpirationPolicy.CREATED, length, timeUnit);
    }

    /**
     * Adds a cooldown.
     *
     * @param cooldown The cooldown to add.
     */
    private void addCooldown(@NotNull final Cooldown cooldown) {
        final long remainingSeconds = cooldown.getCooldownExpiry() - System.currentTimeMillis();
        if (remainingSeconds < 0) return;
        cooldowns.put(cooldown.getCooldownId(), cooldown, ExpirationPolicy.CREATED, TimeUnit.MILLISECONDS.toSeconds(remainingSeconds), TimeUnit.SECONDS);
    }

    public ExpiringMap<UUID, Cooldown> getCooldowns() {
        return cooldowns;
    }
}
