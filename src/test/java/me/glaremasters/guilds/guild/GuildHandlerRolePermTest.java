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

import ch.jalu.configme.properties.Property;
import me.glaremasters.guilds.configuration.sections.RoleSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers role permission key derivation in {@code roles.yml}.
 *
 * <p>{@link GuildHandler#loadRoles()} reads one boolean per {@link GuildRolePerm} out of
 * {@code roles.yml} by turning the enum constant name into a key. That lowercase used the default
 * locale, so on a Turkish-locale JVM {@code "I"} became a dotless {@code "ı"} and
 * {@code INITIATE_WAR} was read from {@code roles.0.permissions.ınıtate-war}. No such key exists, so
 * the lookup quietly returned false and no role ever gained the permission. The visible effect was
 * that {@code /guild war accept}, {@code /guild war challenge} and {@code /guild war deny} rejected
 * every player, and the defender list came back empty. The plugin ships a {@code tr-TR.yml}, so
 * Turkish servers are a supported audience.
 *
 * <p>{@code Messages} and {@code DatabaseBackend} were fixed for this bug class earlier, each
 * carrying a comment explaining why. This test guards the third derivation. It is not the last
 * place in the tree that lowercases a value which has to match a fixed literal:
 * {@code GuildHandler.blacklistCheck} and {@code WarUtils.notify} still use the default locale, and
 * both take admin-supplied config rather than an enum constant, so neither is covered here.
 *
 * <p>Only the static derivation is exercised, for the same reason {@link GuildHandlerTierTest} does
 * the same: it is the whole of the decision, and testing it directly keeps the test free of a
 * {@link GuildHandler} instance, which would need a plugin, a data folder and a database behind it.
 */
class GuildHandlerRolePermTest {

    private static final String PERMISSION_PATH_SEGMENT = ".permissions.";

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
    @DisplayName("permission keys are generated with a locale-independent lowercase")
    void keysAreLocaleIndependent() {
        Locale.setDefault(new Locale("tr", "TR"));

        // The regression this file exists for. INITIATE_WAR is the only constant containing an
        // uppercase "I", so it is the one a default-locale lowercase silently corrupted.
        assertEquals("initiate-war", GuildHandler.rolePermissionKey(GuildRolePerm.INITIATE_WAR));
    }

    @Test
    @DisplayName("every permission key is derived the same way under any locale")
    void everyKeyIsStableAcrossLocales() {
        Locale.setDefault(Locale.ENGLISH);
        final Set<String> underEnglish = allKeys();

        for (Locale locale : Arrays.asList(new Locale("tr", "TR"), Locale.GERMAN, Locale.FRENCH)) {
            Locale.setDefault(locale);
            assertEquals(underEnglish, allKeys(), () -> "keys changed under locale " + locale);
        }
    }

    @Test
    @DisplayName("the derived key shape is the one RoleSettings writes")
    void keyShapeIsStable() {
        final List<String> sample = Arrays.asList(
                GuildHandler.rolePermissionKey(GuildRolePerm.INITIATE_WAR),
                GuildHandler.rolePermissionKey(GuildRolePerm.CHANGE_HOME),
                GuildHandler.rolePermissionKey(GuildRolePerm.SEE_CODE_REDEEMERS),
                GuildHandler.rolePermissionKey(GuildRolePerm.DEPOSIT_MONEY));

        assertEquals(Arrays.asList("initiate-war", "change-home", "see-code-redeemers", "deposit-money"), sample);
    }

    @Test
    @DisplayName("RoleSettings declares the permission keys the derivation looks for")
    void everyPermissionHasAConfigKey() {
        final Set<String> paths = configuredPaths();
        final Set<String> configured = suffixesOf(paths);
        final Set<String> derived = allKeys();

        // Guards the assertions below from passing vacuously. If RoleSettings ever stops exposing its
        // keys as static Property fields, or the path shape changes, this fails instead of quietly
        // reducing the check to nothing.
        assertFalse(paths.isEmpty(), "no permission keys were read out of RoleSettings");

        // loadRoles() walks every role level, so the read has to span them too. A filter that picked
        // up only roles.0 would otherwise satisfy every assertion below while proving nothing about
        // the levels below it.
        assertTrue(roleLevelsOf(paths).size() > 1,
                () -> "permission keys were only read for " + roleLevelsOf(paths));

        // Direction one: a key RoleSettings declares has to be reachable from some constant, or the
        // config option exists but can never be granted to anybody.
        final Set<String> unreachable = new TreeSet<>(configured);
        unreachable.removeAll(derived);

        assertTrue(unreachable.isEmpty(),
                () -> "roles.yml keys that no permission constant can grant: " + unreachable);

        // Direction two: a constant normally has a declared key too, or it can never be granted
        // through the generated config. SERVER_OWNER is the one deliberate exception. Every
        // @Conditions("perm:...") site names a constant explicitly, so the "SERVER_OWNER" fallback
        // in ACFHandler.loadConditions is never reached, but a server owner who hand-writes
        // `server-owner: true` into roles.yml does get the permission, so removing the constant would
        // break them.
        final Set<String> undeclared = new TreeSet<>(derived);
        undeclared.removeAll(configured);

        assertEquals(Collections.singleton("server-owner"), undeclared,
                "a new permission was added without a matching roles.yml key in RoleSettings");
    }

    /**
     * The key suffixes {@link GuildHandler#loadRoles()} will look for, one per enum constant.
     *
     * @return every derived key
     */
    private static Set<String> allKeys() {
        return Stream.of(GuildRolePerm.values())
                .map(GuildHandler::rolePermissionKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Every permission path {@link RoleSettings} declares, read from the {@link Property} fields the
     * same way ConfigMe reads them.
     *
     * @return the full dotted paths, for example {@code roles.0.permissions.invite}
     */
    private static Set<String> configuredPaths() {
        final Set<String> paths = new TreeSet<>();

        for (Field field : RoleSettings.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !Property.class.isAssignableFrom(field.getType())) {
                continue;
            }

            final String path = ((Property<?>) read(field)).getPath();

            if (path.contains(PERMISSION_PATH_SEGMENT)) {
                paths.add(path);
            }
        }

        return paths;
    }

    /**
     * The distinct role levels the declared permission paths are spread across.
     *
     * <p>{@link GuildHandler#loadRoles()} reads every level, so a partial read here would hide a
     * permission that is declared for one role but missing for another.
     *
     * @param paths the declared permission paths
     * @return the role level prefixes, for example {@code [roles.0, roles.1]}
     */
    private static Set<String> roleLevelsOf(Set<String> paths) {
        return paths.stream().map(GuildHandlerRolePermTest::roleLevelOf).collect(Collectors.toSet());
    }

    private static String roleLevelOf(String path) {
        // "roles.0.permissions.invite" -> "roles.0"; the role index is the second segment.
        final int firstDot = path.indexOf('.');
        return path.substring(0, path.indexOf('.', firstDot + 1));
    }

    private static Set<String> suffixesOf(Set<String> paths) {
        return paths.stream()
                .map(path -> path.substring(path.indexOf(PERMISSION_PATH_SEGMENT) + PERMISSION_PATH_SEGMENT.length()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private static Object read(Field field) {
        try {
            return field.get(null);
        } catch (IllegalAccessException e) {
            throw new AssertionError("cannot read " + field, e);
        }
    }
}
