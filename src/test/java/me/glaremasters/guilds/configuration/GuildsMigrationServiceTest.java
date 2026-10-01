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
package me.glaremasters.guilds.configuration;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.resource.PropertyReader;
import ch.jalu.configme.resource.YamlFileReader;
import ch.jalu.configme.resource.YamlFileResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers ConfigMe migration detection.
 *
 * <p>This is the test that keeps {@code config.yml} from being rewritten on every single startup.
 * ConfigMe's {@code SettingsManagerImpl.load} calls {@code checkAndMigrate} and, when it reports
 * that a migration happened, calls {@code save()}, which regenerates the file from the property
 * definitions. That strips admin comments and any key the plugin does not define.
 *
 * <p>{@code timers.cooldowns.sethome} was listed as a deprecated path while {@code CooldownSettings}
 * still defined it, so the reader always found it and every startup looked like a migration.
 *
 * <p>The default config is generated from the real property definitions rather than pasted in, so
 * this also fails when a newly added property accidentally lands in the deprecated list, which is
 * how the bug would come back.
 */
class GuildsMigrationServiceTest {

    @TempDir
    Path tempDir;

    private AtomicInteger fileCounter;

    @BeforeEach
    void setUp() {
        fileCounter = new AtomicInteger();
    }

    /**
     * Writes a config.yml generated from the plugin's own property definitions, which is exactly
     * what a server owner gets on a fresh install.
     *
     * @return the generated file
     * @throws IOException if the file cannot be written
     */
    private Path writeDefaultConfig() throws IOException {
        final Path empty = tempDir.resolve("empty-" + fileCounter.incrementAndGet() + ".yml");
        Files.write(empty, Arrays.asList(""));

        // Seeding from an empty file resolves every property to its declared default, which is what
        // lets exportProperties write a complete config.
        final ConfigurationData data = GuildConfigurationBuilder.buildConfigurationData();
        data.initializeValues(new YamlFileReader(empty));

        final Path config = tempDir.resolve("config-" + fileCounter.incrementAndGet() + ".yml");
        new YamlFileResource(config).exportProperties(data);
        return config;
    }

    /**
     * @param yaml the config contents
     * @return a reader over a config file with those contents
     * @throws IOException if the file cannot be written
     */
    private PropertyReader readerFor(String yaml) throws IOException {
        final Path config = tempDir.resolve("config-" + fileCounter.incrementAndGet() + ".yml");
        Files.write(config, Arrays.asList(yaml, ""));
        return new YamlFileReader(config);
    }

    private boolean needsMigration(PropertyReader reader) {
        final ConfigurationData data = GuildConfigurationBuilder.buildConfigurationData();
        return new GuildsMigrationService(tempDir.toFile()).performMigrations(reader, data);
    }

    @Test
    @DisplayName("a freshly generated default config does not trigger a migration")
    void defaultConfigDoesNotNeedMigration() throws IOException {
        assertFalse(needsMigration(new YamlFileReader(writeDefaultConfig())));
    }

    @Test
    @DisplayName("a config a server owner edited does not trigger a migration either")
    void editedConfigDoesNotNeedMigration() throws IOException {
        // An owner-added comment and key are the things a rewrite destroys, so their presence must
        // not by itself cause one.
        final Path config = writeDefaultConfig();
        final String edited = new String(Files.readAllBytes(config), "UTF-8")
                + "\n# my own note\nsome-custom-key: true\n";
        Files.write(config, edited.getBytes("UTF-8"));

        assertFalse(needsMigration(new YamlFileReader(config)));
    }

    @Test
    @DisplayName("the sethome cooldown is a live setting, not a deprecated path")
    void sethomeCooldownIsNotTreatedAsDeprecated() throws IOException {
        // Regression guard. This path used to be in the deprecated list while CooldownSettings
        // still declared it, which made every startup rewrite config.yml.
        final PropertyReader reader = readerFor("timers.cooldowns.sethome: 60");

        assertFalse(needsMigration(reader));
    }

    @Test
    @DisplayName("a genuinely deprecated path still triggers a migration")
    void deprecatedPathStillTriggersMigration() throws IOException {
        // The migration path itself has to keep working, otherwise a server owner upgrading from an
        // old version never gets their tiers moved out of config.yml.
        assertTrue(needsMigration(readerFor("settings.save-interval: 5")));
        assertTrue(needsMigration(readerFor("tablist.enabled: true")));
    }

    @Test
    @DisplayName("an inlined tiers section still triggers a migration")
    void inlineTiersStillTriggerMigration() throws IOException {
        assertTrue(needsMigration(readerFor("tiers.list.1.level: 1")));
    }
}
