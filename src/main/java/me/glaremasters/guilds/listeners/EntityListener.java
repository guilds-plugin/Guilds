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
package me.glaremasters.guilds.listeners;

import ch.jalu.configme.SettingsManager;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.utils.ClaimUtils;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by GlareMasters
 * Date: 7/19/2018
 * Time: 5:21 PM
 */
public class EntityListener implements Listener {

    private final GuildHandler guildHandler;
    private final SettingsManager settingsManager;
    private final ChallengeHandler challengeHandler;
    private final Set<PotionEffectType> bad = new HashSet<>(Arrays.asList(PotionEffectType.BLINDNESS, PotionEffectType.WITHER, PotionEffectType.SLOW_DIGGING, PotionEffectType.WEAKNESS, PotionEffectType.SLOW, PotionEffectType.POISON));

    public EntityListener(GuildHandler guildHandler, SettingsManager settingsManager, ChallengeHandler challengeHandler) {
        this.guildHandler = guildHandler;
        this.settingsManager = settingsManager;
        this.challengeHandler = challengeHandler;
    }

    /**
     * Handles the extra damage to a mob
     *
     * @param event
     */
    @EventHandler
    public void onMobDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Guild guild = guildHandler.getGuild(player);
            if (guild == null) {
                return;
            }
            double dmg = event.getDamage();
            double multiplier = guild.getTier().getDamageMultiplier();
            event.setDamage(dmg * multiplier);
        }
    }

    /**
     * Handles extra XP dropped from mobs
     *
     * @param event
     */
    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        Monster monster = (Monster) event.getEntity();
        Player killer = monster.getKiller();
        if (killer == null) return;
        Guild guild = guildHandler.getGuild(killer);
        if (guild == null) {
            return;
        }
        double xp = event.getDroppedExp();
        double multiplier = guild.getTier().getMobXpMultiplier();
        event.setDroppedExp((int) Math.round(xp * multiplier));
    }

    /**
     * Guild / Ally damage handlers
     *
     * @param event handles when damage is done between two players that might be in the same guild or are allies
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        // Check if they are the same guild
        if (guildHandler.isSameGuild(player, damager) && !player.hasPermission("guilds.ffa.guild")) {
            event.setCancelled(!settingsManager.getProperty(GuildSettings.GUILD_DAMAGE));
            return;
        }

        // Get the challenge object
        GuildChallenge challenge = challengeHandler.getChallenge(player);

        // Check if they are in a challenge
        if (challenge != null) {
            // Check if the challenge has started
            if (challenge.isStarted()) {
                // Cancel the rest of the checks in case they are battling allies
                return;
            }
        }

        if (guildHandler.isAlly(player, damager) && !player.hasPermission("guilds.ffa.ally")) {
            event.setCancelled(!settingsManager.getProperty(GuildSettings.ALLY_DAMAGE));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Projectile)) {
            return;
        }

        Projectile projectile = (Projectile) event.getDamager();

        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }

        Player damaged = (Player) event.getEntity();
        Player damager = (Player) projectile.getShooter();

        if (guildHandler.isSameGuild(damaged, damager) && damaged != damager && !damaged.hasPermission("guilds.ffa.guild")) {
            event.setCancelled(!settingsManager.getProperty(GuildSettings.GUILD_DAMAGE));
            return;
        }

        if (guildHandler.isAlly(damaged, damager) && !damaged.hasPermission("guilds.ffa.ally")) {
            event.setCancelled(!settingsManager.getProperty(GuildSettings.ALLY_DAMAGE));
        }
    }

    /**
     * Handles flame arrows
     *
     * @param event
     */
    @EventHandler
    public void onFlameArrow(EntityCombustByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player))
            return;

        if (!(event.getCombuster() instanceof Arrow))
            return;

        Arrow arrow = (Arrow) event.getCombuster();

        if (!(arrow.getShooter() instanceof Player))
            return;

        Player damagee = (Player) event.getEntity();
        Player damager = (Player) arrow.getShooter();

        if (guildHandler.isSameGuild(damagee, damager) && !damagee.hasPermission("guilds.ffa.guild")) {
            arrow.setFireTicks(0);
            event.setCancelled(!settingsManager.getProperty(GuildSettings.GUILD_DAMAGE));
            return;
        }

        if (guildHandler.isAlly(damagee, damager) && !damagee.hasPermission("guilds.ffa.ally")) {
            arrow.setFireTicks(0);
            event.setCancelled(!settingsManager.getProperty(GuildSettings.ALLY_DAMAGE));
        }
    }

    /**
     * Handles splash potions+
     *
     * @param event
     */
    @EventHandler
    public void onSplash(PotionSplashEvent event) {
        if (event.isCancelled()) {
            return;
        }
        boolean isHarming = false;
        for (PotionEffect effect : event.getPotion().getEffects()) {
            if (bad.contains(effect.getType())) {
                isHarming = true;
                break;
            }
        }

        if (!isHarming)
            return;

        ThrownPotion potion = event.getPotion();

        if (!(potion.getShooter() instanceof Player))
            return;

        Player shooter = (Player) potion.getShooter();

        for (LivingEntity entity : event.getAffectedEntities()) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (guildHandler.isSameGuild(shooter, player) && potion.getShooter() != player && !shooter.hasPermission("guilds.ffa.guild")) {
                    event.setCancelled(!settingsManager.getProperty(GuildSettings.GUILD_DAMAGE));
                    return;
                }
                if (guildHandler.isAlly(shooter, player) && !shooter.hasPermission("guilds.ffa.ally")) {
                    event.setCancelled(!settingsManager.getProperty(GuildSettings.ALLY_DAMAGE));
                }
            }
        }

    }


}
