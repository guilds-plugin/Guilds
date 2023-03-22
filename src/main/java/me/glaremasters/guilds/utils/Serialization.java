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
package me.glaremasters.guilds.utils;

import ch.jalu.configme.SettingsManager;
import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import me.glaremasters.guilds.configuration.sections.GuildVaultSettings;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for serializing and deserializing Minecraft inventories.
 */
public class Serialization {

    private Serialization() {
    }

    /**
     * Serialize an Inventory to a JSON string.
     *
     * @param inventory the Inventory to be serialized
     * @return the serialized JSON string
     */
    public static String serializeInventory(Inventory inventory) {
        return serializeInventory(inventory.getSize(), inventory.getContents());
    }

    /**
     * Serialize an Inventory to a JSON string.
     *
     * @param size  size of the Inventory
     * @param items the ItemStacks in the Inventory
     * @return the serialized JSON string
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

    /**
     * Deserialize an Inventory from a JSON string.
     *
     * @param jsons           the JSON string
     * @param settingsManager the SettingsManager to retrieve the title
     * @return the deserialized Inventory
     * @throws InvalidConfigurationException
     */
    public static Inventory deserializeInventory(String jsons, SettingsManager settingsManager) throws InvalidConfigurationException {
        return deserializeInventory(jsons, null, settingsManager);
    }

    /**
     * Deserialize an Inventory from a JSON string.
     *
     * @param jsons           the JSON string
     * @param title           the title of the Inventory
     * @param settingsManager the SettingsManager to retrieve the title if none is provided
     * @return the deserialized Inventory
     * @throws InvalidConfigurationException
     */
    public static Inventory deserializeInventory(String jsons, String title, SettingsManager settingsManager) throws InvalidConfigurationException {
        try {
            JsonConfiguration json = new JsonConfiguration();
            json.loadFromString(jsons);

            int size = json.getInt("size", 54);
            title = StringUtils.color(settingsManager.getProperty(GuildVaultSettings.VAULT_NAME));

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
