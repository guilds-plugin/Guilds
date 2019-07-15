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

package me.glaremasters.guilds.listeners;

import ch.jalu.configme.SettingsManager;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.utils.ClaimUtils;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
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
@AllArgsConstructor
public class EntityListener implements Listener {

    private GuildHandler guildHandler;
    private SettingsManager settingsManager;
    private final Set<PotionEffectType> bad = new HashSet<>(Arrays.asList(PotionEffectType.BLINDNESS, PotionEffectType.WITHER, PotionEffectType.SLOW_DIGGING, PotionEffectType.WEAKNESS, PotionEffectType.SLOW, PotionEffectType.POISON));

    /**
     * Handles the extra damage to a mob
     * @param event
     */
    @EventHandler
    public void onMobDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Guild guild = guildHandler.getGuild(player);
            double dmg = event.getDamage();
            double multiplier = guild.getTier().getDamageMultiplier();
            if (guild != null) event.setDamage(dmg * multiplier);
        }
    }

    /**
     * Handles extra XP dropped from mobs
     * @param event
     */
    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        Monster monster = (Monster) event.getEntity();
        Player killer = monster.getKiller();
        if (killer == null) return;
        Guild guild = guildHandler.getGuild(killer);
        double xp = event.getDroppedExp();
        double multiplier = guild.getTier().getMobXpMultiplier();
        if (guild != null) event.setDroppedExp((int) Math.round(xp * multiplier));
    }

    /**
     * Guild / Ally damage handlers
     * @param event handles when damage is done between two players that might be in the same guild or are allies
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        if (settingsManager.getProperty(GuildSettings.RESPECT_WG_PVP_FLAG)) {
            event.setCancelled(ClaimUtils.checkPvpDisabled(player));
            return;
        }

        if (guildHandler.isSameGuild(player, damager)) {
            event.setCancelled(!settingsManager.getProperty(GuildSettings.GUILD_DAMAGE));
            return;
        }
        if (guildHandler.isAlly(player, damager)) {
            event.setCancelled(!settingsManager.getProperty(GuildSettings.ALLY_DAMAGE));
        }
    }

    @EventHandler
    public void onArrow(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player))
            return;

        if (!(event.getDamager() instanceof Arrow))
            return;

        Arrow arrow = (Arrow) event.getDamager();

        if (!(arrow.getShooter() instanceof Player))
            return;

        Player damagee = (Player) event.getEntity();
        Player damager = (Player) arrow.getShooter();

        if (settingsManager.getProperty(GuildSettings.RESPECT_WG_PVP_FLAG)) {
            event.setCancelled(ClaimUtils.checkPvpDisabled(damagee));
            return;
        }

        if (guildHandler.isSameGuild(damagee, damager) && damagee != damager ) {
            event.setCancelled(!settingsManager.getProperty(GuildSettings.GUILD_DAMAGE));
            return;
        }

        if (guildHandler.isAlly(damagee, damager)) {
            event.setCancelled(!settingsManager.getProperty(GuildSettings.ALLY_DAMAGE));
        }

    }

    /**
     * Handles flame arrows
     * @param event
     */
    @EventHandler
    public void onFlameArrow(EntityCombustByEntityEvent event) {

        if (!(event.getEntity() instanceof Player))
            return;

        if (!(event.getCombuster() instanceof Arrow))
            return;

        Arrow arrow = (Arrow) event.getCombuster();

        if (!(arrow.getShooter() instanceof Player))
            return;

        Player damagee = (Player) event.getEntity();
        Player damager = (Player) arrow.getShooter();

        if (settingsManager.getProperty(GuildSettings.RESPECT_WG_PVP_FLAG)) {
            event.setCancelled(ClaimUtils.checkPvpDisabled(damagee));
            return;
        }

        if (guildHandler.isSameGuild(damagee, damager)) {
            arrow.setFireTicks(0);
            event.setCancelled(!settingsManager.getProperty(GuildSettings.GUILD_DAMAGE));
            return;
        }

        if (guildHandler.isAlly(damagee, damager)) {
            arrow.setFireTicks(0);
            event.setCancelled(!settingsManager.getProperty(GuildSettings.ALLY_DAMAGE));
        }
    }

    /**
     * Handles splash potions+
     * @param event
     */
    @EventHandler
    public void onSplash(PotionSplashEvent event) {
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
            if (entity instanceof Player)  {
                Player player = (Player) entity;
                if (guildHandler.isSameGuild(shooter, player) && potion.getShooter() != player ) {
                    event.setCancelled(!settingsManager.getProperty(GuildSettings.GUILD_DAMAGE));
                    return;
                }
                if (guildHandler.isAlly(shooter, player)) {
                    event.setCancelled(!settingsManager.getProperty(GuildSettings.ALLY_DAMAGE));
                }
            }
        }

    }


}
