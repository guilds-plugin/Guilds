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

import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import org.bukkit.OfflinePlayer;
import org.mockito.Mockito;

import java.util.UUID;

/**
 * Shared fixtures for the tests.
 *
 * <p>These build the real domain objects rather than mocks wherever possible. {@code GuildRole} and
 * {@code GuildMember} are plain data holders, and {@code Guild} has a plain constructor, so the
 * logic under test is exercised against the types it actually runs on.
 *
 * <p>{@link OfflinePlayer} is the exception. It is a large server-provided interface, so mocking it
 * is cheaper and clearer than implementing every method.
 */
final class TestFixtures {

    /**
     * The role hierarchy the plugin ships by default: level 0 is the guild master and the number
     * grows as rank drops. {@code GuildHandler#getLowestGuildRole} returns the highest level.
     */
    static final GuildRole MASTER = new GuildRole("GuildMaster", "guilds.roles.master", 0);
    static final GuildRole OFFICER = new GuildRole("Officer", "guilds.roles.officer", 1);
    static final GuildRole VETERAN = new GuildRole("Veteran", "guilds.roles.veteran", 2);
    static final GuildRole MEMBER = new GuildRole("Member", "guilds.roles.member", 3);

    private TestFixtures() {
    }

    /**
     * @param uuid the player's unique id
     * @return an offline player that reports the given uuid
     */
    static OfflinePlayer player(UUID uuid) {
        final OfflinePlayer player = Mockito.mock(OfflinePlayer.class);
        Mockito.when(player.getUniqueId()).thenReturn(uuid);
        return player;
    }

    /**
     * @param role the role the member starts on
     * @return a member with a random uuid on the given role
     */
    static GuildMember member(GuildRole role) {
        return new GuildMember(UUID.randomUUID(), role);
    }

    /**
     * @param role the role the member starts on
     * @return a member with a known uuid on the given role
     */
    static GuildMember member(UUID uuid, GuildRole role) {
        return new GuildMember(uuid, role);
    }
}
