package me.glaremasters.guilds.utils;

import ch.jalu.configme.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for reading a stored guild vault back into an inventory.
 *
 * <p>A vault is stored as a JSON document with a {@code size} and an {@code items} section keyed by
 * the slot index. The document is written by the plugin, but it sits in the guild file on disk and
 * can be edited, truncated or left behind by a server version whose items no longer exist. Reading
 * it is therefore the one place in the vault code that has to assume the data is broken: a single
 * bad entry must not take the rest of the vault, or the rest of the guild, down with it.
 *
 * <p>An item that cannot be read is still in the document, and the document is the only copy of it.
 * The read therefore reports which items it could not reconstruct, and no half-read inventory is
 * handed out through {@link Serialization#deserializeInventory(String, SettingsManager)}: an
 * inventory with a hole in it looks complete to whoever holds it, and writing it back loses the
 * item that is missing.
 */
class SerializationTest {

    /**
     * One stored item, in the shape the plugin writes it.
     *
     * @param material the material of the item
     * @return the item as it appears in the stored document
     */
    private static String item(Material material) {
        return "{\"==\":\"org.bukkit.inventory.ItemStack\",\"v\":3465,\"type\":\"" + material.name() + "\"}";
    }

    /**
     * A stored document with the given items, keyed by slot.
     *
     * @param entries the entries as {@code slot: item} pairs
     * @return the document
     */
    private static String document(String... entries) {
        final StringBuilder items = new StringBuilder();
        for (String entry : entries) {
            if (items.length() > 0) {
                items.append(',');
            }
            items.append(entry);
        }
        return "{\"size\":54,\"items\":{" + items + "}}";
    }

    private MockedStatic<Bukkit> bukkit;
    private SettingsManager settingsManager;

    @BeforeEach
    void setUp() {
        bukkit = BukkitFixtures.mockServer();
        settingsManager = BukkitFixtures.settings();
    }

    @AfterEach
    void tearDown() {
        bukkit.close();
    }

    @Test
    @DisplayName("a vault written by the plugin reads back with the same items")
    void roundTrip() throws Exception {
        final Inventory written = BukkitFixtures.inventory(54);
        written.setItem(0, new ItemStack(Material.STONE, 5));
        written.setItem(9, new ItemStack(Material.DIRT, 2));

        final String stored = Serialization.serializeInventory(written);
        final Inventory read = Serialization.deserializeInventory(stored, settingsManager);

        assertNotNull(read, "the vault the plugin just wrote has to read back");
        assertEquals(54, read.getSize());
        assertEquals(Material.STONE, read.getItem(0).getType());
        assertEquals(5, read.getItem(0).getAmount());
        assertEquals(Material.DIRT, read.getItem(9).getType());
        assertEquals(2, read.getItem(9).getAmount());
    }

    @Test
    @DisplayName("the stored document keeps the shape the guild files already have")
    void storedShape() {
        final Inventory written = BukkitFixtures.inventory(54);
        written.setItem(0, new ItemStack(Material.STONE, 5));

        final String stored = Serialization.serializeInventory(written);

        // The guild files on disk hold this shape. Changing it would orphan every vault that is
        // already stored, so it is pinned here.
        assertTrue(stored.startsWith("{\"size\":54,\"items\":{"), stored);
        assertTrue(stored.contains("\"0\":{"), stored);
        assertTrue(stored.contains("\"org.bukkit.inventory.ItemStack\""), stored);
    }

    @Test
    @DisplayName("a slot key that is not a number is reported as an item that could not be read")
    void malformedSlotKey() throws Exception {
        final String stored = document("\"first\":" + item(Material.STONE), "\"0\":" + item(Material.DIRT));

        final Serialization.ReadResult read = assertDoesNotThrow(() -> Serialization.readInventory(stored, settingsManager),
                "a slot key that is not a number must not take the vault down");

        assertTrue(read.isReadable(), "the rest of the vault still has to be read");
        assertFalse(read.isComplete(), "the read has to report that an item was left out");
        assertEquals(Arrays.asList("first"), read.getUnreadableSlots());
        assertEquals(Material.DIRT, read.getInventory().getItem(0).getType(), "the item in a valid slot still has to be there");
        assertNull(Serialization.deserializeInventory(stored, settingsManager),
                "an inventory with an item missing from it must not be handed out");
    }

    @Test
    @DisplayName("a slot outside the inventory is reported as an item that could not be read")
    void slotOutsideTheInventory() throws Exception {
        final String stored = document("\"0\":" + item(Material.STONE), "\"999\":" + item(Material.DIRT));

        final Serialization.ReadResult read = assertDoesNotThrow(() -> Serialization.readInventory(stored, settingsManager),
                "a slot outside the inventory must not take the vault down");

        assertFalse(read.isComplete(), "the read has to report that an item was left out");
        assertEquals(Arrays.asList("999"), read.getUnreadableSlots());
        assertEquals(Material.STONE, read.getInventory().getItem(0).getType());
    }

    @Test
    @DisplayName("a value that is not an item is reported as an item that could not be read")
    void valueThatIsNotAnItem() throws Exception {
        final String stored = document("\"0\":" + item(Material.STONE), "\"1\":\"not an item\"");

        final Serialization.ReadResult read = assertDoesNotThrow(() -> Serialization.readInventory(stored, settingsManager),
                "a value that is not an item must not take the vault down");

        assertFalse(read.isComplete(), "the read has to report that an item was left out");
        assertEquals(Arrays.asList("1"), read.getUnreadableSlots());
        assertEquals(Material.STONE, read.getInventory().getItem(0).getType());
    }

    @Test
    @DisplayName("an item whose material does not exist on this server is reported as an item that could not be read")
    void itemWithAnUnknownMaterial() throws Exception {
        final String stored = document("\"0\":" + item(Material.STONE),
                "\"1\":{\"==\":\"org.bukkit.inventory.ItemStack\",\"v\":3465,\"type\":\"WOOD\"}");

        final Serialization.ReadResult read = assertDoesNotThrow(() -> Serialization.readInventory(stored, settingsManager),
                "an item that cannot exist on this server must not take the vault down");

        assertTrue(read.isReadable(), "the vault still has to open, with the items that can be read");
        assertFalse(read.isComplete(), "the read has to report that an item was left out");
        assertEquals(Arrays.asList("1"), read.getUnreadableSlots());
        assertEquals(Material.STONE, read.getInventory().getItem(0).getType());
        assertNull(read.getInventory().getItem(1));
        assertNull(Serialization.deserializeInventory(stored, settingsManager),
                "an inventory with an item missing from it must not be handed out");
    }

    @Test
    @DisplayName("an item with meta that cannot be read does not take the vault down")
    void itemWithUnreadableMeta() throws Exception {
        final String stored = document("\"0\":" + item(Material.STONE),
                "\"1\":{\"==\":\"org.bukkit.inventory.ItemStack\",\"v\":3465,\"type\":\"DIRT\","
                        + "\"meta\":{\"==\":\"org.bukkit.inventory.meta.ItemMeta\",\"display\":{\"raw\":\"hello\"}}}");

        final Serialization.ReadResult read = assertDoesNotThrow(() -> Serialization.readInventory(stored, settingsManager),
                "an item whose meta cannot be read must not take the vault down");

        assertNotNull(read, "the vault still has to open");
        assertEquals(Material.STONE, read.getInventory().getItem(0).getType(), "the other items are still there");
    }

    @Test
    @DisplayName("a document that is not JSON at all is reported as unreadable")
    void documentThatIsNotJson() throws Exception {
        assertNull(Serialization.deserializeInventory("this is not a vault", settingsManager));
        assertNull(Serialization.deserializeInventory("", settingsManager));
        assertNull(Serialization.deserializeInventory(null, settingsManager));

        final Serialization.ReadResult read = Serialization.readInventory("this is not a vault", settingsManager);
        assertFalse(read.isReadable(), "a document that is not a document cannot be read");
        assertNull(read.getInventory());
        assertTrue(read.getUnreadableSlots().isEmpty());
    }

    @Test
    @DisplayName("a document without an items section is reported as unreadable")
    void documentWithoutItems() throws Exception {
        assertNull(Serialization.deserializeInventory("{\"size\":54}", settingsManager));
        assertFalse(Serialization.readInventory("{\"size\":54}", settingsManager).isReadable());
    }
}
