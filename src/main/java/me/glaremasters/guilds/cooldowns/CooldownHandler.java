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

import me.glaremasters.guilds.database.cooldowns.CooldownsProvider;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Glare
 * Date: 5/15/2019
 * Time: 9:40 AM
 */
public class CooldownHandler {

    private Map<String, Cooldown> cooldowns;
    private final CooldownsProvider cooldownsProvider;

    public CooldownHandler(CooldownsProvider cooldownsProvider) throws FileNotFoundException {
        this.cooldownsProvider = cooldownsProvider;

        cooldowns = cooldownsProvider.loadCooldowns();
    }

    /**
     * Save the cooldowns
     * @throws IOException
     */
    public void saveCooldowns() throws IOException {
        removeExcess();
        cooldownsProvider.saveCooldowns(cooldowns);
    }

    /**
     * Add a new cooldown type to the plugin
     * @param type the type of cooldown
     */
    public void addCooldownType(String type) {
        if (cooldowns.keySet().contains(type)) {
            return;
        }
        cooldowns.put(type, new Cooldown());
    }

    /**
     * Get a cooldown from the list
     * @param type the type of cooldown
     * @return cooldown
     */
    public Cooldown getCooldown(@NotNull String type) {
        return cooldowns.get(type);
    }

    public boolean hasCooldown(String type, UUID uuid) {
        Long expire = getCooldown(type).getUuids().get(uuid);
        return expire != null && expire > System.currentTimeMillis();
    }

    /**
     * Remove old map entires.
     */
    public void removeExcess() {
        long current = System.currentTimeMillis();
        cooldowns.values().forEach(cooldown -> cooldown.getUuids().values().removeIf(time -> time < current));
    }

    /**
     * Remove a player from cooldown
     * @param type the type of cooldown
     * @param uuid the uuid to check
     */
    public void removeCooldown(String type, UUID uuid) {
        getCooldown(type).getUuids().remove(uuid);
    }

    /**
     * Get the time remaining in seconds
     * @param type the type of cooldown
     * @param uuid the uuid to check
     * @return time left
     */
    public int getRemaining(String type, UUID uuid) {
        int int1 = Integer.valueOf(String.format("%d", TimeUnit.MILLISECONDS.toSeconds(getCooldown(type).getUuids().get(uuid))));
        int int2 = Integer.valueOf(String.format("%d", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())));
        return int1 - int2;
    }

    /**
     * Add a player to the cooldown
     * @param player the player to add
     * @param type the type of cooldown
     * @param length how long to put them
     */
    public void addCooldown(OfflinePlayer player, String type, int length, TimeUnit timeUnit) {
        getCooldown(type).getUuids().put(player.getUniqueId(), (System.currentTimeMillis() + timeUnit.toMillis(length)));
    }

    /**
     * Add a guild to a cooldown
     * @param guild the guild to add
     * @param type the type of cooldown
     * @param length thje length of the cooldown
     */
    public void addCooldown(Guild guild, String type, int length, TimeUnit timeUnit) {
        getCooldown(type).getUuids().put(guild.getId(), (System.currentTimeMillis() + timeUnit.toMillis(length)));
    }

    /**
     * Create the cooldowns for the plugin
     */
    public void createCooldowns() {
        for (Cooldown.TYPES type : Cooldown.TYPES.values()) {
            addCooldownType(type.name());
        }
    }

}
