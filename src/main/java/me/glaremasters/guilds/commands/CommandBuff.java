package me.glaremasters.guilds.commands;

import java.util.ArrayList;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by GlareMasters on 7/24/2017.
 */
public class CommandBuff extends CommandBase {

  Inventory buff = Bukkit.createInventory(null, 9, "Guild Buffs");

  public CommandBuff() {
    super("buff", "Buy buffs for your guild!", "guilds.command.buff", false,
        null, null, 0, 0);
  }


  public ItemStack createItemStack(Material mat, String name, ArrayList<String> Lore) {
    ItemStack paper = new ItemStack(mat);

    ItemMeta meta = paper.getItemMeta();
    meta.setDisplayName(name);
    meta.setLore(Lore);

    paper.setItemMeta(meta);
    return paper;
  }

  public void execute(Player player, String[] args) {
    Guild guild = Guild.getGuild(player.getUniqueId());
    if (guild == null) {
      Message.sendMessage(player, Message.COMMAND_ERROR_NO_GUILD);
      return;
    }

    // Buff 1: Haste
    ArrayList<String> haste = new ArrayList<String>();
    Main.getInstance().getConfig().getStringList("buff.description.haste").stream()
        .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(haste::add);
    haste.add("");
    haste.add(ChatColor.translateAlternateColorCodes('&',
        Main.getInstance().getConfig().getString("buff.description.price") + Main.getInstance()
            .getConfig().getInt("buff.price.haste")));
    haste.add(ChatColor.translateAlternateColorCodes('&',
        Main.getInstance().getConfig().getString("buff.description.length") + Main.getInstance()
            .getConfig().getInt("buff.time.haste")));
    buff.setItem(0, createItemStack(Material.FEATHER,
        Main.getInstance().getConfig().getString("buff.name.haste"), haste));

    buff.setItem(1, new ItemStack(Material.STAINED_GLASS_PANE));
    buff.setItem(3, new ItemStack(Material.STAINED_GLASS_PANE));
    buff.setItem(5, new ItemStack(Material.STAINED_GLASS_PANE));
    buff.setItem(7, new ItemStack(Material.STAINED_GLASS_PANE));

    // Buff 2: Walk Speed
    ArrayList<String> speed = new ArrayList<String>();
    Main.getInstance().getConfig().getStringList("buff.description.speed").stream()
        .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(speed::add);
    speed.add("");
    speed.add(ChatColor.translateAlternateColorCodes('&',
        Main.getInstance().getConfig().getString("buff.description.price") + Main.getInstance()
            .getConfig().getInt("buff.price.speed")));
    speed.add(ChatColor.translateAlternateColorCodes('&',
        Main.getInstance().getConfig().getString("buff.description.length") + Main.getInstance()
            .getConfig().getInt("buff.time.speed")));
    buff.setItem(4, createItemStack(Material.SUGAR,
        Main.getInstance().getConfig().getString("buff.name.speed"), speed));

    // Buff 3: Fire Resistance
    ArrayList<String> fireResistance = new ArrayList<String>();
    Main.getInstance().getConfig().getStringList("buff.description.fire-resistance").stream()
        .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(fireResistance::add);
    fireResistance.add("");
    fireResistance.add(ChatColor.translateAlternateColorCodes('&',
        Main.getInstance().getConfig().getString("buff.description.price") + Main.getInstance()
            .getConfig().getInt("buff.price.fire-resistance")));
    fireResistance.add(ChatColor.translateAlternateColorCodes('&',
        Main.getInstance().getConfig().getString("buff.description.length") + Main.getInstance()
            .getConfig().getInt("buff.time.fire-resistance")));
    buff.setItem(2, createItemStack(Material.BLAZE_POWDER,
        Main.getInstance().getConfig().getString("buff.name.fire-resistance"), fireResistance));

    // Buff 4: Night Vision
    ArrayList<String> nightvision = new ArrayList<String>();
    Main.getInstance().getConfig().getStringList("buff.description.night-vision").stream()
        .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(nightvision::add);
    nightvision.add("");
    nightvision.add(ChatColor.translateAlternateColorCodes('&',
        Main.getInstance().getConfig().getString("buff.description.price") + Main.getInstance()
            .getConfig().getInt("buff.price.night-vision")));
    nightvision.add(ChatColor.translateAlternateColorCodes('&',
        Main.getInstance().getConfig().getString("buff.description.length") + Main.getInstance()
            .getConfig().getInt("buff.time.night-vision")));
    buff.setItem(6, createItemStack(Material.REDSTONE_TORCH_ON,
        Main.getInstance().getConfig().getString("buff.name.night-vision"), nightvision));

    // Buff 5: Invisibility
    ArrayList<String> invisibility = new ArrayList<String>();
    Main.getInstance().getConfig().getStringList("buff.description.invisibility").stream()
        .map(it -> ChatColor.translateAlternateColorCodes('&', it)).forEach(invisibility::add);
    invisibility.add("");
    invisibility.add(ChatColor.translateAlternateColorCodes('&',
        Main.getInstance().getConfig().getString("buff.description.price") + Main.getInstance()
            .getConfig().getInt("buff.price.invisibility")));
    invisibility.add(ChatColor.translateAlternateColorCodes('&',
        Main.getInstance().getConfig().getString("buff.description.length") + Main.getInstance()
            .getConfig().getInt("buff.time.invisibility")));
    buff.setItem(8, createItemStack(Material.EYE_OF_ENDER,
        Main.getInstance().getConfig().getString("buff.name.invisibility"), invisibility));

    // Open inventory
    player.openInventory(buff);
  }
}
