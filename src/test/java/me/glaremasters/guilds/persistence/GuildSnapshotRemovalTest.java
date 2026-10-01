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

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.guild.GuildAdapter;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers what a spread capture does when a guild is deleted while it is running.
 *
 * <p>{@code GuildHandler#removeGuild} deletes the guild from the database. A capture that serialises the
 * guild afterwards puts it straight back, so the owner disbands a guild and it reappears on the next
 * save. With a single-pass capture the window was the length of the capture; spread over ticks it is
 * however long the capture takes, which at a thousand guilds is most of a second.
 *
 * <p>GuildHandler is constructed for real here, because it is not otherwise testable: its constructor
 * reads roles.yml and tiers.yml off the plugin data folder and queries the database. Both are satisfied
 * with real files and a stubbed adapter.
 */
class GuildSnapshotRemovalTest {

    @TempDir
    Path dataFolder;

    @BeforeEach
    void installGson() throws Exception {
        final java.lang.reflect.Field field = Guilds.class.getDeclaredField("gson");
        field.setAccessible(true);
        field.set(null, new com.google.gson.GsonBuilder().setPrettyPrinting().create());
    }

    /**
     * Builds a real {@link GuildHandler} holding {@code count} guilds.
     *
     * @param count how many guilds to add
     * @return the handler
     */
    static GuildHandler newHandler(int count) throws IOException {
        return newHandler(count, null);
    }

    /**
     * Builds a real {@link GuildHandler} holding {@code count} guilds.
     *
     * @param count how many guilds to add
     * @param folder where to write roles.yml and tiers.yml, or null for a temporary directory
     * @return the handler
     */
    static GuildHandler newHandler(int count, Path folder) throws IOException {
        final Path folderToUse = folder == null
                ? Files.createTempDirectory("guilds-handler")
                : folder;

        writeConfigFiles(folderToUse);

        final Guilds plugin = Mockito.mock(Guilds.class);
        Mockito.when(plugin.getDataFolder()).thenReturn(folderToUse.toFile());

        final DatabaseAdapter database = Mockito.mock(DatabaseAdapter.class);
        final GuildAdapter adapter = Mockito.mock(GuildAdapter.class);
        Mockito.when(adapter.getAllGuilds()).thenReturn(new ArrayList<Guild>());
        Mockito.when(database.getGuildAdapter()).thenReturn(adapter);
        Mockito.when(plugin.getDatabase()).thenReturn(database);

        final GuildHandler handler = new GuildHandler(plugin, null);

        final ArrayList<Guild> guilds = new ArrayList<Guild>();
        for (int i = 0; i < count; i++) {
            final Guild guild = new Guild(UUID.randomUUID());
            guild.setName("Guild " + i);
            guilds.add(guild);
        }

        for (Guild guild : guilds) {
            // addGuild deserialises whatever vault strings the guild carries, and
            // Serialization#deserializeInventory calls Bukkit.createInventory, which needs a server. So the
            // guilds arrive with no vault strings.
            guild.setVaults(new ArrayList<String>());
            handler.addGuild(guild);
        }

        return handler;
    }

    @Test
    @DisplayName("a guild removed before the capture reaches it is not written back")
    void aGuildRemovedBeforeTheCaptureReachesItIsNotWrittenBack() throws IOException {
        final GuildHandler handler = newHandler(8);

        final CaptureSession started = new CaptureSession(handler, null, null, null, null);

        // One tick's worth, on a one-nanosecond budget, so the capture is part-way through.
        started.step(1L);
        assertTrue(started.hasWork(), "the capture should not have finished yet");

        final ArrayList<Guild> remaining = new ArrayList<Guild>(handler.getGuilds().values());
        final Guild removed = remaining.get(remaining.size() - 1);
        handler.removeGuild(removed);

        int guard = 0;
        while (!started.step(Long.MAX_VALUE / 4L)) {
            guard++;
            assertTrue(guard < 1000, "the capture should finish");
        }

        final PluginSnapshot snapshot = started.finish();

        assertEquals(7, snapshot.getGuilds().size(), "the removed guild must not be in the snapshot");
        assertFalse(snapshot.getGuilds().containsKey(removed.getId().toString()),
                "writing it back would undo the delete in removeGuild");
    }

    @Test
    @DisplayName("a guild removed after the capture passed it is dropped at finish")
    void aGuildRemovedAfterTheCapturePassedItIsDroppedAtFinish() throws IOException {
        // The other order, and the one that used to be a permanent data-loss bug.
        //
        // `step` re-reads each guild before serialising it, which covers a guild removed before its turn.
        // This covers a guild serialised on tick one and disbanded on tick two: the record was a true
        // snapshot when taken, but `removeGuild` deleted the row and the write would put it straight back.
        //
        // Nothing self-corrects that. `GuildAdapter` upserts by id and has no delete pass, so a guild a
        // later save does not mention is left alone. Resurrecting it would restore the balance, the vault
        // contents and the home, permanently, with no way to clear them from inside the plugin. Hence the
        // re-validation in `finish`.
        final GuildHandler handler = newHandler(4);

        final CaptureSession started = new CaptureSession(handler, null, null, null, null);

        // Drain the first guild, then remove it. It is already in the snapshot's guild map at this point.
        started.step(1L);
        final Guild removed = handler.getGuilds().values().iterator().next();
        handler.removeGuild(removed);

        int guard = 0;
        while (!started.step(Long.MAX_VALUE / 4L)) {
            guard++;
            assertTrue(guard < 1000, "the capture should finish");
        }

        final PluginSnapshot snapshot = started.finish();

        assertFalse(snapshot.getGuilds().containsKey(removed.getId().toString()),
                "a guild disbanded mid-capture must not be written back, or it is resurrected permanently");
        assertEquals(3, snapshot.getGuilds().size(), "the three survivors should still be there");
    }

    @Test
    @DisplayName("removing every guild mid-capture yields an empty snapshot")
    void removingEveryGuildMidCaptureYieldsAnEmptySnapshot() throws IOException {
        // One guild has already been serialised by the time the removals happen. `finish` drops it along
        // with the rest, so a guild disbanded at any point during a spread capture is never written back.
        final GuildHandler handler = newHandler(6);

        final CaptureSession started = new CaptureSession(handler, null, null, null, null);
        started.step(1L);

        for (Guild guild : new ArrayList<Guild>(handler.getGuilds().values())) {
            handler.removeGuild(guild);
        }

        while (!started.step(Long.MAX_VALUE / 4L)) {
            // Drain.
        }

        assertTrue(started.finish().getGuilds().isEmpty(),
                "even the guild serialised before the removals is dropped at finish, so nothing is resurrected");
        assertTrue(handler.getGuilds().isEmpty(), "and the handler is genuinely empty");
    }

    /**
     * Writes the two config files {@code GuildHandler}'s constructor insists on.
     *
     * @param folder the plugin data folder
     */
    private static void writeConfigFiles(Path folder) throws IOException {
        final File roles = new File(folder.toFile(), "roles.yml");
        Files.write(roles.toPath(), ("roles:\n"
                + "  '0':\n    name: GuildMaster\n    permission-node: guilds.roles.master\n"
                + "  '1':\n    name: Officer\n    permission-node: guilds.roles.officer\n"
                + "  '2':\n    name: Veteran\n    permission-node: guilds.roles.veteran\n"
                + "  '3':\n    name: Member\n    permission-node: guilds.roles.member\n")
                .getBytes(StandardCharsets.UTF_8));

        final File tiers = new File(folder.toFile(), "tiers.yml");
        Files.write(tiers.toPath(), ("tiers:\n  list:\n"
                + "    '1':\n      level: 1\n      name: Tier 1\n      cost: 1000\n      max-members: 20\n"
                + "      vault-amount: 3\n      mob-xp-multiplier: 1.0\n      damage-multiplier: 1.0\n"
                + "      max-bank-balance: 10000\n      members-to-rankup: 5\n      max-allies: 10\n"
                + "      use-buffs: true\n      permissions: []\n")
                .getBytes(StandardCharsets.UTF_8));
    }
}
