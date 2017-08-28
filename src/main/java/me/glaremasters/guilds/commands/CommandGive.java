package me.glaremasters.guilds.commands;

import java.util.ArrayList;
import java.util.List;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by GlareMasters on 8/28/2017.
 */
public class CommandGive extends CommandBase {

    public CommandGive() {
        super("give", Main.getInstance().getConfig().getString("commands.description.give"),
                "guilds.command.give", true, null,
                null, 2, 2);
    }
    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = Bukkit.getPlayer(args[0]);
        Integer amount = Integer.valueOf(args[1]);
        ItemStack upgradeTicket = new ItemStack(Material.PAPER, amount);
        ItemMeta meta = upgradeTicket.getItemMeta();
        List<String> lores = new ArrayList<String>();
        lores.add("");
        meta.setDisplayName("Guild Upgrade Ticket");
        meta.setLore(lores);
        upgradeTicket.setItemMeta(meta);
        player.getInventory().addItem(upgradeTicket);
    }


}
