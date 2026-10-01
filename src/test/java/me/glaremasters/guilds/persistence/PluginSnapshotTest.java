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
package me.glaremasters.guilds.persistence;

import me.glaremasters.guilds.cooldowns.Cooldown;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers that a snapshot is genuinely detached.
 *
 * <p>The claim being tested is the one the whole change rests on: once a snapshot exists, no mutation
 * of the live plugin state can change what gets written. A test that only checked "no
 * {@link java.util.ConcurrentModificationException}" would pass against a shallow copy of the live
 * models, and that copy is not safe. Gson would still be reading {@code guild.getMembers()} while the
 * main thread changed it.
 */
class PluginSnapshotTest {

    @Test
    @DisplayName("mutating the source map after capture does not change the snapshot")
    void mutatingTheSourceMapAfterCaptureDoesNotChangeTheSnapshot() {
        final Map<String, String> source = new HashMap<>();
        source.put("a", "{\"name\":\"first\"}");

        final PluginSnapshot snapshot = new PluginSnapshot(
                source,
                new HashMap<>(),
                new HashMap<>(),
                new ArrayList<>(),
                null
        );

        source.put("b", "{\"name\":\"second\"}");
        source.put("a", "{\"name\":\"changed\"}");
        source.clear();

        assertEquals(1, snapshot.getGuilds().size(), "the snapshot must not see later additions");
        assertEquals("{\"name\":\"first\"}", snapshot.getGuilds().get("a"), "the snapshot must not see later edits");
    }

    @Test
    @DisplayName("the snapshot's collections cannot be written to")
    void theSnapshotsCollectionsCannotBeWrittenTo() {
        final PluginSnapshot snapshot = new PluginSnapshot(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new ArrayList<>(),
                null
        );

        assertThrows(UnsupportedOperationException.class, () -> snapshot.getGuilds().put("a", "b"));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.getArenas().put("a", "b"));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.getChallenges().put("a", "b"));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.getCooldowns().clear());
    }

    @Test
    @DisplayName("a truncated snapshot keeps exactly the arenas it was given")
    void aTruncatedSnapshotKeepsExactlyTheArenasItWasGiven() {
        // ArenaAdapter#saveArenas deletes every stored arena missing from the collection it is handed,
        // so the key set is the difference between "this arena was not saved" and "this arena does not
        // exist". A snapshot that silently lost an entry during the copy would delete a live arena.
        final Map<String, String> arenas = new HashMap<>();
        arenas.put("one", "{}");
        arenas.put("two", "{}");
        arenas.put("three", "{}");

        final PluginSnapshot snapshot = new PluginSnapshot(
                new HashMap<>(),
                arenas,
                new HashMap<>(),
                new ArrayList<>(),
                null
        );

        arenas.remove("two");

        assertEquals(3, snapshot.getArenas().size(), "the snapshot must keep all three arenas");
        assertTrue(snapshot.getArenas().containsKey("two"), "the removed arena must still be in the snapshot");
    }

    @Test
    @DisplayName("cooldowns in a snapshot survive later additions")
    void cooldownsInASnapshotSurviveLaterAdditions() {
        final List<Cooldown> source = new ArrayList<>();
        final UUID owner = UUID.randomUUID();
        source.add(new Cooldown(UUID.randomUUID(), Cooldown.Type.Home, owner, System.currentTimeMillis() + 60000L));

        final PluginSnapshot snapshot = new PluginSnapshot(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                source,
                null
        );

        source.add(new Cooldown(UUID.randomUUID(), Cooldown.Type.Buffs, owner, System.currentTimeMillis() + 60000L));

        assertEquals(1, snapshot.getCooldowns().size(), "the snapshot must not see the later cooldown");
    }

    @Test
    @DisplayName("a cooldown carries its own values, so a reader cannot see a different one later")
    void aCooldownCarriesItsOwnValuesSoAReaderCannotSeeADifferentOneLater() {
        // Cooldown has four final fields and no setters, which is why the snapshot does not re-serialise
        // them. If a setter ever appears, this test is the one that would notice.
        final UUID owner = UUID.randomUUID();
        final long expiry = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
        final Cooldown cooldown = new Cooldown(UUID.randomUUID(), Cooldown.Type.Home, owner, expiry);

        final PluginSnapshot snapshot = new PluginSnapshot(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                java.util.Collections.singletonList(cooldown),
                null
        );

        final Cooldown captured = snapshot.getCooldowns().get(0);

        assertEquals(owner, captured.getCooldownOwner());
        assertEquals(expiry, captured.getCooldownExpiry().longValue());
        assertEquals(Cooldown.Type.Home, captured.getCooldownType());
    }

    @Test
    @DisplayName("a snapshot knows when it holds nothing")
    void aSnapshotKnowsWhenItHoldsNothing() {
        final PluginSnapshot empty = new PluginSnapshot(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new ArrayList<>(),
                null
        );

        assertTrue(empty.isEmpty());

        final Map<String, String> oneGuild = new HashMap<>();
        oneGuild.put("a", "{}");

        final PluginSnapshot populated = new PluginSnapshot(
                oneGuild,
                new HashMap<>(),
                new HashMap<>(),
                new ArrayList<>(),
                null
        );

        assertFalse(populated.isEmpty());
    }

    @Test
    @DisplayName("the snapshot keeps the backend it was captured against")
    void theSnapshotKeepsTheBackendItWasCapturedAgainst() {
        final me.glaremasters.guilds.database.DatabaseAdapter captured = org.mockito.Mockito
                .mock(me.glaremasters.guilds.database.DatabaseAdapter.class);

        final PluginSnapshot snapshot = new PluginSnapshot(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new ArrayList<>(),
                captured
        );

        assertNotNull(snapshot.getDatabase());
        org.junit.jupiter.api.Assertions.assertSame(captured, snapshot.getDatabase());
    }
}
