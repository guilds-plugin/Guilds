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
import me.glaremasters.guilds.utils.LoggingUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class GuildsMigrationService extends PlainMigrationService {

    private final File pluginFolder;

    public GuildsMigrationService(File pluginFolder) {
        this.pluginFolder = pluginFolder;
    }

    /**
     * Checks if migrations need to be done
     *
     * @param reader            the property reader
     * @param configurationData the data to check
     * @return migrate or not
     */
    @Override
    protected boolean performMigrations(PropertyReader reader, ConfigurationData configurationData) {
        return hasOldObject(reader, "tiers.list", "tiers.yml", true)
                | hasOldObject(reader, "roles", "roles.yml", true)
                | hasOldObject(reader, "guis.guild-buffs.buffs", "buffs.yml", true)
                || hasDeprecatedProperties(reader);
    }

    /**
     * Check if config has old paths
     *
     * @param reader the reader
     * @return old paths or not
     */
    private static boolean hasDeprecatedProperties(PropertyReader reader) {
        String[] deprecatedProperties = {
                "hooks.essentials-remove-brackets",
                "tablist.enabled",
                "settings.save-interval",
                "settings.player-update-languages",
                "tiers.list",
                "tiers.carry-over",
                "roles",
                "guis.guild-buffs",
                "timers.cooldowns.sethome",
                "guild.damage.respect-wg-pvp-flag",
                "settings.syntax-name"
        };
        for (String deprecatedPath : deprecatedProperties) {
            if (reader.contains(deprecatedPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if old data exists and move it to the new file
     * @param reader the reader
     * @param contains the string to check for
     * @param fileName the name of the new file
     * @param doubleDeep double deep children
     * @return has old objects or not
     */
    private boolean hasOldObject(PropertyReader reader, String contains, String fileName, boolean doubleDeep) {
        if (reader.contains(contains)) {
            LoggingUtils.info("&f=====&a[CONFIG MIGRATOR]&f=====");
            LoggingUtils.info("&3Found old config value (&f" + contains + "&3)");
            LoggingUtils.info("&3Converting old config values to new config file (&f" + fileName + "&3)...");
            final File newFile = new File(pluginFolder, fileName);
            if (!newFile.exists()) {
                try {
                    newFile.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return false;
                }
                YamlConfiguration config = YamlConfiguration.loadConfiguration(newFile);
                for (String child : reader.getChildKeys(contains)) {
                    if (doubleDeep) {
                        for (String doubleChild : reader.getChildKeys(child)) {
                            config.set(doubleChild, reader.getObject(doubleChild));
                        }
                    } else {
                        config.set(child, reader.getObject(child));
                    }
                }
                try {
                    config.save(newFile);
                    LoggingUtils.info("&3Converting done!");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

}
