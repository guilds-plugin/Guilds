package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.message.Message;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class CommandInfo extends CommandBase implements Listener {

    public CommandInfo() {
        super("info", "View your guild's info", "guilds.command.info", false, null, null, 0, 0);
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
        meta.setDisplayName(ChatColor.WHITE + Main.getInstance().getConfig().getString("info.playername") + ChatColor.GREEN + player.getName());

        ArrayList<String> info = new ArrayList<String>();
        info.add(ChatColor.WHITE + Main.getInstance().getConfig().getString("info.guildname") + ChatColor.GREEN + guild.getName());
        info.add(ChatColor.WHITE + Main.getInstance().getConfig().getString("info.guildstatus") + ChatColor.GREEN + guild.getStatus());
        meta.setLore(info);

        skull.setItemMeta(meta);
        return skull;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (Main.getInstance().getConfig().getBoolean("gui.info")) {
            Guild guild = Guild.getGuild(player.getUniqueId());
            if (guild == null) {
                Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
                return;
            }
            Inventory heads = Bukkit.createInventory(null, InventoryType.HOPPER, ChatColor.DARK_GREEN + "Guild Info");
            // Skull: From player
            heads.setItem(0, createSkull(player));

            // Item 1: Paper
            ArrayList<String> paperlore = new ArrayList<String>();
            paperlore.add(ChatColor.AQUA + Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName());
            heads.setItem(1, createItemStack(Material.PAPER, Main.getInstance().getConfig().getString("info.master"), paperlore));

            // Item 2: Anvil
            ArrayList<String> anvillore = new ArrayList<String>();
            anvillore.add(ChatColor.AQUA + String.valueOf(guild.getMembers().size()));
            heads.setItem(2, createItemStack(Material.ANVIL, Main.getInstance().getConfig().getString("info.member-count"), anvillore));

            // Item 3: Beacon
            ArrayList<String> beaconlore = new ArrayList<String>();
            beaconlore.add(ChatColor.AQUA + GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getName());
            heads.setItem(3, createItemStack(Material.BEACON, Main.getInstance().getConfig().getString("info.role"), beaconlore));

            // Item 4: Cake
            ArrayList<String> cakelore = new ArrayList<String>();
            cakelore.add(ChatColor.AQUA + guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()).getName()).collect(Collectors.joining(", ")));
            heads.setItem(4, createItemStack(Material.CAKE, Main.getInstance().getConfig().getString("info.members"), cakelore));

            // Open inventory
            player.openInventory(heads);


        } else {
            Guild guild = Guild.getGuild(player.getUniqueId());

            if (guild == null) {
                Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
                return;
            }
            Message.sendMessage(player, Message.COMMAND_INFO_HEADER.replace("{guild}", guild.getName()));
            Message.sendMessage(player, Message.COMMAND_INFO_NAME.replace("{guild}", guild.getName(), "{prefix}", guild.getPrefix()));
            Message.sendMessage(player, Message.COMMAND_INFO_MASTER.replace("{master}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName()));
            Message.sendMessage(player, Message.COMMAND_INFO_MEMBER_COUNT.replace("{members}", String.valueOf(guild.getMembers().size()), "{members-online}", String.valueOf(guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUniqueId())).filter(OfflinePlayer::isOnline).count())));
            Message.sendMessage(player, Message.COMMAND_INFO_RANK.replace("{rank}", GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole()).getName()));
            Message.sendMessage(player, Message.COMMAND_INFO_PLAYERS.replace("{players}", guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()).getName()).collect(Collectors.joining(", "))));
        }
    }
}
