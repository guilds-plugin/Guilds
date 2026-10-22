package me.glaremasters.guilds.guild;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.PaperCommandManager;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildJoinEvent;
import me.glaremasters.guilds.configuration.sections.HooksSettings;
import me.glaremasters.guilds.configuration.sections.PluginSettings;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers what {@code /guild code redeem} does to the player who redeems a code.
 *
 * <p>{@code /guild accept} and {@code /guild code redeem} are the two ways into a guild, and they
 * were not doing the same work. Accept fired the cancellable {@link GuildJoinEvent}, applied the
 * guild's tier permissions and the member's role permissions, and registered the member through
 * {@link Guild#addMember}; redeeming a code did none of that. A player who joined by code was a
 * member in name only: third-party plugins listening for a join never heard about it, a listener
 * that cancelled the event could not stop it, and the player kept no permissions until their next
 * login.
 *
 * <p>Only the bookkeeping is exercised here. The claim and the announcement need a claim plugin and
 * a real command manager, and the permissions are asserted through the Vault {@link Permission}
 * mock, which is the same call the plugin makes.
 */
class GuildHandlerRedeemTest {

    private static final String CODE = "ABC1234";
    private static final String TIER_PERM = "guilds.command.admin";
    private static final String ROLE_NODE = "guilds.roles.member";

    @TempDir
    File dataFolder;

    private MockedStatic<Bukkit> bukkit;
    private PluginManager pluginManager;
    private Permission permission;
    private GuildHandler handler;
    private Guild guild;
    private GuildCode code;
    private Player player;
    private UUID playerId;
    private UUID creatorId;
    private PaperCommandManager manager;

    @BeforeEach
    void setUp() throws IOException {
        writeRoles();
        writeTiers();

        final SettingsManager settingsManager = Mockito.mock(SettingsManager.class);
        Mockito.when(settingsManager.getProperty(PluginSettings.RUN_VAULT_ASYNC)).thenReturn(false);
        Mockito.when(settingsManager.getProperty(HooksSettings.WORLDGUARD)).thenReturn(false);

        permission = Mockito.mock(Permission.class);

        final Guilds guildsPlugin = Mockito.mock(Guilds.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(guildsPlugin.getDataFolder()).thenReturn(dataFolder);
        Mockito.when(guildsPlugin.getPermissions()).thenReturn(permission);
        Mockito.when(guildsPlugin.getDatabase().getGuildAdapter().getAllGuilds())
                .thenReturn(Collections.emptyList());

        handler = new GuildHandler(guildsPlugin, settingsManager);

        playerId = UUID.randomUUID();
        creatorId = UUID.randomUUID();

        player = Mockito.mock(Player.class);
        Mockito.when(player.getUniqueId()).thenReturn(playerId);
        Mockito.when(player.getName()).thenReturn("Joiner");

        pluginManager = Mockito.mock(PluginManager.class);

        // The mocks have to exist before the stubbing starts: Mockito refuses to create a mock
        // inside a `thenReturn` argument, because that is what an unfinished stubbing looks like.
        final OfflinePlayer joiner = offlinePlayer(playerId, "Joiner");
        final OfflinePlayer creator = offlinePlayer(creatorId, "Creator");

        bukkit = Mockito.mockStatic(Bukkit.class);
        bukkit.when(Bukkit::getPluginManager).thenReturn(pluginManager);
        // The generic answer first: a later stub replaces an earlier one, so the named players
        // below have to be registered after this one to win.
        bukkit.when(() -> Bukkit.getOfflinePlayer(Mockito.any(UUID.class))).thenAnswer(invocation -> {
            final UUID uuid = invocation.getArgument(0);
            return offlinePlayer(uuid, "Someone");
        });
        bukkit.when(() -> Bukkit.getOfflinePlayer(playerId)).thenReturn(joiner);
        bukkit.when(() -> Bukkit.getOfflinePlayer(creatorId)).thenReturn(creator);

        manager = Mockito.mock(PaperCommandManager.class, Mockito.RETURNS_DEEP_STUBS);

        final GuildMember master = new GuildMember(UUID.randomUUID(), handler.getGuildRole(0));
        guild = new Guild(UUID.randomUUID());
        guild.setName("TestGuild");
        guild.setPrefix("Test");
        guild.setStatus(Guild.Status.Private);
        guild.setTier(handler.getLowestGuildTier());
        guild.setMembers(new ArrayList<>(Collections.singletonList(master)));
        guild.setInvitedMembers(new ArrayList<>());
        guild.setAllies(new ArrayList<>());
        guild.setPendingAllies(new ArrayList<>());
        guild.setVaults(new ArrayList<>());
        guild.setCodes(new ArrayList<>());
        guild.setGuildMaster(master);

        code = new GuildCode(CODE, 2, creatorId, new ArrayList<>());
        guild.getCodes().add(code);

        handler.addGuild(guild);
    }

    @AfterEach
    void tearDown() {
        bukkit.close();
    }

    @Test
    @DisplayName("redeeming a code fires the join event")
    void redeemFiresTheJoinEvent() {
        handler.handleInvite(manager, player, guild, code);

        final ArgumentCaptor<GuildJoinEvent> event = ArgumentCaptor.forClass(GuildJoinEvent.class);
        Mockito.verify(pluginManager).callEvent(event.capture());

        assertEquals(playerId, event.getValue().getPlayer().getUniqueId());
        assertEquals(guild.getId(), event.getValue().getGuild().getId());
    }

    @Test
    @DisplayName("a cancelled join event stops the redeem")
    void cancelledJoinEventStopsTheRedeem() {
        Mockito.doAnswer(invocation -> {
            ((GuildJoinEvent) invocation.getArgument(0)).setCancelled(true);
            return null;
        }).when(pluginManager).callEvent(Mockito.any());

        handler.handleInvite(manager, player, guild, code);

        // Nothing was consumed: the use is still there, the player is not a redeemer and the
        // guild did not gain a member.
        assertEquals(2, code.getUses());
        assertTrue(code.getRedeemers().isEmpty());
        assertNull(guild.getMember(playerId));
        assertNull(handler.getGuildByPlayerId(playerId));
    }

    @Test
    @DisplayName("redeeming a code puts the player in the guild")
    void redeemAddsTheMember() {
        handler.handleInvite(manager, player, guild, code);

        final GuildMember member = guild.getMember(playerId);
        assertNotNull(member, "the redeemer should be a member of the guild");
        assertEquals(handler.getLowestGuildRole().getLevel(), member.getRole().getLevel());
        assertEquals(guild.getId(), handler.getGuildByPlayerId(playerId).getId());

        // The member is registered with a join date, like a member who accepted an invite.
        assertTrue(member.getJoinDate() > 0L, "the member should have a join date");
    }

    @Test
    @DisplayName("redeeming a code consumes a use and records the redeemer")
    void redeemConsumesAUse() {
        handler.handleInvite(manager, player, guild, code);

        assertEquals(1, code.getUses());
        assertEquals(Collections.singletonList(playerId), code.getRedeemers());
    }

    @Test
    @DisplayName("redeeming a code grants the guild and role permissions")
    void redeemGrantsPermissions() {
        handler.handleInvite(manager, player, guild, code);

        Mockito.verify(permission).playerAdd(Mockito.isNull(), Mockito.eq(player), Mockito.eq(TIER_PERM));
        Mockito.verify(permission).playerAdd(Mockito.isNull(), Mockito.eq(player), Mockito.eq(ROLE_NODE));
    }

    @Test
    @DisplayName("redeeming a code clears a stale invite")
    void redeemClearsAStaleInvite() {
        guild.inviteMember(playerId);
        assertTrue(guild.checkIfInvited(player));

        handler.handleInvite(manager, player, guild, code);

        assertFalse(guild.checkIfInvited(player), "the invite should have been cleared");
    }

    /**
     * Writes a roles file with the same shape the plugin generates: the master first, the lowest
     * role last, because {@link GuildHandler#getLowestGuildRole()} reads the last entry.
     */
    private void writeRoles() throws IOException {
        final String yaml = String.join("\n",
                "roles:",
                "  \"0\":",
                "    name: GuildMaster",
                "    permission-node: guilds.roles.master",
                "    permissions:",
                "      invite: true",
                "  \"3\":",
                "    name: Member",
                "    permission-node: " + ROLE_NODE,
                "    permissions:",
                "      invite: false",
                "");
        Files.write(new File(dataFolder, "roles.yml").toPath(), yaml.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Writes a tiers file whose single tier grants one node, so the tier permission the redeem
     * path is expected to hand over is observable.
     */
    private void writeTiers() throws IOException {
        final String yaml = String.join("\n",
                "tiers:",
                "  list:",
                "    1:",
                "      level: 1",
                "      name: Bronze",
                "      cost: 0.0",
                "      max-members: 2",
                "      vault-amount: 1",
                "      permissions:",
                "      - " + TIER_PERM,
                "");
        Files.write(new File(dataFolder, "tiers.yml").toPath(), yaml.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Builds an offline player whose name and online state the plugin can read.
     */
    private static OfflinePlayer offlinePlayer(final UUID uuid, final String name) {
        final OfflinePlayer offline = Mockito.mock(OfflinePlayer.class);
        Mockito.when(offline.getUniqueId()).thenReturn(uuid);
        Mockito.when(offline.getName()).thenReturn(name);
        Mockito.when(offline.isOnline()).thenReturn(false);
        return offline;
    }
}
