package me.glaremasters.guilds.listeners;

import static me.glaremasters.guilds.util.ConfigUtil.getInt;
import static me.glaremasters.guilds.util.ConfigUtil.getString;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


/**
 * Created by GlareMasters on 8/29/2017.
 */
public class TicketListener implements Listener {

    private String ticketName = getString("upgrade-ticket.name");
    private String ticketMaterial = getString("upgrade-ticket.material");
    private String ticketLore =getString("upgrade-ticket.lore");
    private Guilds guilds;

    public TicketListener(Guilds guilds) {
        this.guilds = guilds;
    }

    @EventHandler
    public void upgradeTicket(PlayerInteractEvent event) {

        ItemStack item = event.getItem();

        if (item == null || !item.getType().toString().equals(ticketMaterial)) return;

        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();

        if (!meta.hasDisplayName()) return;

        if (!meta.getDisplayName().equals(ticketName)) return;

        if (!meta.hasLore()) return;

        if (!meta.getLore().get(0).equals(ticketLore)) return;

        Player player = event.getPlayer();

        Guild guild = Guild.getGuild(player.getUniqueId());

        if (guild == null) return;

        int tier = guild.getTier();

        if (tier >= getInt("max-number-of-tiers")) {
            Message.sendMessage(player, Message.COMMAND_UPGRADE_TIER_MAX);
            return;
        }

        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
        event.setCancelled(true);
        Message.sendMessage(player, Message.COMMAND_UPGRADE_SUCCESS);
        guilds.guildTiersConfig.set(guild.getName(), tier + 1);
        guilds.saveGuildData();
        guild.updateGuild("");
    }
}

