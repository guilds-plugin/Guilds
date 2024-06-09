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
package me.glaremasters.guilds.listeners;

import ch.jalu.configme.SettingsManager;
import com.cryptomorin.xseries.XPotion;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.guild.GuildHandler;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
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

/**
 * Created by GlareMasters
 * Date: 7/19/2018
 * Time: 5:21 PM
 */
public class EntityListener implements Listener {

    private final GuildHandler guildHandler;
    private final SettingsManager settingsManager;
    private final ChallengeHandler challengeHandler;

    public EntityListener(GuildHandler guildHandler, SettingsManager settingsManager, ChallengeHandler challengeHandler) {
        this.guildHandler = guildHandler;
        this.settingsManager = settingsManager;
        this.challengeHandler = challengeHandler;
    }

    /**
     * Handles the extra damage to a mob.
     *
     * @param event The EntityDamageByEntityEvent that triggered the method.
     */
    @EventHandler
    public void onMobDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final Entity damager = event.getDamager();
        if (!(damager instanceof Player)) {
            return;
        }

        final Player player = (Player) damager;
        final Guild guild = guildHandler.getGuild(player);
        if (guild == null) {
            return;
        }

        final double damage = event.getDamage();
        final double multiplier = guild.getTier().getDamageMultiplier();
        event.setDamage(damage * multiplier);
    }

    /**
     * Handles extra XP dropped from mobs
     *
     * @param event The EntityDeathEvent that triggered the method.
     */
    public void onMobDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }

        final Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }

        final Guild guild = guildHandler.getGuild(killer);
        if (guild == null) {
            return;
        }

        final double xp = event.getDroppedExp();
        final double multiplier = guild.getTier().getMobXpMultiplier();
        event.setDroppedExp((int) Math.round(xp * multiplier));
    }

    /**
     * Handles restrictions on player vs player damage based on guild affiliation.
     *
     * @param event the event triggered by player vs player damage
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getEntity();
        final Player damager = (Player) event.getDamager();

        // Check if they are in the same guild and have permission to damage their guild members
        if (guildHandler.isSameGuild(player, damager) && !player.hasPermission("guilds.ffa.guild")) {
            event.setCancelled(!settingsManager.getProperty(GuildSettings.GUILD_DAMAGE));
            return;
        }

        // Get the challenge object for the player
        final GuildChallenge challenge = challengeHandler.getChallenge(player);

        // Check if they are in a challenge
        if (challenge != null && challenge.isStarted()) {
            // Cancel the rest of the checks in case they are battling allies
            return;
        }

        // Check if they are allies and have permission to damage allies
        if (guildHandler.isAlly(player, damager) && !player.hasPermission("guilds.ffa.ally")) {
            event.setCancelled(!settingsManager.getProperty(GuildSettings.ALLY_DAMAGE));
        }
    }

    /**
     * Handles damage caused by Projectile (e.g. arrow)
     *
     * @param event the event that holds information about the damage caused by a projectile
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Projectile)) {
            return;
        }

        final Projectile projectile = (Projectile) event.getDamager();

        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }

        final Player damaged = (Player) event.getEntity();
        final Player damager = (Player) projectile.getShooter();

        // Check if they are in the same guild
        if (guildHandler.isSameGuild(damaged, damager) && damaged != damager && !damaged.hasPermission("guilds.ffa.guild")) {
            event.setCancelled(!settingsManager.getProperty(GuildSettings.GUILD_DAMAGE));
            return;
        }

        // Check if they are allies
        if (guildHandler.isAlly(damaged, damager) && !damaged.hasPermission("guilds.ffa.ally")) {
            event.setCancelled(!settingsManager.getProperty(GuildSettings.ALLY_DAMAGE));
        }
    }

    /**
     * Handles flame arrow damage between players
     *
     * @param event the EntityCombustByEntityEvent fired when a player is set on fire by an arrow
     */
    @EventHandler
    public void onFlameArrow(EntityCombustByEntityEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player) || !(event.getCombuster() instanceof Arrow)) {
            return;
        }

        final Arrow arrow = (Arrow) event.getCombuster();
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        final Player damagee = (Player) event.getEntity();
        final Player damager = (Player) arrow.getShooter();

        // Check if they are in the same guild
        if (guildHandler.isSameGuild(damagee, damager) && !damagee.hasPermission("guilds.ffa.guild")) {
            arrow.setFireTicks(0);
            event.setCancelled(!settingsManager.getProperty(GuildSettings.GUILD_DAMAGE));
            return;
        }

        // Check if they are allies
        if (guildHandler.isAlly(damagee, damager) && !damagee.hasPermission("guilds.ffa.ally")) {
            arrow.setFireTicks(0);
            event.setCancelled(!settingsManager.getProperty(GuildSettings.ALLY_DAMAGE));
        }
    }

    /**
     * Handles harm causing splash potions.
     *
     * @param event The event fired when a splash potion affects entities
     */
    @EventHandler
    public void onSplash(PotionSplashEvent event) {
        if (event.isCancelled()) {
            return;
        }

        final boolean isHarming = isHarmfulPotion(event.getPotion());

        if (!isHarming) {
            return;
        }

        final ThrownPotion potion = event.getPotion();

        if (!(potion.getShooter() instanceof Player)) {
            return;
        }

        final Player shooter = (Player) potion.getShooter();
        handleSplashDamage(shooter, event);
    }

    /**
     * Check if the given potion is a harmful potion
     *
     * @param potion the potion to check
     * @return true if the potion is harmful, false otherwise
     */
    private boolean isHarmfulPotion(final ThrownPotion potion) {
        for (final PotionEffect effect : potion.getEffects()) {
            if (XPotion.DEBUFFS.contains(XPotion.matchXPotion(effect.getType()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handle the damage caused by a harmful splash potion
     *
     * @param shooter the shooter of the splash potion
     * @param event the splash potion event
     */
    private void handleSplashDamage(final Player shooter, final PotionSplashEvent event) {
        for (final LivingEntity entity : event.getAffectedEntities()) {
            if (entity instanceof Player) {
                final Player player = (Player) entity;
                if (guildHandler.isSameGuild(shooter, player) && shooter != player && !shooter.hasPermission("guilds.ffa.guild")) {
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
