package me.bramhaag.guilds.commands;

import me.bramhaag.guilds.Main;
import me.bramhaag.guilds.commands.base.CommandBase;
import me.bramhaag.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.stream.Collectors;


public class CommandList extends CommandBase {
    public CommandList() {
        super("list", "List all guilds on the server", "guilds.command.list", false, null, null, 0, 0);
    }
    public static Inventory guildList;
    @Override
    public void execute(Player player, String[] args) {
        guildList = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Guild List");
        for (int i = 0; i < Main.getInstance().getGuildHandler().getGuilds().values().size(); i++) {
            Guild guild = (Guild) Main.getInstance().getGuildHandler().getGuilds().values().toArray()[i];
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            ArrayList<String> lore = new ArrayList<String>();
            {
                lore.add(ChatColor.RED + Main.getInstance().getConfig().getString("list.prefix") + guild.getPrefix());
                lore.add(ChatColor.LIGHT_PURPLE + Main.getInstance().getConfig().getString("list.name") + guild.getName());
                lore.add(ChatColor.GREEN + Main.getInstance().getConfig().getString("list.master") + Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName());
                lore.add(ChatColor.YELLOW + Main.getInstance().getConfig().getString("list.member-count") + String.valueOf(guild.getMembers().size()));
                lore.add(ChatColor.GOLD + Main.getInstance().getConfig().getString("list.members") + guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()).getName()).collect(Collectors.joining(", ")));
                skullMeta.setLore(lore);
                skullMeta.setOwner(Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName());
                skullMeta.setDisplayName(ChatColor.AQUA + Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName() + "'s Guild");
                skull.setItemMeta(skullMeta);
                guildList.setItem(i, skull);
            }
            player.openInventory(guildList);
        }
    }
}