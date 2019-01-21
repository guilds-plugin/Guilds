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
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.Messages;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by GlareMasters
 * Date: 9/27/2018
 * Time: 7:08 PM
 */
@AllArgsConstructor
public class TicketListener implements Listener {

    //todo


    private Guilds guilds;
    private GuildHandler guildHandler;

    /**
     * This even handles Guild TicketListener and how they are used by the player
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
        Guild guild = guildHandler.getGuild(player);
        if (guild == null) return;
        if (guild.getTier().getLevel() >= getInt("max-number-of-tiers")) {
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
