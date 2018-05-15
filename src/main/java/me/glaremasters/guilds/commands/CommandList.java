package me.glaremasters.guilds.commands;

import static me.glaremasters.guilds.util.ColorUtil.color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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

    public CommandList() {
        super("list", Guilds.getInstance().getConfig().getString("commands.description.list"),
                "guilds.command.list", false, null, null, 0,
                0);
    }

    public static Inventory getSkullsPage(int page) {
        HashMap<UUID, ItemStack> skulls = new HashMap<>();
        FileConfiguration config = Guilds.getInstance().getConfig();
        Inventory inv = Bukkit.createInventory(null, 54, (config.getString("gui-name.list.name")));

        int startIndex = 0;
        int endIndex = 0;

        for (int i = 0; i < Guilds.getInstance().getGuildHandler().getGuilds().values().size(); i++) {
            Guild guild = (Guild) Guilds.getInstance().getGuildHandler().getGuilds().values().toArray()[i];
            ItemStack item = new ItemStack(Material.getMaterial(config.getString("list.item")));
            ItemMeta itemMeta = item.getItemMeta();
            ArrayList<String> lore = new ArrayList<String>();
            if (config.getBoolean("display.name")) { lore.add(color(config.getString("list.name") + guild.getName())); }
            if (config.getBoolean("display.prefix")) { lore.add(color(config.getString("list.prefix") + guild.getPrefix())); }
            if (config.getBoolean("display.master")) { lore.add(color(config.getString("list.master") + Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName())); }
            if (config.getBoolean("display.guildstatus")) { lore.add(color(config.getString("list.guildstatus") + guild.getStatus())); }
            if (config.getBoolean("display.guildtier")) { lore.add(color(config.getString("list.guildtier") + guild.getTier())); }
            if (config.getBoolean("display.guildbalance")) { lore.add(color(config.getString("list.guildbalance") + guild.getBankBalance())); }
            if (config.getBoolean("display.member-count")) { lore.add(color(config.getString("list.member-count") + String.valueOf(guild.getMembers().size()))); }
            if (config.getBoolean("display.members")) {
                List<String> lines = Arrays.asList(guild.getMembers().stream().map(member -> Bukkit.getOfflinePlayer(member.getUniqueId()).getName()).collect(Collectors.joining(", ")).replaceAll("(([a-zA-Z0-9_]+, ){3})", "$0\n").split("\n"));
                for (int j = 0; j < lines.size(); j ++) {
                    lines.set(j, color(config.getString("list.members") + lines.get(j)));
                }
                lore.addAll(lines);
            }
            itemMeta.setLore(lore);
            String name = Bukkit.getOfflinePlayer(guild.getGuildMaster().getUniqueId()).getName();
            itemMeta.setDisplayName(color(config.getString("gui-name.list.head-name").replace("{player}", name)));
            item.setItemMeta(itemMeta);
            skulls.put(guild.getGuildMaster().getUniqueId(), item);
        }

        ItemStack previous = new ItemStack(Material.EMPTY_MAP, 1);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.setDisplayName(color(config.getString("gui-name.list.previous-page")));
        previous.setItemMeta(previousMeta);
        ItemStack next = new ItemStack(Material.EMPTY_MAP, 1);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName(color(config.getString("gui-name.list.next-page")));
        next.setItemMeta(nextMeta);
        ItemStack barrier = new ItemStack(Material.BARRIER, 1);
        ItemMeta barrierMeta = barrier.getItemMeta();
        barrierMeta.setDisplayName(color(config.getString("gui-name.list.page") + page));
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
}


