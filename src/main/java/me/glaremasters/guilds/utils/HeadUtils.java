package me.glaremasters.guilds.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.glaremasters.guilds.Guilds;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

/**
 * Created by GlareMasters
 * Date: 11/26/2018
 * Time: 4:21 PM
 */
public class HeadUtils {
    public static Map<UUID, String> textures = new HashMap<>();

    /*
     * This should be used for caching new skulls. You can just run this method
     * without saving it anywhere to add it to the itemstack cache.
     *
     * It is easy to replace the HashMap implementation with a storage file implementation (eg YAML) by using the
     * same format.
     *
     */
    public static String getTextureUrl(UUID u) {
        try {
            if (textures.containsKey(u)) {
                return textures.get(u);
            }

            URL textureurl = new URL("https://api.minetools.eu/profile/" + u.toString().replaceAll("-", ""));
            InputStreamReader is = new InputStreamReader(textureurl.openStream());
            JsonObject textureProperty = new JsonParser().parse(is).getAsJsonObject().get("decoded").getAsJsonObject().get("textures").getAsJsonObject().get("SKIN").getAsJsonObject();
            String texture = textureProperty.get("url").getAsString();
            textures.put(u, texture);

            return texture;

        } catch (IOException | IllegalStateException e) {
            return Guilds.getGuilds().getConfig().getString("guild-list.head-default-url");
        }
    }



    public static ItemStack getSkull(String skinURL) {

        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

        if(skinURL.isEmpty()) return head;

        ItemMeta headMeta = head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", skinURL).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = headMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(profileField).setAccessible(true);

        try {
            profileField.set(headMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        head.setItemMeta(headMeta);
        return head;
    }

}
