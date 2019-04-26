package me.glaremasters.guilds.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;

/**
 * Created by Glare
 * Date: 4/26/2019
 * Time: 10:20 AM
 */
public class SkullUtils {

    public static String getEncoded(String skinUrl) {
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", skinUrl).getBytes());
        return new String(encodedData);
    }

    public static GameProfile getGameProfile(String url) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", url));
        return profile;
    }

    public static ItemStack getSkull(String skinUrl) {
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

}