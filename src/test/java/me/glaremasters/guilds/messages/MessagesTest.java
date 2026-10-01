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
package me.glaremasters.guilds.messages;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers message key generation.
 *
 * <p>{@code Messages} derives its ConfigMe key from the enum constant name in a static
 * initialiser. It used to lowercase with {@link Locale#getDefault()}, so on a Turkish-locale JVM "I"
 * became a dotless "ı" and {@code INVITE__ALREADY_INVITED} became {@code ınvıte.already-ınvıted}. No
 * such key exists in any language file, so those messages silently fell back to printing the raw
 * key. The plugin ships a {@code tr-TR.yml}, so Turkish servers are a supported audience.
 *
 * <p>The key is computed once per class load, so the default locale has to be in place before the
 * enum is first touched. That is what {@link BeforeEach} arranges, and it is why the two ordering
 * sensitive tests live in this class rather than being spread around.
 *
 * <p>{@link #everyKeyIsDefinedInEnUs()} is the one that keeps the enum honest: it cross-checks
 * every generated key against the default language file, so a new constant whose key nobody wrote
 * fails here rather than in front of a player.
 */
class MessagesTest {

    private static final String EN_US = "/languages/en-US.yml";

    private Locale originalLocale;

    @BeforeEach
    void captureLocale() {
        originalLocale = Locale.getDefault();
    }

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    @Test
    @DisplayName("keys are generated with a locale-independent lowercase")
    void keysAreLocaleIndependent() {
        Locale.setDefault(new Locale("tr", "TR"));

        // Touching the enum is what forces class initialisation, so this line is the test: the keys
        // are built under a Turkish default locale and have to come out the same regardless.
        assertEquals("invite.already-invited", Messages.INVITE__ALREADY_INVITED.getMessageKey().getKey());
        assertEquals("error.already-in-guild", Messages.ERROR__ALREADY_IN_GUILD.getMessageKey().getKey());
        assertEquals("create.guild-name-taken", Messages.CREATE__GUILD_NAME_TAKEN.getMessageKey().getKey());
        assertEquals("syntax.position", Messages.SYNTAX__POSITION.getMessageKey().getKey());
    }

    @Test
    @DisplayName("keys use the same shape with or without colour sections")
    void keyShape() {
        assertEquals("war.no-pending-challenge", Messages.WAR__NO_PENDING_CHALLENGE.getMessageKey().getKey());
        assertEquals("confirm.success", Messages.CONFIRM__SUCCESS.getMessageKey().getKey());
    }

    @Test
    @DisplayName("every message key is defined in en-US.yml")
    void everyKeyIsDefinedInEnUs() throws IOException {
        final Set<String> defined = readLanguageFile(EN_US);
        final List<String> missing = new ArrayList<>();

        for (Messages message : Messages.values()) {
            final String key = message.getMessageKey().getKey();
            assertNotNull(key, () -> message.name() + " produced a null key");
            if (!defined.contains(key)) {
                missing.add(message.name() + " -> " + key);
            }
        }

        assertTrue(missing.isEmpty(), () -> "Message keys missing from " + EN_US + ": " + missing);
    }

    /**
     * Flattens a language file into the set of dotted keys ConfigMe would resolve.
     *
     * @param resource classpath location of the yaml file
     * @return every fully qualified key
     * @throws IOException if the resource cannot be read
     */
    @SuppressWarnings("unchecked")
    private static Set<String> readLanguageFile(String resource) throws IOException {
        final Set<String> keys = new LinkedHashSet<>();

        try (InputStream in = MessagesTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, "Missing resource " + resource);

            final Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            final Map<String, Object> root = new Yaml().loadAs(reader, Map.class);
            collectKeys("", root, keys);
        }

        return keys;
    }

    @SuppressWarnings("unchecked")
    private static void collectKeys(String prefix, Map<String, Object> node, Set<String> keys) {
        for (Map.Entry<String, Object> entry : node.entrySet()) {
            final String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();

            if (entry.getValue() instanceof Map) {
                collectKeys(key, (Map<String, Object>) entry.getValue(), keys);
            } else {
                keys.add(key);
            }
        }
    }
}
