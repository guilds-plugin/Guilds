package me.glaremasters.guilds.utils;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFBukkitUtil;
import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import me.glaremasters.guilds.configuration.sections.GuildVaultSettings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GlareMasters
 * Date: 3/19/2019
 * Time: 11:49 PM
 */
public class Serialization {

    private Serialization() {}

    public static String serializeInventory(Inventory inventory) {
        return serializeInventory(inventory.getSize(), inventory.getContents());
    }

    /**
     * Serialize the inventory to JSON
     * @param size size of inventory
     * @param items the items to be serialized
     * @return serialized inventory
     */
    public static String serializeInventory(int size, ItemStack[] items) {
        JsonConfiguration json = new JsonConfiguration();
        json.set("size", size);
        int idx = 0;
        Map<String, ItemStack> itemMap = new HashMap<>();
        for (ItemStack item : items) {
            int i = idx++;
            itemMap.put("" + i, item);
        }
        json.createSection("items", itemMap);
        return json.saveToString();
    }

    public static Inventory deserializeInventory(String jsons, SettingsManager settingsManager) throws InvalidConfigurationException {
        return deserializeInventory(jsons, null, settingsManager);
    }

    /**
     * Deserialize the inventory from JSON
     * @param jsons the JSON string
     * @param title the name of the inventory
     * @return the deserialized string
     * @throws InvalidConfigurationException
     */
    public static Inventory deserializeInventory(String jsons, String title, SettingsManager settingsManager) throws InvalidConfigurationException {
        try {
            JsonConfiguration json = new JsonConfiguration();
            json.loadFromString(jsons);

            int size = json.getInt("size", 54);
            title = ACFBukkitUtil.color(settingsManager.getProperty(GuildVaultSettings.VAULT_NAME));

            Inventory inventory = Bukkit.createInventory(null, size, title);
            Map<String, Object> items = json.getConfigurationSection("items").getValues(false);
            for (Map.Entry<String, Object> item : items.entrySet()) {
                ItemStack itemstack = (ItemStack) item.getValue();
                int idx = Integer.parseInt(item.getKey());
                inventory.setItem(idx, itemstack);
            }
            return inventory;

        } catch (InvalidConfigurationException e) {
            return null;
        }
    }



}
