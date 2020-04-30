/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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

// TODO: from simple -
//  I created a bunch of shims in here so I didn't have to go around changing all kinds of references to methods.
//  They are the ones that are taking cooldown types by name. I just resolve the cooldown type and pass it to the new method.

/**
 * Created by Glare
 * Date: 5/15/2019
 * Time: 9:40 AM
 */
public class CooldownHandler {
    private final ExpiringMap<UUID, Cooldown> cooldowns;
    private final Guilds guilds;

    public CooldownHandler(Guilds guilds) {
        this.guilds = guilds;
        cooldowns = ExpiringMap.builder().variableExpiration().expirationListener((key, cooldown) -> {
        }).build();

        Guilds.newChain().async(() -> {
            try {
                List<Cooldown> saved = guilds.getDatabase().getCooldownAdapter().getAllCooldowns();
                for (Cooldown cooldown : saved) {
                    // If the time in the cooldown is LESS THAN the current time, then that time has already passed
                    if (cooldown.getCooldownExpiry() < System.currentTimeMillis()) {
                        // Since the time has already passed, let's remove it
                        guilds.getDatabase().getCooldownAdapter().deleteCooldown(cooldown);
                    } else {
                        // The time has not past, we won't remove it
                        addCooldown(cooldown);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).execute();
    }

    /**
     * Save the cooldowns
     * @throws IOException
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
    public Cooldown getCooldown(@NotNull Cooldown.Type cooldownType, @NotNull UUID cooldownOwner) {
        return cooldowns.values().stream().filter(c -> c.getCooldownType() == cooldownType && c.getCooldownOwner().equals(cooldownOwner)).findFirst().orElse(null);
    }

    /**
     * Check if a cooldown is still valid
     * @param cooldownType the cooldown type name
     * @param cooldownOwner the owner UUID of the cooldown
     * @return true or false
     */
    public boolean hasCooldown(@NotNull String cooldownType, @NotNull UUID cooldownOwner) {
        return hasCooldown(Cooldown.Type.getByTypeName(cooldownType), cooldownOwner);
    }

    /**
     * Check if a cooldown is still valid
     * @param cooldownType the cooldown type
     * @param cooldownOwner the owner UUID of the cooldown
     * @return true or false
     */
    public boolean hasCooldown(@NotNull Cooldown.Type cooldownType, @NotNull UUID cooldownOwner) {
        Cooldown cooldown = getCooldown(cooldownType, cooldownOwner);
        return cooldown != null && cooldown.getCooldownExpiry() > System.currentTimeMillis();
    }

    public int getRemaining(String cooldownType, UUID cooldownOwner) {
        return getRemaining(Cooldown.Type.getByTypeName(cooldownType), cooldownOwner);
    }

    /**
     * Get the time remaining in seconds
     * @param cooldownType the type of cooldown
     * @param cooldownOwner the uuid to check
     * @return time left
     */
    public int getRemaining(Cooldown.Type cooldownType, UUID cooldownOwner) {
        int int1 = Integer.parseInt(String.format("%d", TimeUnit.MILLISECONDS.toSeconds(getCooldown(cooldownType, cooldownOwner).getCooldownExpiry())));
        int int2 = Integer.parseInt(String.format("%d", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())));
        return int1 - int2;
    }

    /**
     * Add a player to the cooldown
     * @param player the player to add
     * @param type the type of cooldown
     * @param length how long to put them
     */
    public void addCooldown(OfflinePlayer player, String type, int length, TimeUnit timeUnit) {
        addCooldown(Cooldown.Type.getByTypeName(type), player.getUniqueId(), length, timeUnit);
    }

    /**
     * Add a guild to a cooldown
     * @param guild the guild to add
     * @param type the type of cooldown
     * @param length thje length of the cooldown
     */
    public void addCooldown(Guild guild, String type, int length, TimeUnit timeUnit) {
        addCooldown(Cooldown.Type.getByTypeName(type), guild.getId(), length, timeUnit);
    }

    /**
     * Add a cooldown
     * @param cooldownType the cooldown type
     * @param cooldownOwner the cooldown owner UUID
     * @param length length of time
     * @param timeUnit unit of time
     */
    public void addCooldown(Cooldown.Type cooldownType, UUID cooldownOwner, int length, TimeUnit timeUnit) {
        Cooldown cooldown = new Cooldown(cooldownType, cooldownOwner, (System.currentTimeMillis() + timeUnit.toMillis(length)));
        cooldowns.put(cooldown.getCooldownId(), cooldown, ExpirationPolicy.CREATED, length, timeUnit);
    }

    private void addCooldown(Cooldown cooldown) {
        long remainingSeconds = cooldown.getCooldownExpiry() - System.currentTimeMillis();
        if (remainingSeconds < 0) return;
        cooldowns.put(cooldown.getCooldownId(), cooldown, ExpirationPolicy.CREATED, TimeUnit.MILLISECONDS.toSeconds(remainingSeconds), TimeUnit.SECONDS);
    }

    public ExpiringMap<UUID, Cooldown> getCooldowns() {
        return cooldowns;
    }
}
