package me.glaremasters.guilds.guild;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers how a member is registered with a guild, for both ways in.
 *
 * <p>{@code /guild accept} and {@code /guild code redeem} are the two ways into a guild, and they
 * used to register the member differently. Accept went through {@link Guild#addMember}, which
 * cleared a stale invite and stamped the member with the date they joined. A code went through
 * {@link Guild#addMemberByCode}, which did neither, so a member who joined by code showed a join
 * date of zero — the epoch, in the members list — until their next login patched it up.
 *
 * <p>Both now go through the same registration.
 */
class GuildMemberRegistrationTest {

    private static final GuildRole MASTER = new GuildRole("GuildMaster", "guilds.roles.master", 0);
    private static final GuildRole MEMBER = new GuildRole("Member", "guilds.roles.member", 3);

    private Guild guild;
    private GuildMember master;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        master = new GuildMember(UUID.randomUUID(), MASTER);
        playerId = UUID.randomUUID();

        guild = new Guild(UUID.randomUUID());
        guild.setMembers(new ArrayList<>());
        guild.getMembers().add(master);
        guild.setInvitedMembers(new ArrayList<>());
        guild.setGuildMaster(master);
    }

    @Test
    @DisplayName("a member added by code is stamped with the date they joined")
    void codeMemberHasAJoinDate() {
        guild.addMemberByCode(new GuildMember(playerId, MEMBER));

        final GuildMember member = guild.getMember(playerId);
        assertNotNull(member);
        assertTrue(member.getJoinDate() > 0L, "the member should have a join date");
    }

    @Test
    @DisplayName("a member added by code clears the invite they came with")
    void codeMemberClearsTheInvite() {
        guild.inviteMember(playerId);

        guild.addMemberByCode(new GuildMember(playerId, MEMBER));

        assertFalse(guild.checkIfInvited(invitedPlayer()), "the invite should have been cleared");
    }

    @Test
    @DisplayName("a member who is already in the guild is not added twice")
    void aMemberIsOnlyInTheGuildOnce() {
        guild.addMemberByCode(new GuildMember(playerId, MEMBER));
        guild.addMemberByCode(new GuildMember(playerId, MEMBER));

        assertEquals(1, guild.getMembers().stream().filter(m -> m.getUuid().equals(playerId)).count());
    }

    @Test
    @DisplayName("a member added by code keeps the role the caller chose")
    void codeMemberKeepsItsRole() {
        guild.addMemberByCode(new GuildMember(playerId, MEMBER));

        assertEquals(MEMBER, guild.getMember(playerId).getRole());
    }

    @Test
    @DisplayName("an invited member is registered the same way a code member is")
    void invitedMemberIsRegisteredTheSameWay() {
        final GuildHandler handler = Mockito.mock(GuildHandler.class);
        Mockito.when(handler.getLowestGuildRole()).thenReturn(MEMBER);

        final OfflinePlayer player = Mockito.mock(OfflinePlayer.class);
        Mockito.when(player.getUniqueId()).thenReturn(playerId);
        guild.inviteMember(playerId);

        guild.addMember(player, handler);

        final GuildMember member = guild.getMember(playerId);
        assertNotNull(member);
        assertTrue(member.getJoinDate() > 0L, "the member should have a join date");
        assertFalse(guild.checkIfInvited(invitedPlayer()), "the invite should have been cleared");
    }

    @Test
    @DisplayName("a player who is already in the guild is not added a second time")
    void anInvitedPlayerIsNotAddedTwice() {
        final GuildHandler handler = Mockito.mock(GuildHandler.class);
        Mockito.when(handler.getLowestGuildRole()).thenReturn(MEMBER);

        final OfflinePlayer player = Mockito.mock(OfflinePlayer.class);
        Mockito.when(player.getUniqueId()).thenReturn(playerId);

        guild.addMember(player, handler);
        guild.addMember(player, handler);

        assertEquals(1, guild.getMembers().stream().filter(m -> m.getUuid().equals(playerId)).count());
    }

    @Test
    @DisplayName("a member who is not in the guild is not found")
    void anUnknownMemberIsNotFound() {
        assertNull(guild.getMember(playerId));
    }

    /**
     * The invited player, as the guild sees them.
     */
    private Player invitedPlayer() {
        final Player player = Mockito.mock(Player.class);
        Mockito.when(player.getUniqueId()).thenReturn(playerId);
        return player;
    }
}
