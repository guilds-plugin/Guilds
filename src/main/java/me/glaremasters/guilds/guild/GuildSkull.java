/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
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

package me.glaremasters.guilds.guild;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

/**
 * Created by GlareMasters
 * Date: 2/15/2019
 * Time: 1:34 PM
 */

public class GuildSkull {
    private String serialized;
    private transient ItemStack itemStack;

    public GuildSkull(Player player) {
        serialized = getEncoded(getTextureUrl(player.getUniqueId()));
        itemStack = getSkull(serialized);
    }

    private String getTextureUrl(UUID uuid) {
        try {
            URL texture = new URL("https://api.minetools.eu/profile/" + uuid.toString().replaceAll("-", ""));
            InputStreamReader is = new InputStreamReader(texture.openStream());
            JsonObject textureProperty = new JsonParser().parse(is).getAsJsonObject().get("decoded").getAsJsonObject().get("textures").getAsJsonObject().get("SKIN").getAsJsonObject();
            return textureProperty.get("url").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getEncoded(String skinUrl) {
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", skinUrl).getBytes());
        return new String(encodedData);
    }

    public ItemStack getSkull(String skinUrl) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        if (skinUrl.isEmpty()) return head;

        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = getGameProfile(skinUrl);
        Field profileField;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
            e1.printStackTrace();
        }
        head.setItemMeta(headMeta);
        return head;
    }

    private GameProfile getGameProfile(String url) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", url));
        return profile;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
    public String getSerialized() {
        return serialized;
    }
}