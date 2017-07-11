package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;


public class CommandList extends CommandBase {
    public CommandList() {
        super("list", "List all guilds on the server", "guilds.command.list", false, null, null, 0, 0);
    }

    public static Inventory guildList = null;

    @Override
    public void execute(Player player, String[] args) {
        guildList = getSkullsPage(1);

        player.openInventory(guildList);
    }

    public static Inventory getSkullsPage(int page) {
        HashMap<UUID, ItemStack> skulls = new HashMap<>();
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Guild List - Page " + String.valueOf(page));
        int startIndex = 0;
        int endIndex = 0;

        for (int i = 0; i < Main.getInstance().getGuildHandler().getGuilds().values().size(); i++) {
            Guild guild = (Guild) Main.getInstance().getGuildHandler().getGuilds().values().toArray()[i];
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            ArrayList<String> lore = new ArrayList<String>();

            lore.add(ChatColor.RED + Main.getInstance().getConfig().getString("list.prefix") + guild.getPrefix());
            lore.add(ChatColor.LIGHT_PURPLE + Main.getInstance().getConfig().getString("list.name") + guild.getName());
            lore.add(ChatColor.GREEN + Main.getInstance().getConfig().getString("list.master") + Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName());
            lore.add(ChatColor.YELLOW + Main.getInstance().getConfig().getString("list.member-count") + String.valueOf(guild.getMembers().size()));
            lore.add(ChatColor.GOLD + Main.getInstance().getConfig().getString("list.members") + guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()).getName()).collect(Collectors.joining(", ")));
            skullMeta.setLore(lore);
            skullMeta.setOwner(Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName());
            skullMeta.setDisplayName(ChatColor.AQUA + Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName() + "'s Guild");
            skull.setItemMeta(skullMeta);
            skulls.put(guild.getGuildMaster().getUniqueId(), skull);
        }

        ItemStack previous = new ItemStack(Material.TORCH, 1);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.setDisplayName(ChatColor.GOLD + "Previous page");
        previous.setItemMeta(previousMeta);
        ItemStack next = new ItemStack(Material.TORCH, 1);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GOLD + "Next page");
        next.setItemMeta(nextMeta);
        inv.setItem(53, next);
        inv.setItem(45, previous);

        startIndex = (page - 1) * 45;

        endIndex = startIndex + 45 - 1;

        if (endIndex < skulls.values().size()) {
            endIndex = skulls.values().size();
        }

        int iCount = 0;
        for (int i1 = startIndex; startIndex < endIndex; i1++) {
            iCount++;
            Bukkit.broadcastMessage(String.valueOf(i1));
            inv.setItem(iCount, (ItemStack) skulls.values().toArray()[i1]);
        }

        return inv;
    }
}


