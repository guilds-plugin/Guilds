package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;


/**
 * Created by GlareMasters on 8/29/2017.
 */
public class TicketListener implements Listener {

    @EventHandler
    public void upgradeTicket(PlayerInteractEvent event) {
        FileConfiguration config = Main.getInstance().getConfig();
        String ticketName = ChatColor
                .translateAlternateColorCodes('&', config.getString("upgrade-ticket.name"));
        Player player = event.getPlayer();
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || !item.getItemMeta().hasDisplayName()) { return; }
        if (item.getItemMeta().getDisplayName().equalsIgnoreCase(ticketName)) {
            int tier = guild.getTier();
            if (tier >= config.getInt("max-number-of-tiers")) {
                Message.sendMessage(player, Message.COMMAND_UPGRADE_TIER_MAX);
                return;
            }
            player.getInventory().removeItem(event.getItem());
            player.updateInventory();
            Message.sendMessage(player, Message.COMMAND_UPGRADE_SUCCESS);
            Main.getInstance().guildTiersConfig.set(guild.getName(), tier + 1);
            Main.getInstance().saveGuildTiers();
            guild.updateGuild("");

        }
    }

}
