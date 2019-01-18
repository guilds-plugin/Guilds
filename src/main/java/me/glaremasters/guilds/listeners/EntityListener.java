/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
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

import lombok.AllArgsConstructor;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Created by GlareMasters
 * Date: 7/19/2018
 * Time: 5:21 PM
 */
@AllArgsConstructor
public class EntityListener implements Listener {

    //todo

    private GuildHandler guildHandler;

    /**
     * Damage Multiplier Handler
     * @param event this event handles the boost that a Guild gets if they have a damage multiplier
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Guild guild = guildHandler.getGuild(player);
            if (guild != null) event.setDamage((int) (event.getDamage() * guild.getTier().getDamageMultiplier()));
        }
    }

    /**
     * Exp Multiplier Handler
     * @param event this event handles the boost that a Guild gets if they have an exp multiplier
     */
    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        Monster monster = (Monster) event.getEntity();
        Player killer = monster.getKiller();
        if (killer == null) return;
        Guild guild = guildHandler.getGuild(killer);
        if (guild != null) event.setDroppedExp((event.getDroppedExp() * guild.getTier().getDamageMultiplier()));
    }


}
