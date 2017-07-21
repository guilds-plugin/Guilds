package me.glaremasters.guilds.util;

/**
 * Created by GlareMasters on 7/21/2017.
 */

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {
  public static ItemBuilder create() {
    return new ItemBuilder();
  }
  public static ItemBuilder createFrom(ItemStack stack) {
    return new ItemBuilder(stack);
  }

  private ItemStack stack;

  private ItemBuilder() {
    this(new ItemStack(Material.POTATO_ITEM, 1));
  }

  private ItemBuilder(ItemStack stack) {
    this.stack = stack;
  }

  private String color(String string) {
    return ChatColor.translateAlternateColorCodes('&', string);
  }

  public ItemBuilder material(Material material) {
    stack.setType(material);
    return this;
  }

  public ItemBuilder amount(int amount) {
    stack.setAmount(amount);
    return this;
  }

  public ItemBuilder durability(short durability) {
    stack.setDurability(durability);
    return this;
  }

  public ItemBuilder name(String displayName) {
    ItemMeta meta = stack.getItemMeta();
    meta.setDisplayName(color(displayName));
    stack.setItemMeta(meta);
    return this;
  }

  @SuppressWarnings("WeakerAccess")
  public ItemBuilder lore(String line) {
    ItemMeta meta = stack.getItemMeta();
    List<String> lore;

    if (meta.hasLore()) {
      lore = meta.getLore();
    } else {
      lore = new ArrayList<>();
    }

    lore.add(color(line));
    meta.setLore(lore);
    stack.setItemMeta(meta);
    return this;
  }

  public ItemBuilder lore(String... lines) {
    Arrays.stream(lines).forEach(this::lore);
    return this;
  }

  public ItemBuilder enchant(Enchantment enchantment, int level) {
    if (level > enchantment.getMaxLevel()) {
      stack.addUnsafeEnchantment(enchantment, level);
      return this;
    }

    stack.addEnchantment(enchantment, level);
    return this;
  }

  public ItemBuilder damage(short damage) {
    stack.setDurability(damage);
    return this;
  }

  public ItemStack build() {
    return stack;
  }
}
