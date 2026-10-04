package me.glaremasters.guilds.guild;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Tests for the order of the permission writes a tier change makes.
 *
 * <p>The bug these cover: an upgrade revoked the old tier's nodes and then granted the new tier's as
 * two independent calls, each of which hands its work to a shared executor unless
 * {@code settings.run-vault-async} is off — and it is on by default. Nothing ordered the two, so the
 * removal could land after the addition. Every tier in the shipped {@code tiers.yml} grants the same
 * node, so the removal won and the guild was left without a permission the new tier was meant to
 * grant, which a guild master only noticed because the admin commands stopped working until someone
 * handed them the node through LuckPerms.
 *
 * <p>{@link GuildHandler#applyTierPermsTo(Permission, OfflinePlayer, List, List)} is the whole of the
 * ordering guarantee, so it is exercised directly here. Testing it needs no plugin, data folder or
 * database, only a Vault {@link Permission} that records what it is asked to do.
 */
class GuildHandlerTierPermsTest {

    private static final OfflinePlayer MEMBER = mock(OfflinePlayer.class);

    /**
     * Writes down every node the provider is told to add or remove, in the order it was told, so a
     * test can assert on the sequence rather than on the end state.
     */
    private static Permission recordingPermission(List<String> calls) {
        final Permission permission = mock(Permission.class);
        doAnswer(invocation -> {
            calls.add(invocation.getMethod().getName() + " " + invocation.getArgument(2, String.class));
            return true;
        }).when(permission).playerAdd(isNull(), any(OfflinePlayer.class), anyString());
        doAnswer(invocation -> {
            calls.add(invocation.getMethod().getName() + " " + invocation.getArgument(2, String.class));
            return true;
        }).when(permission).playerRemove(isNull(), any(OfflinePlayer.class), anyString());
        return permission;
    }

    @Test
    @DisplayName("the new tier's nodes are granted after the old tier's are revoked")
    void grantLandsAfterRevoke() {
        final List<String> calls = new ArrayList<>();

        // The shipped shape: every tier grants the same placeholder node.
        GuildHandler.applyTierPermsTo(recordingPermission(calls), MEMBER,
                Arrays.asList("example.perm.here"), Arrays.asList("example.perm.here"));

        assertEquals(Arrays.asList(
                "playerRemove example.perm.here",
                "playerAdd example.perm.here"
        ), calls, "the grant has to be the last write, or the node is gone");
    }

    @Test
    @DisplayName("a node the old tier granted and the new tier does not is still revoked")
    void staleNodesAreRevoked() {
        final List<String> calls = new ArrayList<>();

        GuildHandler.applyTierPermsTo(recordingPermission(calls), MEMBER,
                Arrays.asList("example.perm.here", "old.node"),
                Arrays.asList("example.perm.here", "new.node"));

        assertEquals(Arrays.asList(
                "playerRemove example.perm.here",
                "playerRemove old.node",
                "playerAdd example.perm.here",
                "playerAdd new.node"
        ), calls);
    }

    @Test
    @DisplayName("empty entries in a tier's node list are skipped, not handed to Vault")
    void blankNodesAreSkipped() {
        final List<String> calls = new ArrayList<>();

        GuildHandler.applyTierPermsTo(recordingPermission(calls), MEMBER,
                Arrays.asList("", "example.perm.here"),
                Arrays.asList("example.perm.here", ""));

        assertEquals(Arrays.asList(
                "playerRemove example.perm.here",
                "playerAdd example.perm.here"
        ), calls);
    }

    @Test
    @DisplayName("a tier that grants nothing leaves the player with nothing")
    void bothSidesEmpty() {
        final List<String> calls = new ArrayList<>();

        GuildHandler.applyTierPermsTo(recordingPermission(calls), MEMBER, List.of(), List.of());

        assertEquals(List.of(), calls);
    }
}
