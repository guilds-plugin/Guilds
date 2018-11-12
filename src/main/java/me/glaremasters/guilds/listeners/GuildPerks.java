package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.guild.Guild;
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
public class GuildPerks implements Listener {

    /**
     * Damage Multiplier Handler
     * @param event this event handles the boost that a Guild gets if they have a damage multiplier
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Guild guild = Guild.getGuild(player.getUniqueId());
            if (guild != null) event.setDamage((int) (event.getDamage() * guild.getDamageMultiplier()));
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
        Guild guild = Guild.getGuild(killer.getUniqueId());
        if (guild != null) event.setDroppedExp((int) (event.getDroppedExp() * guild.getExpMultiplier()));
    }


}
