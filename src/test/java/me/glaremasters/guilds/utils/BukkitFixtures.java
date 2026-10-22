package me.glaremasters.guilds.utils;

import ch.jalu.configme.SettingsManager;
import me.glaremasters.guilds.configuration.sections.GuildVaultSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Server-side stubs for the tests that touch inventories.
 *
 * <p>{@link Bukkit} is a static holder, so the only way to exercise code that creates inventories
 * without a running server is to mock the class. Mockito's static mocking covers the whole class,
 * so every static the code under test touches has to be stubbed: an unstubbed one returns null and
 * the next call on it throws.
 */
public final class BukkitFixtures {

    /**
     * The data version reported for items. The real value depends on the server version; nothing in
     * the code under test branches on it.
     */
    private static final int DATA_VERSION = 3465;

    private BukkitFixtures() {
    }

    /**
     * Mocks the static Bukkit class with the pieces the inventory code needs.
     *
     * @return the static mock, which the caller has to close
     */
    public static MockedStatic<Bukkit> mockServer() {
        final MockedStatic<Bukkit> bukkit = Mockito.mockStatic(Bukkit.class);

        final ItemFactory itemFactory = Mockito.mock(ItemFactory.class);
        // No item has meta, so the factory never reports one.
        Mockito.when(itemFactory.equals(Mockito.any(), Mockito.any())).thenReturn(false);
        bukkit.when(Bukkit::getItemFactory).thenReturn(itemFactory);

        final UnsafeValues unsafe = Mockito.mock(UnsafeValues.class);
        Mockito.when(unsafe.getDataVersion()).thenReturn(DATA_VERSION);
        Mockito.when(unsafe.getMaterial(Mockito.anyString(), Mockito.anyInt()))
                .thenAnswer(invocation -> Material.getMaterial(invocation.getArgument(0, String.class)));
        bukkit.when(Bukkit::getUnsafe).thenReturn(unsafe);

        bukkit.when(() -> Bukkit.createInventory(Mockito.isNull(), Mockito.anyInt(), Mockito.anyString()))
                .thenAnswer(invocation -> inventory(invocation.getArgument(1, Integer.class)));

        return bukkit;
    }

    /**
     * Builds an inventory that behaves like a chest: a fixed size, an array of slots, and an
     * {@code Inventory} view of it.
     *
     * @param size the number of slots
     * @return the inventory
     */
    public static Inventory inventory(int size) {
        final ItemStack[] contents = new ItemStack[size];
        final Inventory inventory = Mockito.mock(Inventory.class);
        Mockito.when(inventory.getSize()).thenReturn(size);
        Mockito.when(inventory.getContents()).thenAnswer(invocation -> contents.clone());
        Mockito.when(inventory.getItem(Mockito.anyInt()))
                .thenAnswer(invocation -> contents[invocation.getArgument(0, Integer.class)]);
        Mockito.doAnswer(invocation -> {
            contents[invocation.getArgument(0, Integer.class)] = invocation.getArgument(1);
            return null;
        }).when(inventory).setItem(Mockito.anyInt(), Mockito.any());
        return inventory;
    }

    /**
     * A settings manager that answers with the shipped vault name.
     *
     * @return the settings manager
     */
    public static SettingsManager settings() {
        final SettingsManager settingsManager = Mockito.mock(SettingsManager.class);
        Mockito.when(settingsManager.getProperty(GuildVaultSettings.VAULT_NAME)).thenReturn("&8» &rGuild Vault");
        return settingsManager;
    }
}
