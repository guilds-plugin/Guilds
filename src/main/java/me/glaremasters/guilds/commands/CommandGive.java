package me.glaremasters.guilds.commands;

import static me.glaremasters.guilds.util.ColorUtil.color;
import java.util.ArrayList;
import java.util.List;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import org.bukkit.Bukkit;
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

    private FileConfiguration config = Guilds.getInstance().getConfig();
    private String ticketName = color(config.getString("upgrade-ticket.name"));
    private String ticketMaterial = config.getString("upgrade-ticket.material");
    private String ticketLore = color(config.getString("upgrade-ticket.lore"));

    public CommandGive() {
        super("give", Guilds.getInstance().getConfig().getString("commands.description.give"),
                "guilds.command.give", true, null,
                null, 2, 2);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) return;

        try {
            Integer amount = Integer.valueOf(args[1]);
            ItemStack upgradeTicket = new ItemStack(Material.getMaterial(ticketMaterial), amount);
            ItemMeta meta = upgradeTicket.getItemMeta();
            List<String> lores = new ArrayList<String>();
            lores.add(ticketLore);
            meta.setDisplayName(ticketName);
            meta.setLore(lores);
            upgradeTicket.setItemMeta(meta);
            player.getInventory().addItem(upgradeTicket);
        } catch (NumberFormatException e) {
            return;
        }
    }


}
