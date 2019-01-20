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
import me.glaremasters.guilds.Messages;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRole;
import me.rayzr522.jsonmessage.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.material.Sign;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static me.glaremasters.guilds.utils.ConfigUtils.*;

/**
 * Created by GlareMasters
 * Date: 7/19/2018
 * Time: 5:31 PM
 */
@AllArgsConstructor
public class PlayerListener implements Listener {

    //todo

    private GuildHandler guildHandler;

    private Set<UUID> ALREADY_INFORMED = new HashSet<>();
    public static final Set<UUID> GUILD_CHAT_PLAYERS = new HashSet<>();

    /**
     * Guild / Ally damage handlers
     * @param event handles when damage is done between two players that might be in the same guild or are allies
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();
        Guild playerGuild = guildHandler.getGuild(player);
        Guild damagerGuild = guildHandler.getGuild(damager);
        if (playerGuild == null || damagerGuild == null) return;
        if (playerGuild.equals(damagerGuild)) event.setCancelled(!getBoolean("allow-guild-damage"));
        if (guilds.getGuildUtils().areAllies(player.getUniqueId(), damager.getUniqueId())) event.setCancelled(!getBoolean("allow-ally-damage"));
    }

    /**
     * This will check if a user is OP and will inform them of any important announcements from the Guild's Developer
     * @param event
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (guilds.getConfig().getBoolean("announcements.in-game")) {
            guilds.getServer().getScheduler().scheduleAsyncDelayedTask(guilds, () -> {
                if (player.isOp()) {
                    if (!ALREADY_INFORMED.contains(player.getUniqueId())) {
                        JSONMessage.create(color("&f[&aGuilds&f]&r Announcements (Hover over me for more information)")).tooltip(guilds.getAnnouncements()).openURL(guilds.getDescription().getWebsite()).send(player);
                        ALREADY_INFORMED.add(player.getUniqueId());
                    }
                }
            }, 70L);
        }
    }

    /**
     * Check if the sign being placed is to be turned into a Guild Sign
     * @param event
     */
    @EventHandler
    public void onSignPlace(SignChangeEvent event) {
        Sign sign = (Sign) event.getBlock().getState().getData();
        Block attached = event.getBlock().getRelative(sign.getAttachedFace());
        // Check if the sign is attached to a chest
        if (attached.getType() != Material.CHEST) return;
        // Check if it's a Guild Vault sign
        if (!event.getLine(0).equalsIgnoreCase("[Guild Vault]")) return;
        // Check if player has permission
        if (!event.getPlayer().hasPermission("guilds.command.admin")) {
            event.setCancelled(true);
            return;
        }
        // Send the message to the player saying it's been created
        guilds.getManager().getCommandIssuer(event.getPlayer()).sendInfo(Messages.ADMIN__GUILD_VAULT_SIGN);
    }

    /**
     * Check if the inventory being clicked on is part of the Guild Buff system
     * @param event
     */
    @EventHandler
    public void onBuffBuy(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Guild guild = utils.getGuild(player.getUniqueId());
        if (!event.getInventory().getTitle().equals(getString("gui-name.buff"))) return;
        if (event.getInventory().getTitle().equals(getString("gui-name.buff"))) event.setCancelled(true);
        if (event.getCurrentItem() == null) return;
        GuildBuff buff = GuildBuff.get(event.getCurrentItem().getType());
        double balance = guild.getBalance();
        if (buff == null) return;
        if (balance < buff.cost) {
            guilds.getManager().getCommandIssuer(player).sendInfo(Messages.BANK__NOT_ENOUGH_BANK);
            return;
        }
        if (getBoolean("disable-buff-stacking") && !player.getActivePotionEffects().isEmpty()) return;

        guild.getMembers()
                .stream()
                .map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()))
                .filter(OfflinePlayer::isOnline)
                .forEach(member -> ((Player) member).addPotionEffect(new PotionEffect(buff.potion, buff.time, buff.amplifier)));
        guild.setBalance(balance - buff.cost);
    }

    /**
     * This event handles Guild Chat and how it's received by other members of the Guild
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Guild guild = utils.getGuild(player.getUniqueId());

        if (guild == null) {
            return;
        }

        if (GUILD_CHAT_PLAYERS.contains(player.getUniqueId())) {

            event.getRecipients().forEach(r -> {
                if (guilds.getSpy().contains(r)) {
                    r.sendMessage(getString("spy-chat-format").replace("{role}", GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getName()).replace("{player}", player.getName()).replace("{message}", event.getMessage()).replace("{guild}", guild.getName()));
                }
            });
            event.getRecipients().removeIf(r -> (guild.getMember(r.getUniqueId()) == null));
            event.getRecipients().forEach(recipient -> recipient.sendMessage(getString("guild-chat-format").replace("{role}", GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getName()).replace("{player}", player.getName()).replace("{message}", event.getMessage())));
            event.setCancelled(true);
        }
    }

    /**
     * Make sure the player has their tier role
     * @param event
     */
    @EventHandler
    public void rolePerm(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Guild guild = utils.getGuild(player.getUniqueId());
        if (guild == null) return;
        String node = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getNode();
        if (!player.hasPermission(node)) {
            Bukkit.getScheduler().runTaskLater(guilds, () -> guilds.getPermissions().playerAdd(player, node), 60L);
        }
    }

    /**
     * Handle giving player perms for all tiers
     * @param event
     */
    @EventHandler
    public void tierPerms(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Guild guild = utils.getGuild(player.getUniqueId());
        if (guild == null) return;
        utils.addGuildPerms(guild, player);
    }

    /**
     * This is just an enum of all the buff types to choose from
     */
    public enum GuildBuff {

        HASTE(PotionEffectType.FAST_DIGGING, Material.FEATHER, "haste"),
        SPEED(PotionEffectType.SPEED, Material.SUGAR, "speed"),
        FIRE_RESISTANCE(PotionEffectType.FIRE_RESISTANCE, Material.BLAZE_POWDER, "fire-resistance"),
        NIGHT_VISION(PotionEffectType.NIGHT_VISION, Material.REDSTONE_TORCH_ON, "night-vision"),
        INVISIBILITY(PotionEffectType.INVISIBILITY, Material.EYE_OF_ENDER, "invisibility"),
        STRENGTH(PotionEffectType.INCREASE_DAMAGE, Material.DIAMOND_SWORD, "strength"),
        JUMP(PotionEffectType.JUMP, Material.DIAMOND_BOOTS, "jump"),
        WATER_BREATHING(PotionEffectType.WATER_BREATHING, Material.BUCKET, "water-breathing"),
        REGENERATION(PotionEffectType.REGENERATION, Material.EMERALD, "regeneration");


        public final PotionEffectType potion;
        public final Material itemType;
        public final int time;
        public final double cost;
        public final String name;
        public final int amplifier;

        GuildBuff(PotionEffectType potion, Material itemType, String configValueName) {
            this.time = getInt("buff.time." + configValueName) * 20;
            this.cost = getDouble("buff.price." + configValueName);
            this.itemType = itemType;
            this.potion = potion;
            this.name = getString("buff.name." + configValueName);
            this.amplifier =getInt("buff.amplifier." + configValueName);
        }

        public static GuildBuff get(Material itemType) {

            return Stream.of(values()).filter(it -> it.itemType == itemType).findAny().orElse(null);
        }

    }
}
