
/*
 * MIT License
 *
 * Copyright (c) 2019-2020 PiggyPiglet
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

package me.glaremasters.guilds.scanner;

import org.jetbrains.annotations.NotNull;

import java.security.CodeSource;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The ZISScanner (ZipInputStream Scanner), as the name suggests, scans the
 * literal jar file with a ZipInputStream; recursing through every file entry
 * while checking the name to see if it's a class, and if the package
 * isn't on the exclusion list. If all is well, load the class through the main
 * classes classloader.
 */
public final class ZISScanner {

    /**
     * Get all the classes in the main class's classloader, which are
     * in a specific package, and aren't in any of the excluded packages.
     * <p>
     * Semantics:
     * A class is not loaded if any of the following conditions are true:
     * <i>Filename includes the file name, and it's relative path from the root of the jar</i>
     * <ul>
     *   <li>Filename doesn't end with .class</li>
     *   <li>Filename doesn't start with the configured package, and doesn't
     *   start with RPF's package.</li>
     *   <li>Filename starts with any of the provided package exclusions</li>
     * </ul>
     * A file that follows the above criteria may not be loaded because:
     * <ul>
     *     <li>It's not a class (e.g. interfaces)</li>
     * </ul>
     * The class list may be empty because:
     * <ul>
     *     <li>The main class's code source is null</li>
     * </ul>
     * <p>
     * If any of the logic before the recursing throws an exception,
     * it'll be caught and wrapped into a RuntimeException, effectively
     * terminating the program.
     *
     * @param main Main class
     * @param pckg Package to scan
     * @return Set of loaded classes
     */
    public Set<Class<?>> getClasses(@NotNull final Class<?> main, @NotNull String pckg) {
        pckg = pckg.replace('.', '/');

        final ClassLoader loader = main.getClassLoader();
        final CodeSource src = main.getProtectionDomain().getCodeSource();

        if (src == null) {
            return Collections.emptySet();
        }

        final Set<Class<?>> classes = new HashSet<>();

        try (final ZipInputStream zip = new ZipInputStream(src.getLocation().openStream())) {
            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                final String name = entry.getName();

                if (!name.endsWith(".class") || (!name.startsWith(pckg))) {
                    continue;
                }

                try {
                    classes.add(loader.loadClass(name.replace('/', '.').replace(".class", "")));
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return classes;
    }
}