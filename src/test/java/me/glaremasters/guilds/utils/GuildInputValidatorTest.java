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

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import me.glaremasters.guilds.guild.Guild;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the guild name validation shared by {@code /guild create}, {@code /guild rename}, and
 * {@code /guild admin rename}.
 *
 * <p>Two regressions are pinned here. {@code /guild admin rename} used to skip validation
 * entirely, so an admin could set a duplicate visible name; because every {@code --other} command
 * resolves its target guild by visible name, a duplicate made all of them non-deterministic. And
 * both rename paths compared the new name against the guild's own name, so a guild could never
 * keep a name it already had.
 *
 * <p>The SettingsManager is a real ConfigMe instance reading a real YAML file, so the regex and
 * colour-code handling exercised here is the same code the plugin runs.
 */
class GuildInputValidatorTest {

    /** The default guild.requirements.name the plugin ships. */
    private static final String DEFAULT_NAME_REQUIREMENTS = "[a-zA-Z0-9&]{1,64}";

    @TempDir
    Path tempDir;

    private static final AtomicInteger FILE_COUNTER = new AtomicInteger();

    private static Guild guildNamed(String name) {
        final Guild guild = new Guild(UUID.randomUUID());
        guild.setName(name);
        return guild;
    }

    /**
     * Builds a real ConfigMe settings manager from a generated config file.
     *
     * @param nameRequirements value for guild.requirements.name
     * @param includeColorCodes value for guild.requirements.include-color-codes
     * @return a settings manager holding only the properties this test needs
     * @throws IOException if the temp config cannot be written
     */
    private SettingsManager settings(String nameRequirements, boolean includeColorCodes) throws IOException {
        final Path file = tempDir.resolve("config-" + FILE_COUNTER.incrementAndGet() + ".yml");
        Files.write(file, Arrays.asList(
                "guild.requirements.name: \"" + nameRequirements + "\"",
                "guild.requirements.prefix: \"[a-zA-Z0-9&]{1,20}\"",
                "guild.requirements.include-color-codes: " + includeColorCodes,
                ""
        ));

        return SettingsManagerBuilder
                .withYamlFile(file)
                .configurationData(GuildSettings.class)
                .create();
    }

    @Test
    @DisplayName("a name already used by another guild is taken")
    void detectsDuplicateName() {
        final List<Guild> guilds = Arrays.asList(guildNamed("Raiders"), guildNamed("Union"));

        assertTrue(GuildInputValidator.isNameTaken("Raiders", guilds));
        assertTrue(GuildInputValidator.isNameTaken("Union", guilds));
        assertFalse(GuildInputValidator.isNameTaken("Rangers", guilds));
    }

    @Test
    @DisplayName("duplicate detection ignores case and colour codes")
    void duplicateDetectionIgnoresCaseAndColour() {
        // GuildHandler#getGuild(String) compares names with colours stripped, so to every command
        // that looks a guild up by name, a name that only differs by colour is the same name.
        final List<Guild> guilds = Collections.singletonList(guildNamed("§cRaiders"));

        assertTrue(GuildInputValidator.isNameTaken("Raiders", guilds));
        assertTrue(GuildInputValidator.isNameTaken("raiders", guilds));
        assertTrue(GuildInputValidator.isNameTaken("&cRaiders", guilds));
        assertTrue(GuildInputValidator.isNameTaken("&4Raiders", guilds));
        assertFalse(GuildInputValidator.isNameTaken("Rangers", guilds));
    }

    @Test
    @DisplayName("a guild does not conflict with itself")
    void aGuildDoesNotConflictWithItself() {
        final Guild guild = guildNamed("§cRaiders");
        final List<Guild> guilds = Collections.singletonList(guild);

        // Without the exclusion this is true, and renaming a guild to the name it already has is
        // rejected as a duplicate.
        assertTrue(GuildInputValidator.isNameTaken("Raiders", guilds));
        assertFalse(GuildInputValidator.isNameTaken("Raiders", guilds, guild.getId()));
        assertFalse(GuildInputValidator.isNameTaken("&cRaiders", guilds, guild.getId()));
    }

    @Test
    @DisplayName("excluding one guild still detects a clash with the others")
    void exclusionOnlySkipsTheNamedGuild() {
        final Guild first = guildNamed("Raiders");
        final Guild second = guildNamed("Union");
        final List<Guild> guilds = Arrays.asList(first, second);

        assertFalse(GuildInputValidator.isNameTaken("Raiders", guilds, first.getId()));
        assertTrue(GuildInputValidator.isNameTaken("Union", guilds, first.getId()));
        assertTrue(GuildInputValidator.isNameTaken("Raiders", guilds, second.getId()));
        assertTrue(GuildInputValidator.isNameTaken("Raiders", guilds, UUID.randomUUID()));
    }

    @Test
    @DisplayName("names are validated against the configured regex")
    void namesAreValidatedAgainstTheRegex() throws IOException {
        final SettingsManager settings = settings(DEFAULT_NAME_REQUIREMENTS, true);

        assertTrue(GuildInputValidator.isValidName("Raiders", settings));
        assertTrue(GuildInputValidator.isValidName("Raid3rs", settings));
        assertFalse(GuildInputValidator.isValidName("", settings));
        assertFalse(GuildInputValidator.isValidName("Raiders!", settings));
        assertFalse(GuildInputValidator.isValidName(String.join("", Collections.nCopies(65, "a")), settings));
    }

    @Test
    @DisplayName("the shipped regex allows ampersands")
    void colourCodesAllowedByDefault() throws IOException {
        assertTrue(GuildInputValidator.isValidName("&cRaiders", settings(DEFAULT_NAME_REQUIREMENTS, true)));
    }

    @Test
    @DisplayName("with colour codes excluded the visible text and the marker must both match")
    void colourCodesExcluded() throws IOException {
        // The visible part still has to satisfy the regex, and the marker character itself has to be
        // one the regex permits. &cRaiders passes because the default regex includes &.
        final SettingsManager noColours = settings(DEFAULT_NAME_REQUIREMENTS, false);

        assertTrue(GuildInputValidator.isValidName("&cRaiders", noColours));
        assertTrue(GuildInputValidator.isValidName("Raiders", noColours));

        // A regex with no & in it must reject a colour code, even though the visible text matches.
        final SettingsManager noAmpersand = settings("[a-zA-Z0-9]{1,64}", false);
        assertFalse(GuildInputValidator.isValidName("&cRaiders", noAmpersand));
        assertTrue(GuildInputValidator.isValidName("Raiders", noAmpersand));
    }
}
