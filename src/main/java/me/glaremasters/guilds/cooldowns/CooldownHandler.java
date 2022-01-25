/*
 * MIT License
 *
 * Copyright (c) 2022 Glare
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
 * Created by Glare
 * Date: 5/15/2019
 * Time: 9:40 AM
 */
public class CooldownHandler {
    private final ExpiringMap<UUID, Cooldown> cooldowns = ExpiringMap.builder().variableExpiration().expirationListener((key, cooldown) -> {}).build();
    private final Guilds guilds;

    public CooldownHandler(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Called when the plugin first enables to load all the data
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
     * Save the cooldowns
     */
    public void saveCooldowns() {
        guilds.getDatabase().getCooldownAdapter().saveCooldowns(cooldowns.values());
    }

    /**
     * Get a cooldown from the list
     * @param cooldownType the cooldown type
     * @param cooldownOwner the owner UUID of the cooldown
     * @return cooldown
     */
    public Cooldown getCooldown(@NotNull final Cooldown.Type cooldownType, @NotNull final UUID cooldownOwner) {
        return cooldowns.values().stream().filter(c -> c.getCooldownType() == cooldownType && c.getCooldownOwner().equals(cooldownOwner)).findFirst().orElse(null);
    }

    /**
     * Check if a cooldown is still valid
     * @param cooldownType the cooldown type name
     * @param cooldownOwner the owner UUID of the cooldown
     * @return true or false
     */
    public boolean hasCooldown(@NotNull final String cooldownType, @NotNull final UUID cooldownOwner) {
        return hasCooldown(Cooldown.Type.getByTypeName(cooldownType), cooldownOwner);
    }

    /**
     * Check if a cooldown is still valid
     * @param cooldownType the cooldown type
     * @param cooldownOwner the owner UUID of the cooldown
     * @return true or false
     */
    public boolean hasCooldown(@NotNull final Cooldown.Type cooldownType, @NotNull final UUID cooldownOwner) {
        final Cooldown cooldown = getCooldown(cooldownType, cooldownOwner);
        return cooldown != null && cooldown.getCooldownExpiry() > System.currentTimeMillis();
    }

    public int getRemaining(@NotNull final String cooldownType, @NotNull final UUID cooldownOwner) {
        return getRemaining(Cooldown.Type.getByTypeName(cooldownType), cooldownOwner);
    }

    /**
     * Get the time remaining in seconds
     * @param cooldownType the type of cooldown
     * @param cooldownOwner the uuid to check
     * @return time left
     */
    public int getRemaining(@NotNull final Cooldown.Type cooldownType, @NotNull final UUID cooldownOwner) {
        final int int1 = Integer.parseInt(String.format("%d", TimeUnit.MILLISECONDS.toSeconds(getCooldown(cooldownType, cooldownOwner).getCooldownExpiry())));
        final int int2 = Integer.parseInt(String.format("%d", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())));
        return int1 - int2;
    }

    /**
     * Add a player to the cooldown
     * @param player the player to add
     * @param type the type of cooldown
     * @param length how long to put them
     */
    public void addCooldown(@NotNull final OfflinePlayer player, @NotNull final String type, final int length, @NotNull final TimeUnit timeUnit) {
        addCooldown(Cooldown.Type.getByTypeName(type), player.getUniqueId(), length, timeUnit);
    }

    /**
     * Add a guild to a cooldown
     * @param guild the guild to add
     * @param type the type of cooldown
     * @param length thje length of the cooldown
     */
    public void addCooldown(@NotNull final Guild guild, @NotNull final String type, final int length, @NotNull final TimeUnit timeUnit) {
        addCooldown(Cooldown.Type.getByTypeName(type), guild.getId(), length, timeUnit);
    }

    /**
     * Add a cooldown
     * @param cooldownType the cooldown type
     * @param cooldownOwner the cooldown owner UUID
     * @param length length of time
     * @param timeUnit unit of time
     */
    public void addCooldown(@NotNull final Cooldown.Type cooldownType, @NotNull final UUID cooldownOwner, final int length, @NotNull final TimeUnit timeUnit) {
        final Cooldown cooldown = new Cooldown(UUID.randomUUID(), cooldownType, cooldownOwner, (System.currentTimeMillis() + timeUnit.toMillis(length)));
        cooldowns.put(cooldown.getCooldownId(), cooldown, ExpirationPolicy.CREATED, length, timeUnit);
    }

    private void addCooldown(@NotNull final  Cooldown cooldown) {
        final long remainingSeconds = cooldown.getCooldownExpiry() - System.currentTimeMillis();
        if (remainingSeconds < 0) return;
        cooldowns.put(cooldown.getCooldownId(), cooldown, ExpirationPolicy.CREATED, TimeUnit.MILLISECONDS.toSeconds(remainingSeconds), TimeUnit.SECONDS);
    }

    public ExpiringMap<UUID, Cooldown> getCooldowns() {
        return cooldowns;
    }
}
