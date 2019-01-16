package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.GuildUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static me.glaremasters.guilds.utils.ConfigUtils.getInt;
import static me.glaremasters.guilds.utils.ConfigUtils.getString;

/**
 * Created by GlareMasters
 * Date: 9/27/2018
 * Time: 7:08 PM
 */
public class Tickets implements Listener {

    private Guilds guilds;
    private GuildUtils utils;

    public Tickets(Guilds guilds, GuildUtils utils) {
        this.guilds = guilds;
        this.utils = utils;
    }

    /**
     * This even handles Guild Tickets and how they are used by the player
     * @param event
     */
    @EventHandler
    public void upgradeTicket(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.getType().toString().equals(getString("upgrade-ticket.material"))) return;
        if (!item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return;
        if (!meta.getDisplayName().equals(getString("upgrade-ticket.name"))) return;
        if (!meta.hasLore()) return;
        if (!meta.getLore().get(0).equals(getString("upgrade-ticket.lore"))) return;
        Player player = event.getPlayer();
        Guild guild = utils.getGuild(player.getUniqueId());
        if (guild == null) return;
        if (guild.getTier() >= getInt("max-number-of-tiers")) {
            guilds.getManager().getCommandIssuer(player).sendInfo(Messages.UPGRADE__TIER_MAX);
            return;
        }
        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
        event.setCancelled(true);
        guilds.getManager().getCommandIssuer(player).sendInfo(Messages.UPGRADE__SUCCESS);
        guild.setTier((guild.getTier() + 1));

    }
}
