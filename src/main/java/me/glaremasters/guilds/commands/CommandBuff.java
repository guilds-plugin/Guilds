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
public class CommandBuff extends CommandBase{
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
    haste.add(ChatColor.AQUA + "This buff will allow you and your");
    haste.add(ChatColor.AQUA + "Guild Members to obtain increased");
    haste.add(ChatColor.AQUA + "mining speed for a certain amount of time.");
    buff.setItem(0, createItemStack(Material.FEATHER,
        Main.getInstance().getConfig().getString("buff.haste"), haste));

    buff.setItem(1, new ItemStack(Material.STAINED_GLASS_PANE));
    buff.setItem(3, new ItemStack(Material.STAINED_GLASS_PANE));
    buff.setItem(5, new ItemStack(Material.STAINED_GLASS_PANE));
    buff.setItem(7, new ItemStack(Material.STAINED_GLASS_PANE));

    // Buff 2: Walk Speed
    ArrayList<String> speed = new ArrayList<String>();
    speed.add(ChatColor.AQUA + "This buff will allow you and your");
    speed.add(ChatColor.AQUA + "Guild Members to obtain increased");
    speed.add(ChatColor.AQUA + "movement speed for a certain amount of time.");
    buff.setItem(2, createItemStack(Material.SUGAR,
        Main.getInstance().getConfig().getString("buff.speed"), speed));

    // Buff 3: Fire Resistance
    ArrayList<String> fireResistance = new ArrayList<String>();
    fireResistance.add(ChatColor.AQUA + "This buff will allow you and your");
    fireResistance.add(ChatColor.AQUA + "Guild Members to obtain increased");
    fireResistance.add(ChatColor.AQUA + "fire resistance for a certain amount of time.");
    buff.setItem(4, createItemStack(Material.BLAZE_POWDER,
        Main.getInstance().getConfig().getString("buff.fire-resistance"),fireResistance));

    // Buff 4: Night Vision
    ArrayList<String> nightvision = new ArrayList<String>();
    nightvision.add(ChatColor.AQUA + "This buff will allow you and your");
    nightvision.add(ChatColor.AQUA + "Guild Members to obtain increased");
    nightvision.add(ChatColor.AQUA + "night vision for a certain amount of time.");
    buff.setItem(6, createItemStack(Material.REDSTONE_TORCH_ON,
        Main.getInstance().getConfig().getString("buff.night-vision"), nightvision));

    // Buff 5: Invisibility
    ArrayList<String> invisibility = new ArrayList<String>();
    invisibility.add(ChatColor.AQUA + "This buff will allow you and your");
    invisibility.add(ChatColor.AQUA + "Guild Members to obtain increased");
    invisibility.add(ChatColor.AQUA + "invisibility for a certain amount of time.");
    buff.setItem(8, createItemStack(Material.EYE_OF_ENDER,
        Main.getInstance().getConfig().getString("buff.invisibility"), invisibility));

    // Open inventory
    player.openInventory(buff);
  }
}
