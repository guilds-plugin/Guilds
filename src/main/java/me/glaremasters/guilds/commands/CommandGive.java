package me.glaremasters.guilds.commands;

import java.util.ArrayList;
import java.util.List;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by GlareMasters on 8/28/2017.
 */
public class CommandGive extends CommandBase {

    public CommandGive() {
        super("give", Guilds.getInstance().getConfig().getString("commands.description.give"),
                "guilds.command.give", true, null,
                null, 2, 2);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        FileConfiguration config = Guilds.getInstance().getConfig();
        Player player = Bukkit.getPlayer(args[0]);
        Integer amount = Integer.valueOf(args[1]);
        ItemStack upgradeTicket = new ItemStack(Material.PAPER, amount);
        ItemMeta meta = upgradeTicket.getItemMeta();
        List<String> lores = new ArrayList<String>();
        lores.add(ChatColor.translateAlternateColorCodes('&', config.getString("upgrade-ticket.lore")));
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("upgrade-ticket.name")));
        meta.setLore(lores);
        upgradeTicket.setItemMeta(meta);
        player.getInventory().addItem(upgradeTicket);
    }


}
