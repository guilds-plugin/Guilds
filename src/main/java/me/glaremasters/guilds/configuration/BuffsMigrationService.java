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
import ch.jalu.configme.migration.PlainMigrationService;
import ch.jalu.configme.resource.PropertyReader;

public class BuffsMigrationService extends PlainMigrationService {

    /**
     * Checks if migrations need to be done
     *
     * @param reader            the property reader
     * @param configurationData the data to check
     * @return migrate or not
     */
    @Override
    protected boolean performMigrations(PropertyReader reader, ConfigurationData configurationData) {
        return hasDeprecatedProperties(reader);
    }

    /**
     * Check if config has old paths
     *
     * @param reader the reader
     * @return old paths or not
     */
    private static boolean hasDeprecatedProperties(PropertyReader reader) {
        String[] deprecatedProperties = {
                "guis",
        };
        for (String deprecatedPath : deprecatedProperties) {
            if (reader.contains(deprecatedPath)) {
                return true;
            }
        }
        return false;
    }

}