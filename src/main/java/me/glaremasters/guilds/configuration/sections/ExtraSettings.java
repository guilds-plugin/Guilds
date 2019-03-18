/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
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

import java.util.List;

import static ch.jalu.configme.properties.PropertyInitializer.newListProperty;
import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by GlareMasters
 * Date: 1/17/2019
 * Time: 2:29 PM
 */
public class ExtraSettings implements SettingsHolder {

    @Comment("What do you want the name of the upgrade ticket to be?")
    public static final Property<String> TICKET_NAME =
            newProperty("extras.ticket.name", "&bGuild Upgrade Ticket");

    @Comment("What do you want the lore of the ticket to be?")
    public static final Property<List<String>> TICKET_LORE =
            newListProperty("extras.ticket.lore", "&dRight click this ticket to upgrade your guild tier!");

    @Comment("What do you want the material of the ticket to be?")
    public static final Property<String> TICKET_MATERIAL =
            newProperty("extras.ticket.material", "PAPER");

    @Comment("How long do you want the default length of guild codes to be?")
    public static final Property<Integer> CODE_LENGTH =
            newProperty("extras.code.length", 7);

    @Comment("Do you want inactive codes (no uses left) to display on the /guild code list?")
    public static final Property<Boolean> LIST_INACTIVE_CODES =
            newProperty("extras.code.list-inactive-codes", true);

    public static final Property<Integer> ACTIVE_CODE_AMOUNT =
            newProperty("extras.code.amount", 10);



    private ExtraSettings() {
    }
}