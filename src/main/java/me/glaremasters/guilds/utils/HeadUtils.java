package me.glaremasters.guilds.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.glaremasters.guilds.Guilds;

public class HeadUtils {

	private static Map<UUID, String> textures = new HashMap<UUID, String>();
	
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
			
			URL textureurl = new URL(
					"https://sessionserver.mojang.com/session/minecraft/profile" + u + "?unsigned=false");
			InputStreamReader is = new InputStreamReader(textureurl.openStream());
			JsonObject textureProperty = new JsonParser().parse(is).getAsJsonObject().get("properties").getAsJsonArray()
					.get(0).getAsJsonObject();

			String texture = textureProperty.get("value").getAsString();

			byte[] decoded = Base64.getDecoder().decode(texture.getBytes());

			String dec = new String(decoded);
			textures.put(u, dec);
			
			return dec;
			
		} catch (IOException e) {
			return Guilds.getGuilds().getConfig().getString("guild-list.head-default-url");
		}
	}
	
	
	
    public static ItemStack getSkull(String skinURL) {
    	
        ItemStack head = new ItemStack(Material.SKULL_ITEM);
        
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
        profileField.setAccessible(true);
        try {
            profileField.set(headMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        head.setItemMeta(headMeta);
        return head;
    }
	
}
