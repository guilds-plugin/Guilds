package me.glaremasters.guilds.commands;

import static me.glaremasters.guilds.util.ColorUtil.color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class CommandList extends CommandBase {

    public static Inventory guildList = null;
    public static HashMap<UUID, Integer> playerPages = new HashMap<>();
    public static FileConfiguration config = Guilds.getInstance().getConfig();
    public static List<String> possible_items = config.getStringList("random-items");

    public CommandList() {
        super("list", Guilds.getInstance().getConfig().getString("commands.description.list"),
                "guilds.command.list", false, null, null, 0,
                0);
    }

    public static Inventory getSkullsPage(int page) {
        HashMap<UUID, ItemStack> skulls = new HashMap<>();
        Inventory inv = Bukkit.createInventory(null, 54, color(config.getString("guild-list.gui-name")));

        int startIndex = 0;
        int endIndex = 0;

        for (int i = 0; i < Guilds.getInstance().getGuildHandler().getGuilds().values().size(); i++) {
            Guild guild = (Guild) Guilds.getInstance().getGuildHandler().getGuilds().values().toArray()[i];
            ItemStack item = new ItemStack(Material.getMaterial(randomItem()));
            ItemMeta itemMeta = item.getItemMeta();
            ArrayList<String> lore = new ArrayList<String>();
            for (String text : config.getStringList("guild-list.head-lore")) {
                lore.add(color(text).
                        replace("{guild-name}", guild.getName())
                        .replace("{guild-prefix}", guild.getPrefix())
                        .replace("{guild-master}", Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName())
                        .replace("{guild-status}", guild.getStatus())
                        .replace("{guild-tier}", String.valueOf(guild.getTier()))
                        .replace("{guild-balance}", String.valueOf(guild.getBankBalance()))
                        .replace("{guild-member-count}", String.valueOf(guild.getMembers().size())));
            }
            itemMeta.setLore(lore);
            String name = Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName();
            itemMeta.setDisplayName(color(config.getString("guild-list.item-name").replace("{player}", name)));
            item.setItemMeta(itemMeta);
            skulls.put(guild.getGuildMaster().getUniqueId(), item);
        }

        ItemStack previous = new ItemStack(Material.getMaterial(config.getString("guild-list.previous-page-item")), 1);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.setDisplayName(color(config.getString("guild-list.previous-page-item-name")));
        previous.setItemMeta(previousMeta);
        ItemStack next = new ItemStack(Material.getMaterial(config.getString("guild-list.next-page-item")), 1);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(color(config.getString("guild-list.next-page-item-name")));
        next.setItemMeta(nextMeta);
        ItemStack barrier = new ItemStack(Material.getMaterial(config.getString("guild-list.page-number-item")), 1);
        ItemMeta barrierMeta = barrier.getItemMeta();
        barrierMeta.setDisplayName(color(config.getString("guild-list.page-number-item-name").replace("{page}", String.valueOf(page))));
        barrier.setItemMeta(barrierMeta);
        inv.setItem(53, next);
        inv.setItem(49, barrier);
        inv.setItem(45, previous);

        startIndex = (page - 1) * 45;
        endIndex = startIndex + 45;

        if (endIndex > skulls.values().size()) { endIndex = skulls.values().size(); }

        int iCount = 0;
        for (int i1 = startIndex; i1 < endIndex; i1++) {
            inv.setItem(iCount, (ItemStack) skulls.values().toArray()[i1]);
            iCount++;
        }

        return inv;
    }

    @Override
    public void execute(Player player, String[] args) {

        playerPages.put(player.getUniqueId(), 1);
        guildList = getSkullsPage(1);
        player.openInventory(guildList);
    }

    public static String randomItem() {
        int random = new Random().nextInt(possible_items.size());
        String mat_name = possible_items.get(random);
        return mat_name;
    }


}


