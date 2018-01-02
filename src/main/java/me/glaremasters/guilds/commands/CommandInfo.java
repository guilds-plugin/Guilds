package me.glaremasters.guilds.commands;

import java.util.ArrayList;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class CommandInfo extends CommandBase implements Listener {

    public CommandInfo() {
        super("info", Guilds.getInstance().getConfig().getString("commands.description.info"),
                "guilds.command.info", false, null, null, 0, 0);
    }

    public ItemStack createItemStack(Material mat, String name, ArrayList<String> Lore) {
        ItemStack paper = new ItemStack(mat);

        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Lore);

        paper.setItemMeta(meta);
        return paper;
    }

    public ItemStack createSkull(Player player) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());

        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        final Guilds instance = Guilds.getInstance();
        meta.setDisplayName(
                ChatColor.WHITE + instance.getConfig().getString("info.playername")
                        + ChatColor.GREEN + player.getName());

        ArrayList<String> info = new ArrayList<String>();
        info.add(ChatColor.WHITE + instance.getConfig().getString("info.kills")
                + ChatColor.GREEN + player.getStatistic(Statistic.PLAYER_KILLS));
        info.add(ChatColor.WHITE + instance.getConfig().getString("info.deaths")
                + ChatColor.GREEN + player.getStatistic(Statistic.DEATHS));
        meta.setLore(info);

        skull.setItemMeta(meta);
        return skull;
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) {
            Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
            return;
        }
        FileConfiguration config = Guilds.getInstance().getConfig();
        Inventory heads = Bukkit
                .createInventory(null, InventoryType.HOPPER,
                        ChatColor.DARK_GREEN + config
                                .getString("gui-name.info"));
        // Skull: From player
        heads.setItem(1, createSkull(player));

        // Item 1: Paper
        ArrayList<String> paperlore = new ArrayList<String>();
        paperlore.add(
                ChatColor.WHITE + config.getString("info.guildname")
                        + ChatColor.GREEN + guild.getName());
        paperlore.add(
                ChatColor.WHITE + config.getString("info.prefix")
                        + ChatColor.GREEN + guild.getPrefix());
        paperlore.add(
                ChatColor.WHITE + config.getString("info.role")
                        + ChatColor.GREEN + GuildRole
                        .getRole(guild.getMember(player.getUniqueId()).getRole())
                        .getName());
        paperlore.add(
                ChatColor.WHITE + config.getString("info.master")
                        + ChatColor.GREEN + Bukkit
                        .getOfflinePlayer(guild.getGuildMaster().getUniqueId())
                        .getName());
        paperlore.add(
                ChatColor.WHITE + config.getString("info.member-count")
                        + ChatColor.GREEN + String.valueOf(guild.getMembers().size()));
        paperlore.add(
                ChatColor.WHITE + config.getString("info.guildstatus")
                        + ChatColor.GREEN + guild.getStatus());
        paperlore.add(
                ChatColor.WHITE + config.getString("info.guildtier")
                        + ChatColor.GREEN + guild.getTier());
        heads.setItem(2, createItemStack(Material.PAPER, config.getString("info.info"), paperlore));

        // Item 2: Diamond
        ArrayList<String> diamondlore = new ArrayList<String>();
        diamondlore.add(ChatColor.WHITE + config.getString("info.balance")
                + ChatColor.GREEN + guild.getBankBalance());
        diamondlore.add(ChatColor.WHITE + config.getString("info.max-balance")
                + ChatColor.GREEN + guild.getMaxBankBalance());
        heads.setItem(3, createItemStack(Material.DIAMOND,
                config.getString("info.money"), diamondlore));

        // Open inventory
        player.openInventory(heads);


    }
}
