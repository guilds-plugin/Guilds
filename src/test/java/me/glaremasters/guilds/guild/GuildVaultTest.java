package me.glaremasters.guilds.guild;

import ch.jalu.configme.SettingsManager;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.guild.GuildAdapter;
import me.glaremasters.guilds.utils.BukkitFixtures;
import me.glaremasters.guilds.utils.Serialization;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the vault cache of a guild and for the way it is written back.
 *
 * <p>The cache is positional: the entry at index N is vault N, and a player asking for vault N
 * gets vault N. A stored vault that cannot be read is the case that breaks that, because the
 * natural way to handle it, leaving it out of the cache, moves every vault after it one place up.
 * A guild with three vaults whose second one is unreadable would then serve the contents of the
 * third vault to the player who asked for the second one, and write the third vault over the
 * second one the next time the guild is saved.
 *
 * <p>The second half is the save. A stored vault that cannot be read is still data: it is either a
 * vault this server version cannot make sense of, or a vault that was written badly. Replacing it
 * with an empty vault throws it away, and refusing to save the guild at all throws away
 * everything else the guild has in its vaults.
 */
class GuildVaultTest {

    private static final String UNREADABLE = "this is not a vault";

    @TempDir
    Path dataFolder;

    private MockedStatic<Bukkit> bukkit;
    private SettingsManager settingsManager;
    private GuildHandler handler;

    @BeforeEach
    void setUp() throws IOException {
        bukkit = BukkitFixtures.mockServer();
        settingsManager = BukkitFixtures.settings();
        handler = new GuildHandler(plugin(), settingsManager);
    }

    @AfterEach
    void tearDown() {
        bukkit.close();
    }

    /**
     * A plugin whose data folder holds the two configuration files the handler reads, and whose
     * database holds no guilds.
     *
     * @return the plugin
     */
    private Guilds plugin() throws IOException {
        Files.write(dataFolder.resolve("roles.yml"), ("roles:\n"
                + "  0:\n"
                + "    name: GuildMaster\n"
                + "    permission-node: guilds.roles.master\n").getBytes("UTF-8"));
        Files.write(dataFolder.resolve("tiers.yml"), ("tiers:\n"
                + "  list:\n"
                + "    1:\n"
                + "      level: 1\n"
                + "      name: Tier 1\n").getBytes("UTF-8"));

        final GuildAdapter adapter = Mockito.mock(GuildAdapter.class);
        Mockito.when(adapter.getAllGuilds()).thenReturn(Collections.emptyList());
        final DatabaseAdapter database = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(database.getGuildAdapter()).thenReturn(adapter);

        final Guilds plugin = Mockito.mock(Guilds.class);
        Mockito.when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
        Mockito.when(plugin.getDatabase()).thenReturn(database);
        return plugin;
    }

    /**
     * A stored vault holding a single item, so that the vaults of a guild can be told apart.
     *
     * @param material the material of the item
     * @return the stored vault
     */
    private static String stored(Material material) {
        final Inventory inventory = BukkitFixtures.inventory(54);
        inventory.setItem(0, new ItemStack(material, 1));
        return Serialization.serializeInventory(inventory);
    }

    /**
     * A guild with the given stored vaults.
     *
     * @param vaults the stored vaults
     * @return the guild
     */
    private static Guild guild(List<String> vaults) {
        final Guild guild = new Guild(UUID.randomUUID());
        guild.setName("Test");
        guild.setVaults(vaults);
        return guild;
    }

    @Test
    @DisplayName("a vault that cannot be read does not move the vaults after it")
    void unreadableVaultKeepsItsPlace() {
        final Guild guild = guild(Arrays.asList(stored(Material.STONE), UNREADABLE, stored(Material.DIRT)));

        handler.addGuild(guild);

        final List<Inventory> cache = handler.getVaults().get(guild);
        assertEquals(3, cache.size(), "the cache has to have a slot for every stored vault");
        assertEquals(Material.STONE, cache.get(0).getItem(0).getType(), "the first vault is the first vault");
        assertNull(cache.get(1).getItem(0), "the vault that cannot be read opens empty");
        assertEquals(Material.DIRT, cache.get(2).getItem(0).getType(), "the third vault is still the third vault");
    }

    @Test
    @DisplayName("the vault a player asks for is the vault they get")
    void unreadableVaultIsNotServedToTheWrongPlayer() {
        final Guild guild = guild(Arrays.asList(stored(Material.STONE), UNREADABLE, stored(Material.DIRT)));

        handler.addGuild(guild);

        assertEquals(Material.STONE, handler.getGuildVault(guild, 1).getItem(0).getType());
        assertNull(handler.getGuildVault(guild, 2).getItem(0));
        assertEquals(Material.DIRT, handler.getGuildVault(guild, 3).getItem(0).getType());
    }

    @Test
    @DisplayName("the data of a vault that cannot be read survives a save of the guild")
    void unreadableVaultIsNotWrittenOver() throws IOException {
        final Guild guild = guild(Arrays.asList(stored(Material.STONE), UNREADABLE, stored(Material.DIRT)));

        handler.addGuild(guild);
        handler.saveData();

        final List<String> vaults = guild.getVaults();
        assertEquals(3, vaults.size(), "the stored vaults have to stay the same length");
        assertEquals(UNREADABLE, vaults.get(1), "the data that could not be read has to be left alone");
    }

    @Test
    @DisplayName("the other vaults of a guild are saved even when one of them cannot be read")
    void otherVaultsAreSaved() throws Exception {
        final Guild guild = guild(Arrays.asList(stored(Material.STONE), UNREADABLE, stored(Material.DIRT)));

        handler.addGuild(guild);
        // The player puts something into the first vault.
        handler.getGuildVault(guild, 1).setItem(5, new ItemStack(Material.GOLD_INGOT, 3));
        handler.saveData();

        final List<String> vaults = guild.getVaults();
        assertEquals(3, vaults.size());
        final Inventory first = Serialization.deserializeInventory(vaults.get(0), settingsManager);
        assertNotNull(first, "the first vault has to be saved");
        assertEquals(Material.GOLD_INGOT, first.getItem(5).getType(), "what the player put in has to be saved");
        assertEquals(Material.DIRT, Serialization.deserializeInventory(vaults.get(2), settingsManager).getItem(0).getType());
    }

    @Test
    @DisplayName("a vault that is opened for the first time is stored")
    void newVaultIsStored() throws Exception {
        final Guild guild = guild(Collections.emptyList());

        handler.addGuild(guild);
        // The player opens the first vault, which the guild has never had.
        handler.getVaults().get(guild).add(handler.createNewVault(settingsManager));
        handler.getGuildVault(guild, 1).setItem(0, new ItemStack(Material.EMERALD, 7));
        handler.saveData();

        assertEquals(1, guild.getVaults().size(), "the vault the player opened has to be stored");
        final Inventory first = Serialization.deserializeInventory(guild.getVaults().get(0), settingsManager);
        assertEquals(Material.EMERALD, first.getItem(0).getType());
        assertEquals(7, first.getItem(0).getAmount());
    }

    @Test
    @DisplayName("a stored vault the cache does not have is not dropped")
    void storedVaultWithoutCacheIsKept() {
        final List<String> stored = Arrays.asList(stored(Material.STONE), UNREADABLE, stored(Material.DIRT));
        final List<Inventory> cache = new ArrayList<>();
        cache.add(BukkitFixtures.inventory(54));

        final List<String> written = GuildHandler.serializeVaultCache(cache, stored, Collections.singleton(1));

        assertEquals(3, written.size(), "the stored list must not be truncated");
        assertEquals(stored.get(1), written.get(1), "the stored vaults that are not cached have to be left alone");
        assertEquals(stored.get(2), written.get(2));
    }

    @Test
    @DisplayName("a vault that cannot be read keeps its data until something is put into it")
    void unreadableVaultKeepsItsDataUntilItIsUsed() throws Exception {
        final List<String> stored = Arrays.asList(UNREADABLE);
        final List<Inventory> cache = new ArrayList<>();
        cache.add(BukkitFixtures.inventory(54));
        final Set<Integer> unreadable = Collections.singleton(0);

        assertEquals(stored, GuildHandler.serializeVaultCache(cache, stored, unreadable),
                "an untouched vault that cannot be read keeps the data it has");

        cache.get(0).setItem(0, new ItemStack(Material.DIAMOND, 1));
        final List<String> written = GuildHandler.serializeVaultCache(cache, stored, unreadable);
        assertNotEquals(UNREADABLE, written.get(0), "what the player put in has to be saved");
        assertEquals(Material.DIAMOND, Serialization.deserializeInventory(written.get(0), settingsManager).getItem(0).getType());
    }

    @Test
    @DisplayName("the vaults of a guild are read in the order they are stored")
    void cacheKeepsTheStoredOrder() {
        final List<String> stored = Arrays.asList(stored(Material.STONE), stored(Material.DIRT), stored(Material.GOLD_INGOT));

        final Set<Integer> unreadable = new HashSet<>();
        final List<Inventory> cache = GuildHandler.buildVaultCache(stored, settingsManager, unreadable);

        assertEquals(3, cache.size());
        assertTrue(unreadable.isEmpty(), "nothing was unreadable");
        assertEquals(Material.STONE, cache.get(0).getItem(0).getType());
        assertEquals(Material.DIRT, cache.get(1).getItem(0).getType());
        assertEquals(Material.GOLD_INGOT, cache.get(2).getItem(0).getType());
    }
}
