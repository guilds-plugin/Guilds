package me.glaremasters.guilds.guild;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.PaperCommandManager;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.guild.GuildAdapter;
import me.glaremasters.guilds.guis.VaultGUI;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.BukkitFixtures;
import me.glaremasters.guilds.utils.Serialization;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
 * <p>The second half is the save. A stored vault that cannot be read in full is still data: the
 * items that could not be read are in the stored document and nowhere else. Replacing that
 * document with the inventory that could be read throws those items away, and refusing to save
 * the guild at all throws away everything else the guild has in its vaults. So a vault that could
 * not be read is kept exactly as it was, is not opened, and the file that holds it is copied aside.
 */
class GuildVaultTest {

    private static final String UNREADABLE = "this is not a vault";

    @TempDir
    Path dataFolder;

    private MockedStatic<Bukkit> bukkit;
    private SettingsManager settingsManager;
    private BukkitCommandIssuer issuer;
    private Guilds plugin;
    private GuildHandler handler;

    @BeforeEach
    void setUp() throws IOException {
        bukkit = BukkitFixtures.mockServer();
        settingsManager = BukkitFixtures.settings();
        issuer = Mockito.mock(BukkitCommandIssuer.class);
        plugin = plugin();
        handler = new GuildHandler(plugin, settingsManager);
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
                + "    permission-node: guilds.roles.master\n").getBytes(StandardCharsets.UTF_8));
        Files.write(dataFolder.resolve("tiers.yml"), ("tiers:\n"
                + "  list:\n"
                + "    1:\n"
                + "      level: 1\n"
                + "      name: Tier 1\n"
                + "      vault-amount: 5\n").getBytes(StandardCharsets.UTF_8));

        final GuildAdapter adapter = Mockito.mock(GuildAdapter.class);
        Mockito.when(adapter.getAllGuilds()).thenReturn(Collections.emptyList());
        final DatabaseAdapter database = Mockito.mock(DatabaseAdapter.class);
        Mockito.when(database.getGuildAdapter()).thenReturn(adapter);

        final PaperCommandManager commandManager = Mockito.mock(PaperCommandManager.class);
        Mockito.doReturn(issuer).when(commandManager).getCommandIssuer(Mockito.any());

        final Guilds plugin = Mockito.mock(Guilds.class);
        Mockito.when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
        Mockito.when(plugin.getDatabase()).thenReturn(database);
        Mockito.when(plugin.getCommandManager()).thenReturn(commandManager);
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
     * A stored vault holding a good item and an item the running server cannot make sense of.
     *
     * <p>WOOD is a 1.8.8 material that does not exist on the servers the tests run against, so the
     * item cannot be reconstructed. The other item is an ordinary one.
     *
     * @return the stored vault
     */
    private static String storedWithUnreadableItem() {
        return "{\"size\":54,\"items\":{"
                + "\"0\":{\"==\":\"org.bukkit.inventory.ItemStack\",\"v\":3465,\"type\":\"STONE\"},"
                + "\"1\":{\"==\":\"org.bukkit.inventory.ItemStack\",\"v\":3465,\"type\":\"WOOD\"}}}";
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
        guild.setTier(GuildTier.builder().level(1).name("Tier 1").vaultAmount(5).build());
        guild.setVaults(vaults);
        return guild;
    }

    /**
     * Writes the file a guild is stored in, the way the JSON storage provider does.
     *
     * @param guild the guild
     * @param content the content of the file
     * @return the file
     */
    private Path writeGuildFile(Guild guild, String content) throws IOException {
        final Path file = dataFolder.resolve("data").resolve(guild.getId() + ".json");
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    /**
     * The copies of the guild files that were made, under the backup directory.
     *
     * @return every file under the backup directory
     */
    private List<Path> backups() throws IOException {
        final Path root = dataFolder.resolve("backups").resolve("corrupt");
        if (!Files.isDirectory(root)) {
            return Collections.emptyList();
        }
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(Files::isRegularFile).collect(java.util.stream.Collectors.toList());
        }
    }

    @Test
    @DisplayName("a vault that cannot be read does not move the vaults after it")
    void unreadableVaultKeepsItsPlace() {
        final Guild guild = guild(Arrays.asList(stored(Material.STONE), UNREADABLE, stored(Material.DIRT)));

        handler.addGuild(guild);

        final List<Inventory> cache = handler.getVaults().get(guild);
        assertEquals(3, cache.size(), "the cache has to have a slot for every stored vault");
        assertEquals(Material.STONE, cache.get(0).getItem(0).getType(), "the first vault is the first vault");
        assertNull(cache.get(1).getItem(0), "the vault that cannot be read holds nothing");
        assertEquals(Material.DIRT, cache.get(2).getItem(0).getType(), "the third vault is still the third vault");
    }

    @Test
    @DisplayName("the vault a player asks for is the vault they get")
    void unreadableVaultIsNotServedToTheWrongPlayer() {
        final Guild guild = guild(Arrays.asList(stored(Material.STONE), UNREADABLE, stored(Material.DIRT)));

        handler.addGuild(guild);

        assertEquals(Material.STONE, handler.getGuildVault(guild, 1).getItem(0).getType());
        assertNull(handler.getGuildVault(guild, 2), "the vault that cannot be read is not opened");
        assertEquals(Material.DIRT, handler.getGuildVault(guild, 3).getItem(0).getType());
    }

    @Test
    @DisplayName("the data of a vault that cannot be read survives a save of the guild")
    void unreadableVaultIsNotWrittenOver() throws Exception {
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
        final Guild guild = guild(stored);
        final List<Inventory> cache = new ArrayList<>();
        cache.add(BukkitFixtures.inventory(54));

        final List<String> written = GuildHandler.serializeVaultCache(guild, cache, Collections.singleton(1));

        assertEquals(3, written.size(), "the stored list must not be truncated");
        assertEquals(stored.get(1), written.get(1), "the stored vaults that are not cached have to be left alone");
        assertEquals(stored.get(2), written.get(2));
    }

    @Test
    @DisplayName("the vaults of a guild are read in the order they are stored")
    void cacheKeepsTheStoredOrder() {
        final List<String> stored = Arrays.asList(stored(Material.STONE), stored(Material.DIRT), stored(Material.GOLD_INGOT));

        final Set<Integer> unreadable = new HashSet<>();
        final List<Inventory> cache = GuildHandler.buildVaultCache(guild(stored), settingsManager, unreadable);

        assertEquals(3, cache.size());
        assertTrue(unreadable.isEmpty(), "nothing was unreadable");
        assertEquals(Material.STONE, cache.get(0).getItem(0).getType());
        assertEquals(Material.DIRT, cache.get(1).getItem(0).getType());
        assertEquals(Material.GOLD_INGOT, cache.get(2).getItem(0).getType());
    }

    @Test
    @DisplayName("an item that cannot be read stays in the stored data when the guild is saved")
    void unreadableItemIsKeptOnSave() throws Exception {
        final Guild guild = guild(Arrays.asList(storedWithUnreadableItem(), stored(Material.DIRT)));

        handler.addGuild(guild);
        handler.saveData();

        final List<String> vaults = guild.getVaults();
        assertEquals(2, vaults.size());
        assertTrue(vaults.get(0).contains("WOOD"), "the item that could not be read is gone from the stored vault");
        assertTrue(vaults.get(0).contains("STONE"), "the item that could be read is gone from the stored vault");
    }

    @Test
    @DisplayName("the stored data of an affected vault survives a save, a reload and a second save")
    void unreadableItemSurvivesAReload() throws Exception {
        final Guild guild = guild(Arrays.asList(storedWithUnreadableItem(), stored(Material.DIRT)));
        final String original = guild.getVaults().get(0);

        handler.addGuild(guild);
        handler.saveData();
        assertEquals(original, guild.getVaults().get(0), "the first save changed the stored data");

        // A reload: the guild is read again from the same stored data.
        final GuildHandler reloaded = new GuildHandler(plugin, settingsManager);
        reloaded.addGuild(guild);
        reloaded.saveData();

        assertEquals(original, guild.getVaults().get(0), "the reload and the second save changed the stored data");
    }

    @Test
    @DisplayName("a vault with an item that cannot be read is not opened")
    void vaultWithUnreadableItemIsNotOpened() {
        final Guild guild = guild(Arrays.asList(stored(Material.STONE), storedWithUnreadableItem()));

        handler.addGuild(guild);

        assertTrue(handler.isVaultDamaged(guild, 2), "the vault has to be marked as damaged");
        assertNull(handler.getGuildVault(guild, 2), "the vault must not be opened");
        assertNotNull(handler.getGuildVault(guild, 1), "the other vaults of the guild still open");
    }

    @Test
    @DisplayName("a player who opens a vault with an item that cannot be read is told why")
    void playerIsToldWhyTheVaultIsRefused() {
        final Guild guild = guild(Arrays.asList(stored(Material.STONE), storedWithUnreadableItem()));
        final Player player = Mockito.mock(Player.class);

        handler.addGuild(guild);
        final Inventory vault = handler.getGuildVault(guild, player, 2);

        assertNull(vault, "the vault must not be opened");
        Mockito.verify(issuer).sendInfo(Messages.VAULTS__UNREADABLE, "{number}", "2");
    }

    @Test
    @DisplayName("opening a vault with an item that cannot be read is refused through the vault GUI")
    void theVaultGuiRefusesTheVault() {
        final Guild guild = guild(Arrays.asList(stored(Material.STONE), storedWithUnreadableItem()));
        final Player player = Mockito.mock(Player.class);
        final VaultGUI gui = new VaultGUI(plugin, settingsManager, handler);

        handler.addGuild(guild);
        final boolean opened = gui.open(guild, player, 2);

        assertFalse(opened, "the vault must not be opened");
        Mockito.verify(player, Mockito.never()).openInventory(Mockito.any(Inventory.class));
        Mockito.verify(issuer).sendInfo(Messages.VAULTS__UNREADABLE, "{number}", "2");
    }

    @Test
    @DisplayName("the other vaults of a guild with a damaged vault still open and still save")
    void otherVaultsOfADamagedGuildStillWork() throws Exception {
        final Guild guild = guild(Arrays.asList(stored(Material.STONE), storedWithUnreadableItem(), stored(Material.DIRT)));
        final Player player = Mockito.mock(Player.class);

        handler.addGuild(guild);

        assertNotNull(handler.getGuildVault(guild, player, 1), "the first vault still opens");
        assertNotNull(handler.getGuildVault(guild, player, 3), "the third vault still opens");
        handler.getGuildVault(guild, 1).setItem(2, new ItemStack(Material.EMERALD, 4));
        handler.saveData();

        final List<String> vaults = guild.getVaults();
        assertEquals(3, vaults.size());
        assertTrue(vaults.get(0).contains("EMERALD"), "the first vault has to be saved");
        assertTrue(vaults.get(1).contains("WOOD"), "the damaged vault has to keep its data");
        assertTrue(vaults.get(2).contains("DIRT"), "the third vault has to be saved");
    }

    @Test
    @DisplayName("the guild file is copied when a vault of it cannot be read")
    void guildFileIsCopiedWhenAVaultCannotBeRead() throws Exception {
        final Guild guild = guild(Arrays.asList(stored(Material.STONE), storedWithUnreadableItem()));
        final Path file = writeGuildFile(guild, "{\"id\":\"" + guild.getId() + "\",\"vaults\":[]}");

        handler.addGuild(guild);

        final List<Path> backups = backups();
        assertEquals(1, backups.size(), "the guild file has to be copied: " + backups);
        assertArrayEquals(Files.readAllBytes(file), Files.readAllBytes(backups.get(0)), "the copy has to hold the same bytes");
        assertTrue(backups.get(0).toString().contains("guilds"), "the copy belongs in the guilds folder: " + backups.get(0));
    }

    @Test
    @DisplayName("the guild file is copied when a vault of it cannot be read at all")
    void guildFileIsCopiedWhenAVaultCannotBeReadAtAll() throws Exception {
        final Guild guild = guild(Arrays.asList(stored(Material.STONE), UNREADABLE));
        final Path file = writeGuildFile(guild, "{\"id\":\"" + guild.getId() + "\",\"vaults\":[]}");

        handler.addGuild(guild);
        handler.saveData();

        final List<Path> backups = backups();
        assertEquals(1, backups.size(), "the guild file has to be copied: " + backups);
        assertArrayEquals(Files.readAllBytes(file), Files.readAllBytes(backups.get(0)), "the copy has to hold the same bytes");
        assertEquals(UNREADABLE, guild.getVaults().get(1), "the data that could not be read has to be left alone");
    }

    @Test
    @DisplayName("a guild whose vaults are all healthy is not affected")
    void healthyGuildIsUnaffected() throws Exception {
        final Guild guild = guild(Arrays.asList(stored(Material.STONE), stored(Material.DIRT)));
        final Player player = Mockito.mock(Player.class);

        handler.addGuild(guild);

        assertFalse(handler.isVaultDamaged(guild, 1), "a healthy vault is not damaged");
        assertFalse(handler.isVaultDamaged(guild, 2), "a healthy vault is not damaged");
        assertNotNull(handler.getGuildVault(guild, player, 1), "a healthy vault opens");
        assertNotNull(handler.getGuildVault(guild, player, 2), "a healthy vault opens");
        Mockito.verify(issuer, Mockito.never()).sendInfo(Mockito.any(), Mockito.any(String[].class));

        handler.getGuildVault(guild, 1).setItem(0, new ItemStack(Material.EMERALD, 2));
        handler.saveData();

        assertTrue(guild.getVaults().get(0).contains("EMERALD"), "the vault has to be saved");
        assertTrue(guild.getVaults().get(1).contains("DIRT"), "the other vault has to be saved");
        assertTrue(backups().isEmpty(), "a guild with healthy vaults needs no backup");
    }
}
