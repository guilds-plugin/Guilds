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
import co.aikar.commands.PaperCommandManager;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.Messages;
import me.glaremasters.guilds.configuration.sections.GuiSettings;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.configuration.sections.PluginSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.rayzr522.jsonmessage.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static me.glaremasters.guilds.utils.StringUtils.color;

/**
 * Created by GlareMasters
 * Date: 7/19/2018
 * Time: 5:31 PM
 */
@AllArgsConstructor
public class PlayerListener implements Listener {

    //todo

    private GuildHandler guildHandler;
    private SettingsManager settingsManager;
    private Guilds guilds;
    private PaperCommandManager commandManager;

    private final Set<UUID> ALREADY_INFORMED = new HashSet<>();
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
        if (playerGuild.equals(damagerGuild)) event.setCancelled(!settingsManager.getProperty(GuildSettings.GUILD_DAMAGE));
        /*if (guildHandler.areAllies(player.getUniqueId(), damager.getUniqueId())) event.setCancelled(!settingsManager.getProperty(GuildSettings.ALLY_DAMAGE));*/
    }

    /**
     * This will check if a user is OP and will inform them of any important announcements from the Guild's Developer
     * @param event
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (settingsManager.getProperty(PluginSettings.ANNOUNCEMENTS_IN_GAME)) {
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
        commandManager.getCommandIssuer(event.getPlayer()).sendInfo(Messages.ADMIN__GUILD_VAULT_SIGN);
    }

    /**
     * Check if the inventory being clicked on is part of the Guild Buff system
     * @param event
     */
    @EventHandler
    public void onBuffBuy(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Guild guild = guildHandler.getGuild(player);
        if (!event.getInventory().getTitle().equals(settingsManager.getProperty(GuiSettings.GUILD_BUFF_NAME))) return;
        if (event.getInventory().getTitle().equals(settingsManager.getProperty(GuiSettings.GUILD_BUFF_NAME))) event.setCancelled(true);
        if (event.getCurrentItem() == null) return;
        /*GuildBuff buff = GuildBuff.get(event.getCurrentItem().getType());*/
        double balance = guild.getBalance();
        /*if (buff == null) return;*/
/*        if (balance < buff.cost) {
            commandManager.getCommandIssuer(player).sendInfo(Messages.BANK__NOT_ENOUGH_BANK);
            return;
        }*/
        if (settingsManager.getProperty(GuiSettings.BUFF_STACKING) && !player.getActivePotionEffects().isEmpty()) return;

        /*guild.getOnlineMembers().forEach(guildMember -> ((Player) guildMember).addPotionEffect(new PotionEffect(buff.potion, buff.time, buff.amplifier)));*/
        /*guild.setBalance(balance - buff.cost);*/

    }

    /**
     * This event handles Guild Chat and how it's received by other members of the Guild
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Guild guild = guildHandler.getGuild(player);

        if (guild == null) {
            return;
        }

        if (GUILD_CHAT_PLAYERS.contains(player.getUniqueId())) {

            event.getRecipients().forEach(r -> {
                if (guilds.getSpy().contains(r)) {
                    r.sendMessage(settingsManager.getProperty(GuildSettings.SPY_CHAT_FORMAT).replace("{role}", guildHandler.getGuildRole(guild.getMember(player.getUniqueId()).getRole().getLevel()).getName()).replace("{player}", player.getName()).replace("{message}", event.getMessage()).replace("{guild}", guild.getName()));
                }
            });
            event.getRecipients().removeIf(r -> (guild.getMember(r.getUniqueId()) == null));
            event.getRecipients().forEach(recipient -> recipient.sendMessage(settingsManager.getProperty(GuildSettings.GUILD_CHAT_FORMAT).replace("{role}", guildHandler.getGuildRole(guild.getMember(player.getUniqueId()).getRole().getLevel()).getName()).replace("{player}", player.getName()).replace("{message}", event.getMessage())));
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
        Guild guild = guildHandler.getGuild(player);
        if (guild == null) return;
        String node = guildHandler.getGuildRole(guild.getMember(player.getUniqueId()).getRole().getLevel()).getNode();
        if (!player.hasPermission(node)) {
            Bukkit.getScheduler().runTaskLater(guilds, () -> guilds.getPermissions().playerAdd(player, node), 60L);
        }
    }

    /**
     * Handle giving player perms for all tiers
     * @param event
     */
    //todo this is done on multiple stages.
    //we need to make sure that these are logical
    //it's not on so many places, guild creation, member join, player join server etc
    //need to make sure we are not wasting resources by assigning it multiple times.
    //so I think this probably can go.
    @EventHandler
    public void tierPerms(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Guild guild = guildHandler.getGuild(player);
        if (guild == null) return;
        /*utils.addGuildPerms(guild, player);*/


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


/*        public final PotionEffectType potion;
        public final Material itemType;
        public final int time;
        public final double cost;
        public final String name;
        public final int amplifier;*/

        GuildBuff(PotionEffectType potion, Material itemType, String configValueName) {
/*            this.time = getInt("buff.time." + configValueName) * 20;
            this.cost = getDouble("buff.price." + configValueName);
            this.itemType = itemType;
            this.potion = potion;
            this.name = getString("buff.name." + configValueName);
            this.amplifier =getInt("buff.amplifier." + configValueName);*/
        }

/*
        public static GuildBuff get(Material itemType) {

            return Stream.of(values()).filter(it -> it.itemType == itemType).findAny().orElse(null);
        }
*/

    }
}
