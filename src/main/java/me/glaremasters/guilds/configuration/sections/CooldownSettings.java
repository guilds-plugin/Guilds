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
package me.glaremasters.guilds.configuration.sections;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by GlareMasters
 * Date: 1/17/2019
 * Time: 2:29 PM
 */
public class CooldownSettings implements SettingsHolder {

    @Comment("How often (in seconds) can a player set their guild home?")
    public static final Property<Integer> SETHOME =
            newProperty("timers.cooldowns.sethome", 60);

    @Comment("How often (in seconds) can a player go to their guild home?")
    public static final Property<Integer> HOME =
            newProperty("timers.cooldowns.home", 60);

    @Comment("How often (in seconds) can a player request to join a guild?")
    public static final Property<Integer> REQUEST =
            newProperty("timers.cooldowns.request", 60);

    @Comment("How long should a user have to wait before joining a new guild after leaving one?")
    public static final Property<Integer> JOIN =
            newProperty("timers.cooldowns.join", 120);

    @Comment("Do you want to enable making players stand still before teleporting?")
    public static final Property<Boolean> WU_HOME_ENABLED =
            newProperty("timers.warmups.home.enabled", false);

    @Comment("How long should a user have to stand still before teleporting?")
    public static final Property<Integer> WU_HOME =
            newProperty("timers.warmups.home.time", 3);

    private CooldownSettings() {
    }
}
