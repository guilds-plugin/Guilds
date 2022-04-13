/*
 * MIT License
 *
 * Copyright (c) 2022 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.glaremasters.guilds.utils;

import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XMaterial;
import me.glaremasters.guilds.guild.GuildMember;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// This is a temp class until I can sort a bunch of shit out
public class GuiUtils {

    public static final Set<Material> problemItems = new HashSet<>(Arrays.asList(XMaterial.REDSTONE_TORCH.parseMaterial(), XMaterial.RED_BED.parseMaterial()));

    public static ItemStack createItem(String material, String name, List<String> lore) {
        Optional<XMaterial> tempMaterial = XMaterial.matchXMaterial(material);
        XMaterial tempCheck = tempMaterial.orElse(XMaterial.GLASS_PANE);
        ItemStack item = tempCheck.parseItem();
        if (item == null) {
            item = XMaterial.GLASS_PANE.parseItem();
        }
        // Extra check
        if (problemItems.contains(item.getType())) {
            LoggingUtils.warn("Problematic Material Type Found! Switching to Barrier.");
            item.setType(XMaterial.BARRIER.parseMaterial());
        }
        ItemBuilder builder = new ItemBuilder(item);
        builder.setName(StringUtils.color(name));
        if (!lore.isEmpty()) {
            builder.setLore(lore.stream().map(StringUtils::color).collect(Collectors.toList()));
        }
        builder.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        return builder.build();
    }

    /**
     * Creates a skull item of the player in the guild
     *
     * @param member the GuildMember instace of a player
     * @param name   the name for the item
     * @param lore   the lore for the item
     * @return formatted skull texture of a member
     */
    public static ItemStack createSkullItem(GuildMember member, String name, List<String> lore) {
        final ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
        final ItemMeta meta = head.getItemMeta();
        SkullUtils.applySkin(meta, member.getTexture());

        meta.setDisplayName(StringUtils.color(name));
        if (!lore.isEmpty()) {
            meta.setLore(lore.stream().map(StringUtils::color).collect(Collectors.toList()));
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        head.setItemMeta(meta);
        return head;
    }
}
