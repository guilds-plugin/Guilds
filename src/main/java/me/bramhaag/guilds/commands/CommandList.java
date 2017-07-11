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

    private static int count = 0;
    public static Inventory guildList;

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        String guilds = Main.getInstance().getGuildHandler().getGuilds().values()
                .stream()
                .map(Guild::getName).collect(Collectors.joining(""));


        count = 0;
        guildList = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Guild List");
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        {
            lore.add(ChatColor.RED + "Guild Prefix: " + guild.getPrefix());
            lore.add(ChatColor.LIGHT_PURPLE + "Guild Name: " + guilds);
            lore.add(ChatColor.GREEN + "Guild Master: " + Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName());
            lore.add(ChatColor.YELLOW + "Number of Members: " + String.valueOf(guild.getMembers().size()));
            lore.add(ChatColor.GOLD + "Members: " + guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()).getName()).collect(Collectors.joining(", ")));
            skullMeta.setLore(lore);
            skullMeta.setOwner(Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName());
            skullMeta.setDisplayName(ChatColor.AQUA + Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName() + "'s Guild");
            skull.setItemMeta(skullMeta);

            guildList.setItem(count, skull);
            count += 1;
        }
        player.openInventory(guildList);
    }

}