/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
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
import com.mojang.authlib.properties.PropertyMap;
import me.glaremasters.guilds.utils.SkullUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.Base64;

/**
 * Represents a guild skull, which is a player head in Minecraft that has a custom texture.
 */
public class GuildSkull {
    private final String serialized;
    private transient ItemStack itemStack;

    /**
     * Creates a guild skull from a player.
     *
     * @param player the player whose head will be used for the guild skull
     */
    public GuildSkull(Player player) {
        serialized = SkullUtils.getEncoded(getTextureUrl(player));
        itemStack = SkullUtils.getSkull(serialized);
    }

    /**
     * Creates a guild skull from a texture string.
     *
     * @param texture the texture string, which should be a Minecraft resource location string
     */
    public GuildSkull(String texture) {
        serialized = SkullUtils.getEncoded("https://textures.minecraft.net/texture/" + texture);
        itemStack = SkullUtils.getSkull(serialized);
    }

    /**
     * Gets the texture URL of a player's skin.
     *
     * @param player the player whose skin will be used for the guild skull
     * @return the skin texture URL
     */
    private String getTextureUrl(Player player) {
        GameProfile profile = getProfile(player);
        if (profile == null) {
            return "";
        }
        PropertyMap propertyMap = profile.getProperties();
        for (Property property : propertyMap.get("textures")) {
            byte[] decoded = Base64.getDecoder().decode(property.getValue());
            JsonObject texture = new JsonParser().parse(new String(decoded)).getAsJsonObject().get("textures").getAsJsonObject().get("SKIN").getAsJsonObject();
            return texture.get("url").getAsString();
        }
        return "";
    }

    /**
     * Gets the profile of an online player.
     *
     * @param player the player whose profile will be retrieved
     * @return the player's profile
     */
    private GameProfile getProfile(Player player) {
        try {
            Class<?> strClass = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");
            return (GameProfile) strClass.cast(player).getClass().getMethod("getProfile").invoke(strClass.cast(player));
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the version of the server.
     *
     * @return the server version
     */
    private String getServerVersion() {
        try {
            return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return "unknown";
        }
    }

    /**
     * Gets the guild skull as an item stack.
     *
     * @return the guild skull as an item stack
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Get the serialized string
     *
     * @return serialized string
     */
    public String getSerialized() {
        return serialized;
    }
}