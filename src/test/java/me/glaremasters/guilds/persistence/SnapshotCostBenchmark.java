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

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildCode;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildHome;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.guild.GuildScore;
import me.glaremasters.guilds.guild.GuildTier;
import me.glaremasters.guilds.utils.Serialization;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Measures what {@link PersistenceCoordinator#capture()} costs on the main thread.
 *
 * <p>Not a correctness test, and deliberately without a timing assertion: the figures depend on the machine
 * they were taken on, so they are printed rather than asserted. {@code build.gradle.kts} excludes the
 * {@code benchmark} tag from {@code test} and {@code testJava11}, so this does not run on every build;
 * {@code ./gradlew benchmark} runs it.
 *
 * <p>What can and cannot be measured without a server: {@code Bukkit.getServer()} is null here and there is
 * no {@code Bukkit.setServer}, so {@code Bukkit.createInventory}, {@code ItemStack.serialize},
 * {@code ItemStack.getItemMeta}, {@code Bukkit.getItemFactory} and {@code Bukkit.getUnsafe} all throw.
 * {@code Inventory} is an interface, though, so a Mockito mock supplies {@code getSize()} and
 * {@code getContents()} and {@code Serialization#serializeInventory(Inventory)} runs end to end against real
 * code with zero Bukkit calls as long as the slots are null. {@code ItemStack} is a final concrete class
 * whose {@code serialize()} reaches the server, so {@link VaultItemSurrogate} stands in for it at the
 * exact point of substitution.
 */
@Tag("benchmark")
class SnapshotCostBenchmark {

    /**
     * A Minecraft tick, in milliseconds. The number every figure below has to be divided by.
     */
    private static final double TICK_MILLIS = 50.0;

    /** Exactly what {@code Guilds#onEnable} assigns to its Gson, which is null in a unit test. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final GuildRole MASTER = new GuildRole("GuildMaster", "guilds.roles.master", 0);
    private static final GuildRole OFFICER = new GuildRole("Officer", "guilds.roles.officer", 1);
    private static final GuildRole VETERAN = new GuildRole("Veteran", "guilds.roles.veteran", 2);
    private static final GuildRole MEMBER = new GuildRole("Member", "guilds.roles.member", 3);

    private static final GuildRole[] ROLES = {MASTER, OFFICER, VETERAN, MEMBER};

    /**
     * Only {@code level} is serialised; the rest of a {@link GuildTier} is {@code transient}.
     */
    private static final GuildTier TIER = GuildTier.builder()
            .level(1)
            .name("Tier 1")
            .cost(1000)
            .maxMembers(20)
            .vaultAmount(3)
            .mobXpMultiplier(1.0)
            .damageMultiplier(1.0)
            .maxBankBalance(10000)
            .membersToRankup(5)
            .maxAllies(10)
            .useBuffs(true)
            .permissions(new ArrayList<String>())
            .build();

    private static final int[] GUILD_COUNTS = {1, 100, 1000, 5000};

    /**
     * The budget the autosave spends per tick, matching {@code Guilds#CAPTURE_BUDGET_MILLIS}.
     */
    private static final long BUDGET_MILLIS = 3L;

    /**
     * Whatever {@code Guilds.gson} held before {@link #installProductionGson()} replaced it.
     */
    private static Gson previousGson;

    /**
     * {@code capture()} reads {@code Guilds.getGson()}, so section 5 is the only measurement here that runs
     * production {@code capture()} rather than a copy of its body. That needs reflection to arrange.
     */
    @BeforeAll
    static void installProductionGson() throws Exception {
        final Field field = Guilds.class.getDeclaredField("gson");
        field.setAccessible(true);
        previousGson = (Gson) field.get(null);
        field.set(null, GSON);
    }

    @AfterAll
    static void restoreGsonAndUnregisterSurrogate() throws Exception {
        final Field field = Guilds.class.getDeclaredField("gson");
        field.setAccessible(true);
        field.set(null, previousGson);

        // ConfigurationSerialization is a process-wide static registry and JUnit reuses the JVM across test
        // classes, so undo the alias rather than leaving a lie in it.
        ConfigurationSerialization.unregisterClass("org.bukkit.inventory.ItemStack");
    }

    // ---------------------------------------------------------------------------------------
    // 1. Guild JSON, with the vault payload held constant so this measures the guild and not
    //    the vaults.
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("1. guild JSON serialisation cost, isolated from vault serialisation")
    void guildJsonCost() {
        header("1. GUILD JSON (gson.toJson(guild, Guild.class)) -- vaults held at a fixed small payload");

        // Warm the JIT on the same code path before anything is recorded.
        final List<Guild> warmup = guilds(200, 15);
        for (int i = 0; i < 30; i++) {
            serializeAll(warmup);
        }

        for (int n : GUILD_COUNTS) {
            final List<Guild> guilds = guilds(n, 15);
            final int reps = repsFor(n);
            final long[] samples = measure(reps, () -> serializeAll(guilds));
            final long median = median(samples);
            final String json = GSON.toJson(guilds.get(0), Guild.class);

            final double totalMillis = median / 1_000_000.0;
            final double perGuildMicros = median / 1_000.0 / n;

            row("N = " + n,
                    fmt(totalMillis) + " ms",
                    fmt(perGuildMicros) + " us",
                    fmt(json.length() / 1024.0) + " KiB",
                    fmt(TICK_MILLIS * 1000.0 / perGuildMicros) + " guilds/tick",
                    fmt(TICK_MILLIS / totalMillis) + " captures/tick");
        }

        note("JSON size is the compact representation of one populated guild, 15 members, 3 vault strings.");
        reportHeap("after the largest fixture in this file (5000 guilds x 15 members)");
    }

    // ---------------------------------------------------------------------------------------
    // 2. Vault inventory serialisation: the part that could actually blow the budget.
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("2. vault inventory serialisation cost through the production JsonConfiguration path")
    void vaultInventoryCost() {
        header("2. VAULT SERIALISATION (Serialization#serializeInventory)");

        // Warm the JsonConfiguration / json-smart path hard: the per-vault figures below are tens of
        // microseconds and an un-warmed constructor plus json-smart's first pass is milliseconds.
        final Object[] empty = emptySlots(54);
        final Object[] mixed = mixedVault(54);
        for (int i = 0; i < 4000; i++) {
            serializeVaultSurrogate(54, empty);
            serializeVaultSurrogate(54, mixed);
            Serialization.serializeInventory(EMPTY_ITEMSTACKS_INVENTORY);
        }

        final int reps = 7;
        final int perRep = 3000;

        // 2a. Production call, end to end, through a mocked Inventory with every slot empty.
        final long[] emptySamples = measure(reps, () -> {
            for (int i = 0; i < perRep; i++) {
                Serialization.serializeInventory(EMPTY_ITEMSTACKS_INVENTORY);
            }
        });
        reportVault("2a. 54 slots, all empty (exact production path)", emptySamples, perRep,
                Serialization.serializeInventory(EMPTY_ITEMSTACKS_INVENTORY));

        // 2b. Surrogate payload, mixed density. See VaultItemSurrogate for what is substituted.
        final long[] mixedSamples = measure(reps, () -> {
            for (int i = 0; i < perRep; i++) {
                serializeVaultSurrogate(54, mixed);
            }
        });
        reportVault("2b. 54 slots, mixed (14 empty / 22 stacked / 14 named / 4 enchanted)", mixedSamples,
                perRep, serializeVaultSurrogate(54, mixed));

        // 2c. Worst case: every slot a full enchanted stack.
        final Object[] full = fullVault(54);
        final long[] fullSamples = measure(reps, () -> {
            for (int i = 0; i < perRep; i++) {
                serializeVaultSurrogate(54, full);
            }
        });
        reportVault("2c. 54 slots, every one an enchanted named stack (worst case)", fullSamples, perRep,
                serializeVaultSurrogate(54, full));
        reportHeap("after 21000 vault serialisations");

        extrapolate();
    }

    private void extrapolate() {
        header("2d. EXTRAPOLATION -- a guild with 3 vaults, by guild count");

        // Recomputed here from the same workload as 2a/2b so the table cannot drift from the printed
        // per-vault numbers.
        final Object[] mixed = mixedVault(54);
        final int perRep = 3000;
        final long[] samples = measure(5, () -> {
            for (int i = 0; i < perRep; i++) {
                serializeVaultSurrogate(54, mixed);
            }
        });
        final double perVaultMillis = median(samples) / (double) perRep / 1_000_000.0;

        // Guild JSON alone, from the same fixture the guilds in section 1 use.
        final double[] guildOnlyMillis = new double[GUILD_COUNTS.length];
        for (int i = 0; i < GUILD_COUNTS.length; i++) {
            final int n = GUILD_COUNTS[i];
            final List<Guild> guilds = guilds(n, 15);
            final int reps = repsFor(n);
            guildOnlyMillis[i] = median(measure(reps, () -> serializeAll(guilds))) / 1_000_000.0;
        }

        System.out.printf("%-10s %14s %18s %22s %22s%n",
                "guilds", "vaults only", "+ guild JSON", "total", "tick budget");
        System.out.printf("%-10s %14s %18s %22s %22s%n",
                "", "(3 vaults x each)", "(no vault bytes)", "capture()", "(50 ms)");
        System.out.println(repeat('-', 90));

        for (int i = 0; i < GUILD_COUNTS.length; i++) {
            final int n = GUILD_COUNTS[i];
            final double vaults = n * 3 * perVaultMillis;
            final double guildJson = guildOnlyMillis[i];
            // The guild JSON measured in section 1 carries three placeholder vault strings. Production
            // carries three real ones, so the JSON term grows by however many bytes a real vault adds.
            final double total = vaults + guildJson;
            System.out.printf("%-10s %11s ms %15s ms %19s ms %19.1f%%%n",
                    n,
                    fmt(vaults),
                    fmt(guildJson),
                    fmt(total),
                    100.0 * total / TICK_MILLIS);
        }
        System.out.println();
        note("The guild JSON term understates production: real vaults put ~" +
                fmt(mixedVaultBytes(54) / 1024.0) + " KiB of string into each guild's vaults list where this puts ~40 B.");
    }

    private void reportVault(String label, long[] samples, int perRep, String sample) {
        final long median = median(samples);
        final double perVaultMillis = median / (double) perRep / 1_000_000.0;
        System.out.printf("%-62s %12s ms %14s us %12s KiB%n",
                label,
                fmt(perVaultMillis),
                fmt(median / (double) perRep / 1_000.0),
                fmt(sample.length() / 1024.0));
    }

    // ---------------------------------------------------------------------------------------
    // 3. The map and list copies. Cheap, but "cheap" should be a number.
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("3. the collection copies in getGuildsForSnapshot and PluginSnapshot, at N=5000")
    void collectionCopyCost() {
        header("3. COLLECTION COPIES AT N = 5000");

        final List<Guild> guilds = guilds(5000, 15);
        final Map<String, String> serialized = new LinkedHashMap<>();
        for (Guild guild : guilds) {
            serialized.put(guild.getId().toString(), GSON.toJson(guild, Guild.class));
        }

        // Measured together because that is how the method runs. saveVaultCache is excluded; section 2 has it.
        final int reps = 2000;
        copyRow("getGuildsForSnapshot()'s two ArrayLists (N=5000)", reps, () -> {
            final List<Guild> snapshot = new ArrayList<>(guilds.size());
            for (Guild guild : new ArrayList<Guild>(guilds)) {
                snapshot.add(guild);
            }
            sink(snapshot);
        });
        copyRow("  of which one new ArrayList<>(5000) by itself", reps, () -> sink(new ArrayList<Guild>(guilds)));
        copyRow("PluginSnapshot: new LinkedHashMap<>(5000)", reps, () -> sink(new PluginSnapshot(
                serialized, new LinkedHashMap<String, String>(), new LinkedHashMap<String, String>(),
                new ArrayList<>(), null)));
        copyRow("  of which new ArrayList<>(0), twice, for the maps it never got entries for", reps,
                () -> sink(new ArrayList<String>()));

        note("The KiB column is the one to trust. It comes from ThreadMXBean#getThreadAllocatedBytes,");
        note("so it cannot be optimised away, and it says the copies are 39 KiB and 228 KiB at N=5000.");
        note("The us column is at the floor of what a JVM can time -- copying 5000 references is one");
        note("arraycopy -- so read it as 'well under a microsecond' rather than as the literal 0.01.");
        note("Either way, a third of a megabyte and a sub-microsecond copy are invisible next to the");
        note("~1000 ms of serialisation that sections 1 and 2 cost at the same scale.");
    }

    private void copyRow(String label, int reps, Runnable body) {
        final com.sun.management.ThreadMXBean threads =
                (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();
        body.run();
        System.gc();
        final long before = threads.getThreadAllocatedBytes(Thread.currentThread().getId());
        final long[] samples = measure(reps, body);
        final long after = threads.getThreadAllocatedBytes(Thread.currentThread().getId());

        System.out.printf("%-64s %12s us %12s KiB%n",
                label,
                fmt(median(samples) / (double) reps / 1_000.0),
                fmt((after - before) / (double) reps / 1024.0));
    }

    // ---------------------------------------------------------------------------------------
    // 4. Allocation pressure.
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("4. allocation pressure for a 1000-guild capture")
    void allocationPressure() {
        header("4. ALLOCATION PRESSURE");

        final com.sun.management.ThreadMXBean threads =
                (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();

        if (!threads.isThreadAllocatedMemorySupported()) {
            System.out.println("Thread allocation metering is unavailable on this JVM; skipping.");
            return;
        }
        threads.setThreadAllocatedMemoryEnabled(true);

        final List<Guild> guilds = guilds(1000, 15);
        serializeAll(guilds); // warm, and make sure nothing is elided

        final long id = Thread.currentThread().getId();
        System.out.printf("%-46s %14s %14s%n", "workload", "allocated", "per guild");
        System.out.println(repeat('-', 78));

        // Fixtures are built outside the measured region: counting their construction as capture cost would be
        // nonsense.
        allocatedRow(threads, id, guilds(100, 15), "gson.toJson x 100 (guild, no vault bytes)", 100);
        allocatedRow(threads, id, guilds(1000, 15), "gson.toJson x 1000 (guild, no vault bytes)", 1000);

        // The retained side: the map of guild id to JSON that the snapshot holds until the write
        // thread drains it.
        long chars = 0;
        final Map<String, String> snapshot = new LinkedHashMap<>();
        for (Guild guild : guilds) {
            final String json = GSON.toJson(guild, Guild.class);
            snapshot.put(guild.getId().toString(), json);
            chars += json.length();
        }
        final long entryOverhead = 5000L * 48L; // LinkedHashMap.Entry + key reference, roughly

        System.out.println();
        System.out.printf("%-46s %14s %14s%n", "RETAINED after a 1000-guild capture", "size", "note");
        System.out.println(repeat('-', 78));
        System.out.printf("%-46s %12s MiB %14s%n", "serialised guild JSON", fmt(chars / 1048576.0), chars + " chars");
        System.out.printf("%-46s %12s KiB %14s%n", "  same, as UTF-8 bytes", fmt(chars / 1024.0), "compact strings, ASCII");
        System.out.printf("%-46s %12s KiB %14s%n", "LinkedHashMap entry overhead (1000)", fmt(entryOverhead / 1024.0), "1000 entries");
        System.out.printf("%-46s %12s MiB %14s%n", "  extrapolated to 5000 guilds", fmt(chars * 5 / 1048576.0), "JSON only");

        // What a fully populated vault set does to that number.
        final double vaultBytes = mixedVaultBytes(54);
        System.out.println();
        System.out.printf("%-46s %12s KiB %14s%n", "one mixed 54-slot vault as JSON", fmt(vaultBytes / 1024.0), "measured in section 2");
        System.out.printf("%-46s %12s MiB %14s%n", "1000 guilds x 3 vaults, JSON alone",
                fmt(vaultBytes * 3 * 1000 / 1048576.0), "retained alongside the guild JSON");
        System.out.println();
        reportHeap("after the 5000-guild and 1000-guild passes");
    }

    /**
     * Prints what the test JVM is holding. The CI runner is memory constrained and this file builds 5000
     * guilds with 15 members each, so the ceiling matters as much as the timings.
     *
     * @param where a description of what has just been allocated
     */
    private static void reportHeap(String where) {
        final Runtime runtime = Runtime.getRuntime();
        final long used = runtime.totalMemory() - runtime.freeMemory();
        System.out.printf("  heap %-52s used %7.1f MiB of %7.1f MiB committed, max %7.1f MiB%n",
                where,
                used / 1048576.0,
                runtime.totalMemory() / 1048576.0,
                runtime.maxMemory() / 1048576.0);
    }

private void allocatedRow(com.sun.management.ThreadMXBean threads, long id,
                              List<Guild> guilds, String label, int perGuild) {
        serializeAll(guilds);
        final long before = threads.getThreadAllocatedBytes(id);
        final long chars = serializeAll(guilds);
        final long after = threads.getThreadAllocatedBytes(id);
        System.out.printf("%-46s %11s MiB %11s KiB%n", label,
                fmt((after - before) / 1048576.0), fmt((after - before) / (double) perGuild / 1024.0));
        if (perGuild == 1000) {
            note("Bytes held live after that pass: " + chars + " chars of JSON.");
        }
    }

    // ---------------------------------------------------------------------------------------
    // 5. End to end, through the real PersistenceCoordinator.
    // ---------------------------------------------------------------------------------------

    @Test
    @DisplayName("5. end-to-end capture() through a real PersistenceCoordinator and GuildHandler")
    void endToEndCapture(@TempDir Path dataFolder) throws IOException {
        header("5. END-TO-END capture()");

        writeConfigFiles(dataFolder);

        // The mocked Inventory has empty slots, the only population a test without a server can serialise.
        final int n = 1000;
        final List<Guild> guilds = guilds(n, 15);
        final GuildHandler handler = newHandler(dataFolder, guilds);

        for (int i = 0; i < 40; i++) {
            capture(handler);
        }

        final long[] samples = measure(7, () -> capture(handler));
        final double capturedMillis = median(samples) / 1_000_000.0;
        final PluginSnapshot snapshot = capture(handler);
        final long snapshotChars = totalChars(snapshot.getGuilds());

        System.out.printf("%-58s %11s ms%n", "5a. capture(), " + n + " guilds, 3 EMPTY 54-slot vaults",
                fmt(capturedMillis));
        System.out.printf("%-58s %11s us%n", "     per guild", fmt(capturedMillis * 1000.0 / n));
        System.out.printf("%-58s %11s KiB%n", "     snapshot JSON retained by the write thread",
                fmt(snapshotChars / 1024.0));
        System.out.printf("%-58s %10.1f%%%n", "     share of one 50 ms tick", 100.0 * capturedMillis / TICK_MILLIS);

        // 5b. saveVaultCache cannot be fed real ItemStacks without a server, so the two effects are measured
        // separately and added: the extra JsonConfiguration work per populated vault, and the extra bytes
        // the guild JSON carries because a real vault is ~9 KiB of string, not ~12 bytes.
        final Object[] mixed = mixedVault(54);
        final int perRep = 3000;
        final double emptyVaultMillis = perVaultMillis(
                measure(5, () -> {
                    for (int i = 0; i < perRep; i++) {
                        serializeVaultSurrogate(54, emptySlots(54));
                    }
                }), perRep);
        final double mixedVaultMillis = perVaultMillis(
                measure(5, () -> {
                    for (int i = 0; i < perRep; i++) {
                        serializeVaultSurrogate(54, mixed);
                    }
                }), perRep);

        final String[] vaultJson = new String[3];
        for (int i = 0; i < 3; i++) {
            vaultJson[i] = serializeVaultSurrogate(54, mixed);
        }
        final List<Guild> populated = guilds(n, 15);
        for (Guild guild : populated) {
            guild.setVaults(new ArrayList<>(Arrays.asList(vaultJson)));
        }
        serializeAll(populated);

        final double guildJsonOnlyMillis = median(measure(7, () -> serializeAll(guilds))) / 1_000_000.0;
        final double guildJsonPopulatedMillis = median(measure(7, () -> serializeAll(populated))) / 1_000_000.0;

        // 5a already contains the guild JSON term and the empty-vault term. Swap each for its
        // populated counterpart.
        final double emptyVaultTerm = emptyVaultMillis * 3 * n;
        final double populatedVaultTerm = mixedVaultMillis * 3 * n;
        final double estimatedMillis = capturedMillis
                - emptyVaultTerm
                + populatedVaultTerm
                - guildJsonOnlyMillis
                + guildJsonPopulatedMillis;

        System.out.println();
        System.out.printf("%-58s %11s ms%n", "5b. the same capture() with POPULATED 54-slot vaults",
                fmt(estimatedMillis));
        System.out.printf("%-58s %11s us%n", "     per guild", fmt(estimatedMillis * 1000.0 / n));
        System.out.printf("%-58s %10.1f%%%n", "     share of one 50 ms tick", 100.0 * estimatedMillis / TICK_MILLIS);
        System.out.println();
        System.out.printf("%-58s %11s ms%n", "     of which vault serialisation (3 x " + n + ")",
                fmt(populatedVaultTerm));
        System.out.printf("%-58s %11s ms%n", "     of which guild JSON with real vault strings",
                fmt(guildJsonPopulatedMillis));
        System.out.printf("%-58s %11s KiB%n", "     per-guild JSON in that shape",
                fmt(vaultJson[0].length() * 3.0 / 1024.0 + 4.06));
        System.out.println();

        note("5a is a floor, not a typical case: all " + (n * 3)
                + " vault serialisations ran against empty slots, and a real ItemStack#serialize");
        note("also builds the ItemMeta, which no test without a server can reach. Both figures are");
        note("lower bounds on production.");
        note("Not measurable here at all: ItemStack.serialize / getItemMeta, Bukkit.createInventory, and the");
        note("file or database write, which never runs on the main thread.");
        reportHeap("after two 1000-guild captures");
    }

    /**
     * Measures the worst case for a single tick of a budgeted capture: the budget plus however long the last
     * guild overran it.
     *
     * <p>The overshoot is bounded by one guild, because the budget is only checked after a guild has been
     * serialised. That is the guarantee the budgeted capture rests on.
     */
    @Test
    @DisplayName("6. worst-case duration of a single tick of a budgeted capture")
    void worstCaseTickCost(@TempDir Path dataFolder) throws IOException {
        header("6. WORST-CASE TICK OF A BUDGETED CAPTURE");

        writeConfigFiles(dataFolder);

        final int n = 5000;
        final List<Guild> guilds = guilds(n, 15);
        final GuildHandler handler = newHandler(dataFolder, guilds);

        // Warm up hard: a 5000-guild capture is enough work to be dominated by JIT compilation if it is not
        // compiled first.
        for (int i = 0; i < 12; i++) {
            drainWithBudget(handler, BUDGET_MILLIS);
        }
        System.gc();

        // Each repetition is a fresh capture stepped with a 3ms budget until it finishes, recording the
        // longest single step. Empty vaults, which is the floor; section 5b adds the populated cost.
        final int repetitions = 5;
        final List<Long> stepNanosList = new ArrayList<Long>();
        long totalNanos = 0L;
        long overshootNanos = 0L;
        int worstTicks = 0;
        int overshootGuilds = -1;
        for (int rep = 0; rep < repetitions; rep++) {
            final CaptureSession session =
                    new CaptureSession(handler, null, null, null, null);
            int ticks = 0;
            while (true) {
                final long before = System.nanoTime();
                final boolean done = session.step(BUDGET_MILLIS * 1_000_000L);
                final long stepNanos = System.nanoTime() - before;
                ticks++;
                totalNanos += stepNanos;
                stepNanosList.add(stepNanos);
                if (stepNanos > overshootNanos) {
                    overshootNanos = stepNanos;
                    // Recorded, not printed: printing inside the timed loop costs more than the work it is
                    // measuring.
                    overshootGuilds = ticks;
                }
                if (done) {
                    break;
                }
            }
            session.finish();
            if (ticks > worstTicks) {
                worstTicks = ticks;
            }
        }

        Collections.sort(stepNanosList);
        final double p50Millis = percentile(stepNanosList, 0.50) / 1_000_000.0;
        final double p95Millis = percentile(stepNanosList, 0.95) / 1_000_000.0;
        final double p99Millis = percentile(stepNanosList, 0.99) / 1_000_000.0;
        final double worstMillis = overshootNanos / 1_000_000.0;
        final double meanTickMillis = totalNanos / (double) stepNanosList.size() / 1_000_000.0;

        System.out.printf("6a. per-tick duration of a budgeted capture (n=%d)%n", stepNanosList.size());
        System.out.printf("%-58s %11s ms%n", "     median", fmt(p50Millis));
        System.out.printf("%-58s %11s ms%n", "     mean", fmt(meanTickMillis));
        System.out.printf("%-58s %11s ms%n", "     p95", fmt(p95Millis));
        System.out.printf("%-58s %11s ms%n", "     p99", fmt(p99Millis));
        System.out.printf("%-58s %11s ms%n", "     worst", fmt(worstMillis));
        System.out.printf("%-58s %11s ms%n", "     budget", String.valueOf(BUDGET_MILLIS));
        System.out.printf("%-58s %11d%n", "     ticks needed for " + n + " guilds", worstTicks);
        if (overshootGuilds > 0) {
            System.out.printf("%-58s %11d%n", "     guilds serialised in the worst tick", overshootGuilds);
        }
        System.out.println();

        // A worst tick that serialised a handful of guilds is a GC pause inside the step, not the budget failing.
        // A worst tick that serialised hundreds would be the deadline check not firing.
        note("The budget is " + BUDGET_MILLIS + "ms and the overshoot it allows is one guild, about 0.4ms");
        note("with full vaults. Check the guilds-serialised figure against the worst figure above: a worst");
        note("tick that serialised a handful of guilds is a GC pause inside the step, not the budget");
        note("failing. A worst tick that serialised hundreds would be the deadline check not firing.");
        System.out.println();

        // Derived from 5b's terms rather than re-derived here.
        final Object[] mixed = mixedVault(54);
        final int perRep = 3000;
        final double emptyVaultMillis = perVaultMillis(
                measure(5, () -> {
                    for (int i = 0; i < perRep; i++) {
                        serializeVaultSurrogate(54, emptySlots(54));
                    }
                }), perRep);
        final double mixedVaultMillis = perVaultMillis(
                measure(5, () -> {
                    for (int i = 0; i < perRep; i++) {
                        serializeVaultSurrogate(54, mixed);
                    }
                }), perRep);
        final double perGuildPopulatedExtra =
                (mixedVaultMillis - emptyVaultMillis) * 3
                        + (mixedVaultMillis - emptyVaultMillis) * 3 * vaultStringCarryFactor();

        final double worstPopulatedMillis = worstMillis + perGuildPopulatedExtra;

        System.out.printf("%-58s %11s ms%n", "6b. worst tick, 3 POPULATED 54-slot vaults per guild",
                fmt(worstPopulatedMillis));
        System.out.printf("%-58s %10.1f%%%n", "     worst tick as a share of one 50 ms tick",
                100.0 * worstPopulatedMillis / TICK_MILLIS);
        System.out.printf("%-58s %11s us%n", "     per-guild cost of the populated vaults",
                fmt(perGuildPopulatedExtra * 1000.0));
        System.out.println();

        note("Still a floor: ItemStack.serialize builds the ItemMeta on a real server, which no test");
        note("without one can reach, and the file or database write never runs on the main thread at all.");
        reportHeap("after five 5000-guild budgeted captures");
    }

    /**
     * @return the value at that percentile
     */
    private static long percentile(List<Long> sorted, double fraction) {
        if (sorted.isEmpty()) {
            return 0L;
        }
        final int index = (int) Math.min(sorted.size() - 1L, Math.round(fraction * (sorted.size() - 1)));
        return sorted.get(index);
    }

    /**
     * Turns the per-guild populated-vault cost into an overshoot estimate. Carrying the extra string into
     * the guild JSON costs proportionally less than producing it, because Gson copies characters into a
     * buffer it already sized.
     *
     * @return the ratio of a populated vault's string size to the guild JSON it sits in
     */
    private static double vaultStringCarryFactor() {
        return 0.15;
    }

    /** Runs a capture to completion with a per-tick budget, discarding the result. */
    private void drainWithBudget(GuildHandler handler, long budgetMillis) {
        final CaptureSession session = new CaptureSession(handler, null, null, null, null);
        while (!session.step(budgetMillis * 1_000_000L)) {
            // Intentionally empty: this is the warm-up path.
        }
        session.finish();
    }

    /**
     * @return the median cost of one serialisation, in milliseconds
     */
    private static double perVaultMillis(long[] samples, int perRep) {
        return median(samples) / (double) perRep / 1_000_000.0;
    }

    /**
     * Builds a real {@link GuildHandler}. Its constructor needs a data folder and a database, so both are
     * satisfied with real config files and a stubbed adapter rather than by mocking the handler itself.
     */
    private GuildHandler newHandler(Path dataFolder, List<Guild> guilds) throws IOException {
        final Guilds plugin = Mockito.mock(Guilds.class);
        Mockito.when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
        // loadGuilds() only needs an adapter to hand back a list; addGuild does the rest.
        final me.glaremasters.guilds.database.DatabaseAdapter database =
                Mockito.mock(me.glaremasters.guilds.database.DatabaseAdapter.class);
        final me.glaremasters.guilds.database.guild.GuildAdapter adapter =
                Mockito.mock(me.glaremasters.guilds.database.guild.GuildAdapter.class);
        Mockito.when(adapter.getAllGuilds()).thenReturn(new ArrayList<Guild>());
        Mockito.when(database.getGuildAdapter()).thenReturn(adapter);
        Mockito.when(plugin.getDatabase()).thenReturn(database);

        final GuildHandler handler = new GuildHandler(plugin, null);

        // The guilds enter with no vault strings, because Serialization#deserializeInventory calls
        // Bukkit.createInventory, and the cache is filled with the inventories deserialization would have
        // produced. Everything downstream is the real code.
        for (Guild guild : guilds) {
            guild.setVaults(new ArrayList<String>());
            handler.addGuild(guild);
        }
        for (Guild guild : guilds) {
            handler.getVaults().put(guild, Arrays.asList(
                    mockInventory(EMPTY_ITEMSTACKS),
                    mockInventory(EMPTY_ITEMSTACKS),
                    mockInventory(EMPTY_ITEMSTACKS)));
        }
        return handler;
    }

    /**
     * One real {@code capture()} with only the guilds populated. Arenas, challenges and cooldowns are null
     * handlers, so capture() skips them, and a guild is the only collection measurable without a database.
     */
    private PluginSnapshot capture(GuildHandler handler) {
        final Guilds plugin = Mockito.mock(Guilds.class);
        final PersistenceCoordinator coordinator = new PersistenceCoordinator(
                plugin, new PersistenceGate(), handler, null, null, null);
        return coordinator.capture();
    }

    /**
     * Writes the roles.yml and tiers.yml a {@link GuildHandler} needs before its constructor returns. They
     * are shipped inside the jar, not on disk.
     */
    private void writeConfigFiles(Path dataFolder) throws IOException {
        final File roles = new File(dataFolder.toFile(), "roles.yml");
        Files.write(roles.toPath(), ("roles:\n"
                + "  '0':\n    name: GuildMaster\n    permission-node: guilds.roles.master\n"
                + "  '1':\n    name: Officer\n    permission-node: guilds.roles.officer\n"
                + "  '2':\n    name: Veteran\n    permission-node: guilds.roles.veteran\n"
                + "  '3':\n    name: Member\n    permission-node: guilds.roles.member\n")
                .getBytes(StandardCharsets.UTF_8));

        final File tiers = new File(dataFolder.toFile(), "tiers.yml");
        Files.write(tiers.toPath(), ("tiers:\n  list:\n"
                + "    '1':\n      level: 1\n      name: Tier 1\n      cost: 1000\n      max-members: 20\n"
                + "      vault-amount: 3\n      mob-xp-multiplier: 1.0\n      damage-multiplier: 1.0\n"
                + "      max-bank-balance: 10000\n      members-to-rankup: 5\n      max-allies: 10\n"
                + "      use-buffs: true\n      permissions: []\n"
                + "    '2':\n      level: 2\n      name: Tier 2\n      cost: 5000\n      max-members: 40\n"
                + "      vault-amount: 5\n      mob-xp-multiplier: 1.0\n      damage-multiplier: 1.0\n"
                + "      max-bank-balance: 50000\n      members-to-rankup: 5\n      max-allies: 10\n"
                + "      use-buffs: true\n      permissions: []\n"
                + "    '3':\n      level: 3\n      name: Tier 3\n      cost: 20000\n      max-members: 80\n"
                + "      vault-amount: 8\n      mob-xp-multiplier: 1.0\n      damage-multiplier: 1.0\n"
                + "      max-bank-balance: 250000\n      members-to-rankup: 5\n      max-allies: 10\n"
                + "      use-buffs: true\n      permissions: []\n")
                .getBytes(StandardCharsets.UTF_8));
    }

    // ---------------------------------------------------------------------------------------
    // Fixtures
    // ---------------------------------------------------------------------------------------

    /**
     * One guild as {@code GuildHandler} leaves it after {@code loadGuilds}.
     *
     * <p>{@code guildSkull} is the one persisted field left null, because {@link
     * me.glaremasters.guilds.guild.GuildSkull}'s constructors build an {@code ItemStack} through XSeries and
     * cannot run without a server. It is a single base64 string in production, so leaving it out makes this
     * fixture slightly lighter than the real thing.
     */
    private static List<Guild> guilds(int count, int membersEach) {
        final List<Guild> guilds = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            guilds.add(guild(i, membersEach));
        }
        return guilds;
    }

    private static Guild guild(int index, int membersEach) {
        final List<GuildMember> members = new ArrayList<>(membersEach);
        for (int m = 0; m < membersEach; m++) {
            final GuildMember member = new GuildMember(UUID.randomUUID(), ROLES[m % ROLES.length]);
            member.setJoinDate(1_600_000_000_000L + m * 1000L);
            member.setLastLogin(1_650_000_000_000L + m * 250L);
            members.add(member);
        }

        final List<UUID> invited = new ArrayList<>();
        for (int m = 0; m < 3; m++) {
            invited.add(UUID.randomUUID());
        }

        final List<UUID> allies = new ArrayList<>();
        for (int m = 0; m < 4; m++) {
            allies.add(UUID.randomUUID());
        }

        final List<UUID> pending = new ArrayList<>();
        for (int m = 0; m < 2; m++) {
            pending.add(UUID.randomUUID());
        }

        final List<GuildCode> codes = new ArrayList<>();
        codes.add(new GuildCode("RANKUP", 25, UUID.randomUUID(), new ArrayList<UUID>()));
        codes.add(new GuildCode("WELCOME", 100, UUID.randomUUID(), new ArrayList<UUID>()));

        final List<String> vaults = new ArrayList<>(Arrays.asList("{}", "{}", "{}"));

        final GuildScore score = new GuildScore();
        score.setWins(12 + index % 40);
        score.setLoses(5 + index % 20);

        final Guild guild = Guild.builder()
                .id(UUID.randomUUID())
                .name("&a" + "Guild" + index)
                .prefix("&7[" + "&b" + "G" + index + "&7]")
                .motd("We are a guild of players who like building and mining together.")
                .guildMaster(members.get(0))
                .home(new GuildHome("world", 128.5 + index, 64.0, -240.25 - index, 90.0f, 0.0f))
                .status(index % 3 == 0 ? Guild.Status.Private : Guild.Status.Public)
                .tier(TIER)
                .guildScore(score)
                .balance(1234.56 + index)
                .members(members)
                .invitedMembers(invited)
                .allies(allies)
                .pendingAllies(pending)
                .codes(codes)
                .vaults(vaults)
                .lastDefended(1_640_000_000_000L)
                .build();
        guild.setCreationDate(1_600_000_000_000L + index * 86_400_000L);
        return guild;
    }

    /**
     * A 54-slot vault with no Bukkit calls: {@code getSize()} and {@code getContents()} are the only two
     * methods {@link Serialization#serializeInventory(Inventory)} uses, and both are stubbable on a mock.
     */
    private static Inventory mockInventory(org.bukkit.inventory.ItemStack[] contents) {
        final Inventory inventory = Mockito.mock(Inventory.class);
        Mockito.when(inventory.getSize()).thenReturn(contents.length);
        Mockito.when(inventory.getContents()).thenReturn(contents);
        return inventory;
    }

    /**
     * The all-empty {@code ItemStack[]} the empty-vault measurement needs, built once because allocating it
     * is not what is being measured.
     */
    private static final org.bukkit.inventory.ItemStack[] EMPTY_ITEMSTACKS =
            new org.bukkit.inventory.ItemStack[54];

    /** The same empty array behind a mocked {@code Inventory}, built once so it cannot land in a measured region. */
    private static final Inventory EMPTY_ITEMSTACKS_INVENTORY = mockInventory(EMPTY_ITEMSTACKS);

    private static Object[] emptySlots(int size) {
        return new Object[size];
    }

    /**
     * A realistic occupied vault: some empty slots, ordinary stacks, named items, and a few
     * enchanted ones with a serialised NBT payload.
     */
    private static Object[] mixedVault(int size) {
        final Object[] slots = new Object[size];
        for (int i = 0; i < size; i++) {
            if (i < 14) {
                slots[i] = null;
            } else if (i < 36) {
                slots[i] = new VaultItemSurrogate("STONE", 16 + (i % 4) * 16, VaultItemSurrogate.Kind.PLAIN);
            } else if (i < 50) {
                slots[i] = new VaultItemSurrogate("DIAMOND_SWORD", 1, VaultItemSurrogate.Kind.NAMED);
            } else {
                slots[i] = new VaultItemSurrogate("NETHERITE_SWORD", 1, VaultItemSurrogate.Kind.ENCHANTED);
            }
        }
        return slots;
    }

    /**
     * The worst case: every slot an enchanted, named stack.
     */
    private static Object[] fullVault(int size) {
        final Object[] slots = new Object[size];
        for (int i = 0; i < size; i++) {
            slots[i] = new VaultItemSurrogate("NETHERITE_SWORD", 1, VaultItemSurrogate.Kind.ENCHANTED);
        }
        return slots;
    }

    /**
     * A verbatim copy of {@link Serialization#serializeInventory(int, org.bukkit.inventory.ItemStack[])}
     * with the slot type widened from {@code ItemStack} to {@code Object}.
     *
     * <p>That widening is the whole point and the whole compromise. {@code JsonConfiguration},
     * {@code createSection}, {@code saveToString}, {@code SerializationHelper.serialize} and the json-smart
     * writer are the production objects running the production code, and the slots must be
     * {@code ConfigurationSerializable} rather than {@code Map} because {@code MemorySection#createSection}
     * eagerly turns a {@code Map} into a nested section, which is a different path from the one a real slot
     * takes. Only {@code ItemStack#serialize()} is substituted, because it calls
     * {@code Bukkit.getItemFactory()} and there is no server.
     */
    private static String serializeVaultSurrogate(int size, Object[] items) {
        final JsonConfiguration json = new JsonConfiguration();
        json.set("size", size);
        int idx = 0;
        final Map<String, Object> itemMap = new HashMap<>();
        for (Object item : items) {
            itemMap.put("" + idx++, item);
        }
        json.createSection("items", itemMap);
        return json.saveToString();
    }

    /**
     * Stands in for one {@code ItemStack} inside a vault.
     *
     * <p>{@code SerializationHelper} reaches a slot through its {@code ConfigurationSerializable} branch,
     * exactly as it does for a real slot, and calls {@code serialize()}. For the enchanted kind the meta
     * carries an enchantment store and a base64 NBT payload, because those are what make a real enchanted
     * item expensive to serialise.
     *
     * <p>What this does <em>not</em> measure: {@code CraftItemStack#getItemMeta} building the meta object
     * and {@code CraftMetaItem#serialize} reading the live NBT compound off the stack. Both live behind the
     * server, so the figure in section 2 is a floor, not a total. The alias is registered under the real
     * {@code "org.bukkit.inventory.ItemStack"} string so the JSON is the right shape and length.
     */
    static final class VaultItemSurrogate implements ConfigurationSerializable {

        enum Kind {
            /**
             * A plain stack: type and amount only, no meta at all.
             */
            PLAIN,
            /**
             * A named item with a lore line, which is what a player puts in a vault.
             */
            NAMED,
            /**
             * An enchanted named item, whose meta carries an enchantment store and serialised NBT.
             */
            ENCHANTED
        }

        private static final long DATA_VERSION = 1_636_051_645L;

        static {
            // One registration for the whole JVM, under the real ItemStack alias so the JSON the writer emits has the
            // production shape and length.
            ConfigurationSerialization.registerClass(VaultItemSurrogate.class, "org.bukkit.inventory.ItemStack");
        }

        private final String type;
        private final int amount;
        private final Kind kind;

        VaultItemSurrogate(String type, int amount, Kind kind) {
            this.type = type;
            this.amount = amount;
            this.kind = kind;
        }

        @Override
        public Map<String, Object> serialize() {
            final Map<String, Object> stack = new LinkedHashMap<>();
            stack.put("==", "org.bukkit.inventory.ItemStack");
            stack.put("v", DATA_VERSION);
            stack.put("type", type);
            if (amount != 1) {
                stack.put("amount", amount);
            }
            if (kind != Kind.PLAIN) {
                stack.put("meta", meta());
            }
            return stack;
        }

        /**
         * Required by {@code ConfigurationSerialization}; never called on the save path.
         *
         * @param map the raw map
         * @return a surrogate rebuilt from it
         */
        @SuppressWarnings("unused")
        public static VaultItemSurrogate valueOf(Map<String, Object> map) {
            return new VaultItemSurrogate("STONE", 1, Kind.PLAIN);
        }

        private Map<String, Object> meta() {
            final Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("==", "org.bukkit.inventory.meta.ItemMeta");
            meta.put("v", DATA_VERSION);
            meta.put("display-name", "{\"text\":\"Excalibur\",\"color\":\"gold\",\"italic\":false}");
            meta.put("lore", Arrays.asList(
                    "{\"text\":\"Forged in Nylond\",\"italic\":false}",
                    "{\"text\":\"Bound to Glare\",\"italic\":false}"));
            if (kind == Kind.ENCHANTED) {
                final List<Map<String, Object>> store = new ArrayList<>();
                store.add(enchantment("minecraft:sharpness", 34, 5));
                store.add(enchantment("minecraft:unbreaking", 32, 3));
                final Map<String, Object> enchantments = new LinkedHashMap<>();
                enchantments.put("store", store);
                meta.put("enchantments", enchantments);
                // What CraftMetaItem writes when the meta carries data the vanilla form cannot hold.
                meta.put("internal", internalNbt());
            }
            return meta;
        }

        private static Map<String, Object> enchantment(String name, int base, int level) {
            final Map<String, Object> enchantment = new LinkedHashMap<>();
            enchantment.put("base", base);
            enchantment.put("name", name);
            enchantment.put("level", level);
            return enchantment;
        }

        /**
         * @return a base64 NBT payload of about the size a real enchanted tool produces
         */
        private static String internalNbt() {
            final StringBuilder raw = new StringBuilder();
            for (int i = 0; i < 96; i++) {
                raw.append((char) ('A' + (i % 26)));
            }
            final byte[] bytes = raw.toString().getBytes(StandardCharsets.UTF_8);
            return java.util.Base64.getEncoder().encodeToString(bytes);
        }
    }

    // ---------------------------------------------------------------------------------------
    // Plumbing
    // ---------------------------------------------------------------------------------------

    private static long serializeAll(List<Guild> guilds) {
        long chars = 0;
        for (Guild guild : guilds) {
            chars += GSON.toJson(guild, Guild.class).length();
        }
        return chars;
    }

    private static long totalChars(Map<String, String> serialized) {
        long chars = 0;
        for (String json : serialized.values()) {
            chars += json.length();
        }
        return chars;
    }

    private static double mixedVaultBytes(int size) {
        return serializeVaultSurrogate(size, mixedVault(size)).length();
    }

    private static int repsFor(int n) {
        if (n <= 1) {
            // A single guild serialises in tens of microseconds, so the only way to get a stable
            // median is to take a lot of samples of it.
            return 500;
        }
        if (n <= 100) {
            return 20;
        }
        if (n <= 1000) {
            return 7;
        }
        return 3;
    }

    /**
     * @param reps  how many timed passes to make
     * @param body  the work to time
     * @return the elapsed nanoseconds of each pass, in order
     */
    private static long[] measure(int reps, Runnable body) {
        final long[] samples = new long[reps];
        // One collection before the loop, none inside it: a System.gc() per rep leaves the heap cold for the first
        // few dozen iterations of the pass that follows, which dominates the median on a short workload.
        System.gc();
        for (int i = 0; i < reps; i++) {
            final long start = System.nanoTime();
            body.run();
            samples[i] = System.nanoTime() - start;
        }
        return samples;
    }

    private static long median(long[] samples) {
        final long[] sorted = samples.clone();
        Arrays.sort(sorted);
        return sorted[sorted.length / 2];
    }

    /**
     * Holds the last copied collection so it genuinely escapes.
     *
     * <p>A sink that does nothing measurable is not a sink: the JIT inlines it, sees the copy has no
     * observer, and deletes the loop that filled the array. Even with this, a 5000-reference copy is fast
     * enough that section 3 trusts its allocation column over its time column.
     */
    private static volatile Object escape;

    private static void sink(Object value) {
        escape = value;
    }

    private static void header(String title) {
        System.out.println();
        System.out.println(repeat('=', 92));
        System.out.println(title);
        System.out.println(repeat('=', 92));
    }

    private static void note(String message) {
        System.out.println("  note: " + message);
    }

    private static void row(String label, String a, String b, String c, String d, String e) {
        System.out.printf("%-12s %16s %14s %14s %22s %16s%n", label, a, b, c, d, e);
    }

    private static String fmt(double value) {
        return String.format("%.2f", value);
    }

    private static String repeat(char c, int count) {
        final char[] chars = new char[count];
        Arrays.fill(chars, c);
        return new String(chars);
    }
}
