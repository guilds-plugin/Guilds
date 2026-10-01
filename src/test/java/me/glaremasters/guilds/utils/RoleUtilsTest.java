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

import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Covers the role-hierarchy predicates behind {@code /guild promote} and {@code /guild demote}.
 *
 * <p>These all used to dereference {@code guild.getMember(uuid)} without a null check, so a target
 * who was not in the guild threw a {@code NullPointerException} out of the command instead of
 * reporting the problem. {@code CommandPromote} then compounded it by combining the membership and
 * hierarchy checks with {@code &&}, which short-circuited the hierarchy check away for every real
 * member and evaluated it only in the case that threw.
 *
 * <p>{@code RoleUtils#promote} and {@code RoleUtils#demote} need a live {@code GuildHandler} and
 * Vault, so they are not covered here. What is covered is every check the commands run before they
 * call them.
 */
class RoleUtilsTest {

    private final List<GuildMember> members = new ArrayList<>();
    private final Guild guild = new Guild(UUID.randomUUID());

    private void withMembers(GuildMember... guildMembers) {
        members.clear();
        for (GuildMember member : guildMembers) {
            members.add(member);
        }
        // Guild#getMembers() returns the live list, so the test and the guild share it.
        guild.setMembers(members);
    }

    private static OfflinePlayer asPlayer(GuildMember member) {
        return TestFixtures.player(member.getUuid());
    }

    @Test
    @DisplayName("inGuild is false for someone who is not a member")
    void notInGuild() {
        withMembers(TestFixtures.member(TestFixtures.OFFICER));

        assertFalse(RoleUtils.inGuild(guild, TestFixtures.player(UUID.randomUUID())));
        assertTrue(RoleUtils.inGuild(guild, TestFixtures.player(members.get(0).getUuid())));
    }

    @Test
    @DisplayName("a non-member target does not blow up the hierarchy checks")
    void hierarchyChecksTolerateNonMembers() {
        withMembers(TestFixtures.member(TestFixtures.OFFICER));
        final OfflinePlayer stranger = TestFixtures.player(UUID.randomUUID());

        // Every one of these dereferenced a null GuildMember before.
        assertDoesNotThrow(() -> {
            RoleUtils.checkPromote(guild, stranger, TestFixtures.player(members.get(0).getUuid()));
            RoleUtils.sameRole(guild, TestFixtures.player(members.get(0).getUuid()), stranger);
            RoleUtils.isOfficer(guild, stranger);
            RoleUtils.canPromote(guild, stranger);
            RoleUtils.isLower(null, members.get(0));
            RoleUtils.isLower(members.get(0), null);
        });

        assertFalse(RoleUtils.checkPromote(guild, stranger, TestFixtures.player(members.get(0).getUuid())));
        assertFalse(RoleUtils.sameRole(guild, TestFixtures.player(members.get(0).getUuid()), stranger));
        assertFalse(RoleUtils.isOfficer(guild, stranger));
        assertFalse(RoleUtils.canPromote(guild, stranger));
    }

    @Test
    @DisplayName("a superior may promote a subordinate one rank at a time")
    void aSuperiorMayPromoteASubordinate() {
        final GuildMember master = TestFixtures.member(TestFixtures.MASTER);
        final GuildMember officer = TestFixtures.member(TestFixtures.OFFICER);
        final GuildMember veteran = TestFixtures.member(TestFixtures.VETERAN);
        final GuildMember member = TestFixtures.member(TestFixtures.MEMBER);
        withMembers(master, officer, veteran, member);

        // Master promotes veteran to officer. The result (level 1) is strictly below the master.
        assertTrue(RoleUtils.checkPromote(guild, asPlayer(veteran), asPlayer(master)));

        // Officer promotes a plain member to veteran. The result (level 2) is strictly below the
        // officer. This is the case the old rule missed, since it only allowed the exact rank
        // below the actor and so left the master able to promote nobody at all.
        assertTrue(RoleUtils.checkPromote(guild, asPlayer(member), asPlayer(officer)));
    }

    @Test
    @DisplayName("a promotion may not land on a rank equal to or above the actor's")
    void aPromotionMayNotEqualOrOutrankTheActor() {
        final GuildMember master = TestFixtures.member(TestFixtures.MASTER);
        final GuildMember officer = TestFixtures.member(TestFixtures.OFFICER);
        final GuildMember veteran = TestFixtures.member(TestFixtures.VETERAN);
        withMembers(master, officer, veteran);

        // Promoting the veteran would make them an officer, the same rank the actor already holds.
        assertFalse(RoleUtils.checkPromote(guild, asPlayer(veteran), asPlayer(officer)));

        // An officer can never reach the master.
        assertFalse(RoleUtils.checkPromote(guild, asPlayer(master), asPlayer(officer)));

        // Nor can the master reach a second master.
        assertFalse(RoleUtils.checkPromote(guild, asPlayer(master), asPlayer(master)));
    }

    @Test
    @DisplayName("promoting an officer is refused so a second guild master cannot be created")
    void officerIsNotPromotable() {
        withMembers(TestFixtures.member(TestFixtures.MASTER), TestFixtures.member(TestFixtures.OFFICER));

        // Level 1 promotes to level 0, which is the guild master role. That seat changes hands
        // through /guild transfer, so promotion must never hand it out.
        assertTrue(RoleUtils.isOfficer(guild, asPlayer(members.get(1))));
        assertFalse(RoleUtils.checkPromote(guild, asPlayer(members.get(1)), asPlayer(members.get(0))));
    }

    @Test
    @DisplayName("the guild master is already at the top and cannot be promoted")
    void masterIsNotPromotable() {
        withMembers(TestFixtures.member(TestFixtures.MASTER));

        assertFalse(RoleUtils.canPromote(guild, asPlayer(members.get(0))));
    }

    @Test
    @DisplayName("sameRole distinguishes equal and differing ranks")
    void sameRole() {
        final GuildMember one = TestFixtures.member(TestFixtures.VETERAN);
        final GuildMember other = TestFixtures.member(TestFixtures.VETERAN);
        final GuildMember third = TestFixtures.member(TestFixtures.MEMBER);
        withMembers(one, other, third);

        assertTrue(RoleUtils.sameRole(guild, asPlayer(one), asPlayer(other)));
        assertFalse(RoleUtils.sameRole(guild, asPlayer(one), asPlayer(third)));
    }

    @Test
    @DisplayName("isLower reports whether the first member outranks the second")
    void isLower() {
        final GuildMember master = TestFixtures.member(TestFixtures.MASTER);
        final GuildMember member = TestFixtures.member(TestFixtures.MEMBER);
        withMembers(master, member);

        // A master has a lower level number, so the master is "lower" than a plain member.
        assertTrue(RoleUtils.isLower(master, member));
        assertFalse(RoleUtils.isLower(member, master));
    }

    @Test
    @DisplayName("isSamePlayer compares uuids, not names")
    void isSamePlayer() {
        final UUID uuid = UUID.randomUUID();
        final OfflinePlayer one = TestFixtures.player(uuid);
        final OfflinePlayer same = TestFixtures.player(uuid);
        final OfflinePlayer different = TestFixtures.player(UUID.randomUUID());

        assertTrue(RoleUtils.isSamePlayer(one, same));
        assertFalse(RoleUtils.isSamePlayer(one, different));
        assertFalse(RoleUtils.isSamePlayer(one, null));
        assertFalse(RoleUtils.isSamePlayer(null, one));
        assertFalse(RoleUtils.isSamePlayer(null, null));
    }

    @Test
    @DisplayName("two different players are never treated as the same person")
    void distinctPlayersAreDistinct() {
        final UUID first = UUID.randomUUID();
        final UUID second = UUID.randomUUID();

        assertNotEquals(first, second);
        assertFalse(RoleUtils.isSamePlayer(TestFixtures.player(first), TestFixtures.player(second)));
    }
}
