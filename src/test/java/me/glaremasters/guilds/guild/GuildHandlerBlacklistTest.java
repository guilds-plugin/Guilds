package me.glaremasters.guilds.guild;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import me.glaremasters.guilds.configuration.sections.GuildSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the guild name blacklist, which {@code /guild create}, {@code /guild rename},
 * {@code /guild admin rename} and {@code /guild prefix} all run a name or prefix through.
 *
 * <p>The bug: {@code GuildHandler.blacklistCheck} asked the wrong question. It tested whether a
 * blacklisted <em>word</em> contained the proposed <em>name</em>, so the two ends were reversed.
 * With the shipped blacklist ({@code crap}, {@code ass}, {@code stupid}) that refused a guild
 * called "cr" — a substring of "crap" — and allowed a guild called "crapguild", which is what
 * the blacklist exists to refuse.
 *
 * <p>The other half was the meaning of {@code guild.blacklist.case-sensitive}. It reads as a
 * statement about case, and that is how it is treated here: on, the name has to contain a word
 * spelled exactly as configured; off, the same containment check runs ignoring case. Neither
 * branch is about equality — a name that merely <em>is</em> a blacklisted word is a special case
 * of a name that contains one, and refusing it was never the point of a blacklist.
 *
 * <p>The SettingsManager is a real ConfigMe instance reading a real YAML file, so the properties
 * exercised here are the ones the plugin reads.
 */
class GuildHandlerBlacklistTest {

    @TempDir
    Path tempDir;

    private static final AtomicInteger FILE_COUNTER = new AtomicInteger();

    private Locale originalLocale;

    private GuildHandler handler;

    @BeforeEach
    void captureLocale() {
        originalLocale = Locale.getDefault();

        // The check itself only reads the settings manager it is handed, so a handler whose real
        // methods run is enough to exercise it without a plugin, a data folder and a database.
        handler = Mockito.mock(GuildHandler.class, Mockito.CALLS_REAL_METHODS);
    }

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    /**
     * Builds a real ConfigMe settings manager holding only the blacklist properties.
     *
     * @param caseSensitive value for guild.blacklist.case-sensitive
     * @param words        values for guild.blacklist.words
     * @return a settings manager holding the blacklist configuration
     * @throws IOException if the temp config cannot be written
     */
    private SettingsManager settings(boolean caseSensitive, List<String> words) throws IOException {
        final Path file = tempDir.resolve("config-" + FILE_COUNTER.incrementAndGet() + ".yml");

        final List<String> lines = new ArrayList<>();
        lines.add("guild.blacklist.case-sensitive: " + caseSensitive);
        if (words.isEmpty()) {
            // A bare key with nothing under it reads as null, and ConfigMe falls back to the
            // shipped default list. An empty list has to be written out as one.
            lines.add("guild.blacklist.words: []");
        } else {
            lines.add("guild.blacklist.words:");
            for (String word : words) {
                lines.add("  - \"" + word + "\"");
            }
        }

        Files.write(file, lines);

        return SettingsManagerBuilder
                .withYamlFile(file)
                .configurationData(GuildSettings.class)
                .create();
    }

    private static List<String> words(String... words) {
        return Arrays.asList(words);
    }

    @Test
    @DisplayName("a name that contains a blacklisted word is refused")
    void nameContainingABlacklistedWordIsRefused() throws IOException {
        final SettingsManager settings = settings(true, words("crap", "ass", "stupid"));

        // The regression this file exists for. "crapguild" contains "crap", so it is refused.
        assertTrue(handler.blacklistCheck("crapguild", settings));
        assertTrue(handler.blacklistCheck("assassin", settings));
        assertTrue(handler.blacklistCheck("stupidity", settings));
    }

    @Test
    @DisplayName("a name that is only a substring of a blacklisted word is allowed")
    void nameThatIsOnlyASubstringIsAllowed() throws IOException {
        final SettingsManager settings = settings(true, words("crap", "ass", "stupid"));

        // "cr" is inside "crap", but "cr" does not contain "crap", so it is allowed.
        assertFalse(handler.blacklistCheck("cr", settings));
        assertFalse(handler.blacklistCheck("ap", settings));
        assertFalse(handler.blacklistCheck("stu", settings));
    }

    @Test
    @DisplayName("a name that is exactly a blacklisted word is refused")
    void nameThatIsExactlyABlacklistedWordIsRefused() throws IOException {
        final SettingsManager settings = settings(true, words("crap"));

        assertTrue(handler.blacklistCheck("crap", settings));
    }

    @Test
    @DisplayName("the case-sensitive setting compares case exactly")
    void caseSensitiveComparesCaseExactly() throws IOException {
        final SettingsManager settings = settings(true, words("crap"));

        // Case-sensitive: the name has to contain the word as it is spelled in the config.
        assertTrue(handler.blacklistCheck("crapguild", settings));
        assertFalse(handler.blacklistCheck("Crapguild", settings));
        assertFalse(handler.blacklistCheck("CRAPGUILD", settings));
    }

    @Test
    @DisplayName("the case-insensitive setting ignores case and still matches by containment")
    void caseInsensitiveIgnoresCase() throws IOException {
        final SettingsManager settings = settings(false, words("crap"));

        assertTrue(handler.blacklistCheck("crapguild", settings));
        assertTrue(handler.blacklistCheck("Crapguild", settings));
        assertTrue(handler.blacklistCheck("CRAPGUILD", settings));

        // The check does not run the other way round: "cr" is allowed because it does not contain
        // "crap", even though "crap" contains "cr".
        assertFalse(handler.blacklistCheck("cr", settings));
        assertFalse(handler.blacklistCheck("ap", settings));
    }

    @Test
    @DisplayName("the case-insensitive match does not depend on the default locale")
    void caseInsensitiveMatchIsLocaleIndependent() throws IOException {
        Locale.setDefault(new Locale("tr", "TR"));

        // "I" lowercases to a dotless "ı" under a Turkish locale, so a default-locale lowercase
        // on one side of the comparison and not the other loses the match.
        final SettingsManager settings = settings(false, words("SILLY"));

        assertTrue(handler.blacklistCheck("sillyguild", settings));
    }

    @Test
    @DisplayName("a blank word in the list refuses nothing")
    void blankWordRefusesNothing() throws IOException {
        final SettingsManager settings = settings(true, words("", "  ", "crap"));

        // A blank word is contained in every name, so taking it literally would refuse every
        // guild on the server.
        assertFalse(handler.blacklistCheck("innocent", settings));
        assertTrue(handler.blacklistCheck("craplord", settings));
    }

    @Test
    @DisplayName("an empty blacklist refuses nothing")
    void emptyBlacklistRefusesNothing() throws IOException {
        final SettingsManager settings = settings(true, words());

        assertFalse(handler.blacklistCheck("crapguild", settings));
    }
}
