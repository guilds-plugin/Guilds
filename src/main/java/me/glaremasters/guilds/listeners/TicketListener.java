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

import ch.jalu.configme.SettingsManager;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

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
    private SettingsManager settingsManager;

    /**
     * This even handles Guild TicketListener and how they are used by the player
     * @param event
     */
    @EventHandler
    public void upgradeTicket(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item == null)
            return;

        Player player = event.getPlayer();
        Guild guild = guildHandler.getGuild(player);

        if (guild == null)
            return;

        if (item.isSimilar(guildHandler.matchTicket(settingsManager))) {
            if (guildHandler.isMaxTier(guild)) {
                guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.UPGRADE__TIER_MAX);
                return;
            }
            if (item.getAmount() > 1)
                item.setAmount(item.getAmount() - 1);
            else
                player.getInventory().setItemInHand(new ItemStack(Material.AIR));

            event.setCancelled(true);

            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.UPGRADE__SUCCESS);
            guildHandler.upgradeTier(guild);
        }
    }
}
