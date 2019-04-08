package me.glaremasters.guilds.utils;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Glare
 * Date: 4/8/2019
 * Time: 2:22 PM
 */
public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(@NotNull ItemStack item) {
        this.item = item;
        meta = item.getItemMeta();
        Validate.notNull(meta, "Item/material must have ItemMeta");
    }

    public ItemBuilder(@NotNull Material material) {
        this(new ItemStack(material));
    }

    @NotNull
    @Contract(pure = true)
    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    @NotNull
    public ItemBuilder setAmount(int amount) {
        item.setAmount(amount);
        return this;
    }

    @NotNull
    public ItemBuilder setName(@NotNull String name) {
        meta.setDisplayName(name);
        return this;
    }

    @NotNull
    public ItemBuilder setLore(@NotNull List<String> lore) {
        meta.setLore(lore);
        return this;
    }

    @NotNull
    public ItemBuilder setLore(@NotNull String... lore) {
        meta.setLore(Arrays.asList(lore));
        return this;
    }

    @NotNull
    public ItemBuilder addEnchantment(@NotNull Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    @NotNull
    public ItemBuilder addItemFlags(@NotNull ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    @NotNull
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    @NotNull
    public <T extends ItemMeta> ItemBuilder applyCustomMeta(@NotNull Class<T> type, @NotNull Consumer<T> applier) {
        applier.accept(type.cast(meta));
        return this;
    }
}
