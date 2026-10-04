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
package me.glaremasters.guilds.guild;

import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link Guild#removeMember(GuildMember)} refusing to remove the guild master.
 *
 * <p>{@code guildMaster} is a bare {@link GuildMember} reference that nothing keeps in sync with the
 * member list, and {@code /guilds admin removeplayer} had no guard against naming the master. Once
 * the master was gone, {@code guildMaster} pointed at somebody who was no longer a member, so every
 * later read of it dereferenced null and {@code tryTransferGuildAdmin} refused forever — the guild
 * could never be handed to anyone else, and no error was ever shown.
 *
 * <p>{@code /guild kick} already refused to kick the master and {@code /guild leave} routes a master
 * out through the disband branch, so this guard is what actually closes the hole.
 *
 * <p>Only the member bookkeeping is exercised. The permission dance a real removal performs needs a
 * {@link GuildHandler} and a Vault {@code Permission}, so it lives in the command rather than here.
 */
class GuildRemoveMemberTest {

    private static final GuildRole MASTER = new GuildRole("GuildMaster", "guilds.roles.master", 0);
    private static final GuildRole OFFICER = new GuildRole("Officer", "guilds.roles.officer", 1);
    private static final GuildRole MEMBER = new GuildRole("Member", "guilds.roles.member", 3);

    private Guild guild;
    private GuildMember master;
    private GuildMember officer;

    @BeforeEach
    void setUp() {
        master = new GuildMember(UUID.randomUUID(), MASTER);
        officer = new GuildMember(UUID.randomUUID(), OFFICER);

        guild = new Guild(UUID.randomUUID());
        guild.setMembers(new ArrayList<>(Arrays.asList(master, officer)));
        guild.setGuildMaster(master);
    }

    @Test
    @DisplayName("an ordinary member is removed")
    void ordinaryMemberIsRemoved() {
        assertTrue(guild.removeMember(officer));

        assertEquals(1, guild.getMembers().size());
        assertFalse(guild.getMembers().contains(officer));
    }

    @Test
    @DisplayName("the guild master is not removed")
    void masterIsNotRemoved() {
        assertFalse(guild.removeMember(master));

        assertTrue(guild.getMembers().contains(master), "the master must stay in the member list");
    }

    @Test
    @DisplayName("a refused removal leaves the guild master reference usable")
    void masterReferenceSurvivesRefusal() {
        guild.removeMember(master);

        // This is the whole point. While guildMaster can still be dereferenced, getMember() resolves
        // and a later transfer has something to read.
        assertSame(master, guild.getGuildMaster());
        assertSame(master, guild.getMember(guild.getGuildMaster().getUuid()));
    }

    @Test
    @DisplayName("removing a member who is not in the guild reports false")
    void unknownMemberIsNotRemoved() {
        final GuildMember stranger = new GuildMember(UUID.randomUUID(), MEMBER);

        assertFalse(guild.removeMember(stranger));
        assertEquals(2, guild.getMembers().size());
    }

    @Test
    @DisplayName("removing null reports false instead of throwing")
    void nullIsNotRemoved() {
        assertFalse(guild.removeMember((GuildMember) null));
        assertEquals(2, guild.getMembers().size());
    }

    @Test
    @DisplayName("the master guard compares by member, not by role")
    void guardIsOnTheMemberNotTheRole() {
        // Only the member that guildMaster points at is protected. Another member holding the master
        // role is an inconsistent state this guard deliberately does not try to reason about.
        final GuildMember secondMaster = new GuildMember(UUID.randomUUID(), MASTER);
        guild.getMembers().add(secondMaster);

        assertTrue(guild.removeMember(secondMaster));
        assertTrue(guild.getMembers().contains(master));
    }

    @Test
    @DisplayName("removing the master by OfflinePlayer is refused too")
    void masterIsNotRemovedByPlayer() {
        // This is the overload /guilds admin removeplayer actually calls, so it has to refuse as
        // well. It resolves through getMember, which is why it inherits the guard.
        final OfflinePlayer asPlayer = Mockito.mock(OfflinePlayer.class);
        Mockito.when(asPlayer.getUniqueId()).thenReturn(master.getUuid());

        assertFalse(guild.removeMember(asPlayer));
        assertTrue(guild.getMembers().contains(master), "the master must stay in the member list");
        assertSame(master, guild.getGuildMaster());
    }

    @Test
    @DisplayName("an ordinary member is still removable by OfflinePlayer")
    void ordinaryMemberIsRemovedByPlayer() {
        final OfflinePlayer asPlayer = Mockito.mock(OfflinePlayer.class);
        Mockito.when(asPlayer.getUniqueId()).thenReturn(officer.getUuid());

        assertTrue(guild.removeMember(asPlayer));
        assertFalse(guild.getMembers().contains(officer));
    }

    @Test
    @DisplayName("an OfflinePlayer who is not in the guild reports false")
    void unknownPlayerIsNotRemoved() {
        final OfflinePlayer stranger = Mockito.mock(OfflinePlayer.class);
        Mockito.when(stranger.getUniqueId()).thenReturn(UUID.randomUUID());

        assertFalse(guild.removeMember(stranger));
        assertEquals(2, guild.getMembers().size());
    }
}
