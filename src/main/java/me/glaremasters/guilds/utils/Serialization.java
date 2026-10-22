package me.glaremasters.guilds.utils;

import ch.jalu.configme.SettingsManager;
import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.dumptruckman.bukkit.configuration.util.SerializationHelper;
import me.glaremasters.guilds.configuration.sections.GuildVaultSettings;
import net.minidev.json.parser.JSONParser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A utility class for serializing and deserializing Minecraft inventories.
 *
 * <p>An inventory is stored as a JSON document with a {@code size} and an {@code items} section
 * keyed by the slot index. That shape is what every guild file on disk already holds, so it is not
 * changed here: the two halves of this class have to keep agreeing on it.
 *
 * <p>Reading is the half that has to expect broken data. The document is written by one server
 * version and read by another, it can be edited by hand, and it can be left half-written by a
 * crash. A single unreadable entry may not take the rest of the vault, or the rest of the guild,
 * down with it, so the items are read one at a time and the ones that cannot be read are skipped.
 */
public class Serialization {

    /**
     * The number of slots a vault has. It is also the largest inventory the plugin creates, and the
     * size a stored vault falls back to when the stored size is not a size an inventory can have.
     */
    private static final int VAULT_SIZE = 54;

    /**
     * The mode the configuration library parses JSON with. It is the mode the library uses itself,
     * so a document is read back exactly the way it was written: whole numbers stay whole numbers.
     */
    private static final int JSON_MODE = JSONParser.USE_INTEGER_STORAGE;

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
     * @return the deserialized Inventory, or null if the stored data cannot be read at all
     * @throws InvalidConfigurationException retained for source compatibility
     */
    public static Inventory deserializeInventory(String jsons, SettingsManager settingsManager) throws InvalidConfigurationException {
        return deserializeInventory(jsons, null, settingsManager);
    }

    /**
     * Deserialize an Inventory from a JSON string.
     *
     * <p>The items are read one at a time. An item that cannot be read, because it is not an item
     * or because it holds something this server version does not have, is skipped and the rest of
     * the vault is still returned. A slot that is not a number, or one outside the inventory, is
     * skipped for the same reason.
     *
     * @param jsons           the JSON string
     * @param title           the title of the Inventory
     * @param settingsManager the SettingsManager to retrieve the title if none is provided
     * @return the deserialized Inventory, or null if the stored data cannot be read at all
     * @throws InvalidConfigurationException retained for source compatibility
     */
    public static Inventory deserializeInventory(String jsons, String title, SettingsManager settingsManager) throws InvalidConfigurationException {
        title = StringUtils.color(settingsManager.getProperty(GuildVaultSettings.VAULT_NAME));

        final Map<String, Object> document = readDocument(jsons);
        if (document == null) {
            return null;
        }

        final Object stored = document.get("items");
        if (!(stored instanceof Map)) {
            // A document without an items section holds nothing that can be read back.
            return null;
        }

        final int size = readSize(document.get("size"));
        final Inventory inventory;
        try {
            inventory = Bukkit.createInventory(null, size, title);
        } catch (RuntimeException e) {
            LoggingUtils.warn("Unable to create a vault of size " + size + ".", e);
            return null;
        }

        for (Map.Entry<?, ?> entry : ((Map<?, ?>) stored).entrySet()) {
            final Object value = entry.getValue();
            if (value == null) {
                // An empty slot.
                continue;
            }
            final int slot = readSlot(entry.getKey());
            if (slot < 0 || slot >= size) {
                LoggingUtils.warn("Skipping the item stored in slot '" + entry.getKey() + "' of a vault: a vault of size " + size + " has no such slot.");
                continue;
            }
            final ItemStack item = readItem(value);
            if (item == null) {
                LoggingUtils.warn("Skipping the item stored in slot '" + entry.getKey() + "' of a vault: it could not be read.");
                continue;
            }
            inventory.setItem(slot, item);
        }
        return inventory;
    }

    /**
     * Reads the stored document.
     *
     * <p>The document is parsed with the same parser, and in the same mode, as the configuration
     * library uses, so the numbers in it come out the way they went in.
     *
     * @param jsons the stored document
     * @return the document, or null if it is not a document at all
     */
    private static Map<String, Object> readDocument(String jsons) {
        if (jsons == null || jsons.trim().isEmpty()) {
            return null;
        }
        try {
            final Object parsed = new JSONParser(JSON_MODE).parse(jsons);
            if (!(parsed instanceof Map)) {
                return null;
            }
            final Map<String, Object> document = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) parsed).entrySet()) {
                document.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return document;
        } catch (Exception e) {
            LoggingUtils.warn("Unable to read a stored vault: the data is not a valid inventory document.", e);
            return null;
        }
    }

    /**
     * Reads the number of slots the stored vault has.
     *
     * @param stored the stored size
     * @return the size, or the size of a vault when the stored one is not usable
     */
    private static int readSize(Object stored) {
        int size = VAULT_SIZE;
        if (stored instanceof Number) {
            size = ((Number) stored).intValue();
        } else if (stored instanceof String) {
            try {
                size = Integer.parseInt(((String) stored).trim());
            } catch (NumberFormatException e) {
                size = VAULT_SIZE;
            }
        }
        if (size < 9 || size % 9 != 0 || size > VAULT_SIZE) {
            LoggingUtils.warn("A stored vault declares the size " + stored + ", which is not a size an inventory can have. Using " + VAULT_SIZE + " instead.");
            return VAULT_SIZE;
        }
        return size;
    }

    /**
     * Reads the slot an item is stored in.
     *
     * @param stored the stored slot
     * @return the slot, or -1 when it is not a slot
     */
    private static int readSlot(Object stored) {
        try {
            return Integer.parseInt(String.valueOf(stored).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Reads a stored item.
     *
     * <p>The item is deserialized on its own, so that an item this server version cannot read
     * costs that one item and not the whole vault. The configuration library is asked to do the
     * deserializing, because it also resolves the item meta nested inside the item.
     *
     * @param stored the stored item
     * @return the item, or null if it cannot be read
     */
    private static ItemStack readItem(Object stored) {
        if (stored instanceof ItemStack) {
            return (ItemStack) stored;
        }
        if (!(stored instanceof Map)) {
            return null;
        }
        try {
            final Object item = SerializationHelper.deserialize((Map<?, ?>) stored);
            return item instanceof ItemStack ? (ItemStack) item : null;
        } catch (Exception e) {
            LoggingUtils.warn("Unable to read a stored vault item.", e);
            return null;
        }
    }

}
