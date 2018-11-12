package me.glaremasters.guilds.utils;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GlareMasters
 * Date: 9/10/2018
 * Time: 5:07 PM
 */
public class Serialization {

    private Serialization() {}

    public static String serializeInventory(Inventory inventory) {
        return serializeInventory(inventory.getTitle(), inventory.getSize(), inventory.getStorageContents());
    }

    /**
     * Serialize the inventory to JSON
     * @param title name of inventory
     * @param size size of inventory
     * @param items the items to be serialized
     * @return serialized inventory
     */
    public static String serializeInventory(String title, int size, ItemStack[] items) {
        JsonConfiguration json = new JsonConfiguration();
        json.set("size", size);
        json.set("name", title);
        int idx = 0;
        Map<String, ItemStack> itemMap = new HashMap<>();
        for (ItemStack item : items) {
            int i = idx++;
            itemMap.put("" + i, item);
        }
        json.createSection("items", itemMap);
        return json.saveToString();
    }

    public static Inventory deserializeInventory(String jsons) throws InvalidConfigurationException {
        return deserializeInventory(jsons, null);
    }

    /**
     * Deserialize the inventory from JSON
     * @param jsons the JSON string
     * @param title the name of the inventory
     * @return the deserialized string
     * @throws InvalidConfigurationException
     */
    public static Inventory deserializeInventory(String jsons, String title) throws InvalidConfigurationException {
        try {
            JsonConfiguration json = new JsonConfiguration();
            json.loadFromString(jsons);

            int size = json.getInt("size", 54);
            if (title == null) {
                title = json.getString("name");
            }

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
