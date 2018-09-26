package me.glaremasters.guilds.listeners;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Serialization;
import me.rayzr522.jsonmessage.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.Sign;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static me.glaremasters.guilds.utils.ConfigUtils.color;
import static me.glaremasters.guilds.utils.ConfigUtils.getBoolean;

/**
 * Created by GlareMasters
 * Date: 7/19/2018
 * Time: 5:31 PM
 */
public class Players implements Listener {

    private Guilds guilds;

    private Set<UUID> ALREADY_INFORMED = new HashSet<>();

    public Players(Guilds guilds) {
        this.guilds = guilds;
    }

    /**
     * Guild / Ally damage handler
     * @param event handles when damage is done between two players that might be in the same guild or are allies
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();
        Guild playerGuild = Guild.getGuild(player.getUniqueId());
        Guild damagerGuild = Guild.getGuild(damager.getUniqueId());
        if (playerGuild == null || damagerGuild == null) return;
        if (playerGuild.equals(damagerGuild)) event.setCancelled(!getBoolean("allow-guild-damage"));
        if (Guild.areAllies(player.getUniqueId(), damager.getUniqueId())) event.setCancelled(!getBoolean("allow-ally-damage"));
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        if (!event.getInventory().getName().equalsIgnoreCase(guild.getName() + "'s Guild Vault")) return;
        guild.updateInventory(Serialization.serializeInventory(event.getInventory()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        guilds.getServer().getScheduler().scheduleSyncDelayedTask(guilds, () -> {
            if (player.isOnline()) {
                if (!ALREADY_INFORMED.contains(player.getUniqueId())) {
                    JSONMessage.create("Announcements").tooltip(guilds.getAnnouncements()).openURL(guilds.getDescription().getWebsite()).send(player);
                    ALREADY_INFORMED.add(player.getUniqueId());
                }
            }
        }, 70L);
    }

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

    @EventHandler
    public void onGlobalVault(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block.getType() != Material.WALL_SIGN) return;
        org.bukkit.block.Sign sign = (org.bukkit.block.Sign) block.getState();
        if (!sign.getLine(0).equalsIgnoreCase("[Guild Vault]")) return;
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canOpenVault()) {
            guilds.getManager().getCommandIssuer(player).sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }
        if (guild.getInventory().equalsIgnoreCase("")) {
            Inventory inv = Bukkit.createInventory(null, 54, guild.getName() + "'s Guild Vault");
            player.openInventory(inv);
            return;
        }
        try {
            player.openInventory(Serialization.deserializeInventory(guild.getInventory()));
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
