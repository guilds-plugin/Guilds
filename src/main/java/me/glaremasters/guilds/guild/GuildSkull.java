/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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
import me.glaremasters.guilds.utils.SkullUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

/**
 * Created by GlareMasters
 * Date: 2/15/2019
 * Time: 1:34 PM
 */

public class GuildSkull {
    private String serialized;
    private transient ItemStack itemStack;

    /**
     * Get the Guild Skull of a player
     * @param player the player you're getting the skull of
     */
    public GuildSkull(Player player) {
        serialized = SkullUtils.getEncoded(getTextureUrl(player.getUniqueId()));
        itemStack = SkullUtils.getSkull(serialized);
    }

    /**
     * Get the Guild Skull from a sting
     * @param texture the texture you want to use
     */
    public GuildSkull(String texture) {
        serialized = SkullUtils.getEncoded("https://textures.minecraft.net/texture/" + texture);
        itemStack = SkullUtils.getSkull(serialized);
    }

    /**
     * Get the url of a texture
     * @param uuid the uuid to get the texture of
     * @return texture of uuid
     */
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

    /**
     * Get the skull as an item stack
     * @return item stack
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Get the serialized string
     * @return serialized string
     */
    public String getSerialized() {
        return serialized;
    }
}