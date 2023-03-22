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
package me.glaremasters.guilds.conf

import ch.jalu.configme.Comment
import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer
import ch.jalu.configme.properties.PropertyInitializer.newBeanProperty
import ch.jalu.configme.properties.PropertyInitializer.newProperty
import ch.jalu.configme.properties.types.BeanPropertyType
import me.glaremasters.guilds.conf.objects.BuffCommand
import me.glaremasters.guilds.conf.objects.BuffNav
import me.glaremasters.guilds.conf.objects.BuffNavItem
import me.glaremasters.guilds.conf.objects.BuffSettings
import me.glaremasters.guilds.conf.objects.GuildBuff

internal object GuildBuffSettings : SettingsHolder {

    @JvmField
    @Comment("What should the name of the inventory be?")
    val GUI_NAME: Property<String> = newProperty("guild-buffs.gui-name", "Guild Buffs")

    @JvmField
    @Comment("How often (in seconds) can a guild buy a buff?")
    val COOLDOWN = newProperty("guild-buffs.cooldown", 60)

    @JvmField
    @Comment("Do we want to allow users to have more than one buff at a time?")
    val BUFF_STACKING: Property<Boolean> = newProperty("guild-buffs.buff-stacking", false)

    @JvmField
    @Comment("Set the name and material for the navigation buttons")
    val NAVIGATION: Property<BuffNav> = newBeanProperty(BuffNav::class.java, "guild-buffs.nav", BuffNav(BuffNavItem("EMPTY_MAP", "Next"), BuffNavItem("EMPTY_MAP", "Previous")))

    @JvmField
    @Comment("This is where the buffs themselves are to be created. You can create an unlimited number of buffs as long as the IDENTIFIERS are different.",
            "Ths identifiers can be anything you want, they just tell the plugin that they aren't the same as the buff before.",
            "The buffs are loaded into the menu in the order they are listed in the config. So, to change the order, simply change their location in their list of the config.",
            "Example Buff:",
            "    # The identifier of the buff, can be anything you want.\n" +
                    "#    - identifier: '1'\n" +
                    "    # This is what the buff will look like to the player when they DON'T have the permission listed below.\n" +
                    "#      locked:\n" +
                    "#        name: '&a&lSubstance of the Redmod Graff'\n" +
                    "#        material: FEATHER\n" +
                    "#        lore:\n" +
                    "#        - '&aType » &7Haste'\n" +
                    "#        - '&aLength » &760 Seconds'\n" +
                    "#        - '&aCost » &7\$60'\n" +
                    "    # This is what the buff will look like to the player when they DO have the permission listed below.\n" +
                    "#      unlocked:\n" +
                    "#        name: '&a&lSubstance of the Redmod Graff'\n" +
                    "#        material: FEATHER\n" +
                    "#        lore:\n" +
                    "#        - '&aType » &7Haste'\n" +
                    "#        - '&aLength » &760 Seconds'\n" +
                    "#        - '&aCost » &7\$60'\n" +
                    "    # The price of the buff, pulled from Guild Bank.\n" +
                    "#      price: 60.0\n" +
                    "    # The effects that the guild will get for buying the buff. You should be able to list as many as you want.\n" +
                    "    # The way you do it is: EFFECT_TYPE;AMPLIFICATION;LENGTH OF BUFF\n" +
                    "    # So, this effect here will give Haste I for 60 seconds.\n" +
                    "#      effects:\n" +
                    "#      - FAST_DIGGING;0;60\n" +
                    "    # The permission required to purchase the buff.\n" +
                    "#      permission: example.perm.here\n" +
                    "    # Would you like to execute commands on the player that bought the buff for the guild? Supports {player}, {buyer}, {buff_name}.\n" +
                    "#      clicker:\n" +
                    "#        enabled: false\n" +
                    "#        commands:\n" +
                    "#        - ''\n" +
                    "    # Would you like to execute commands on the guild that bough the buff? Supports {player}, {buyer}, {buff_name}.\n" +
                    "#      guild:\n" +
                    "#        enabled: false\n" +
                    "#        commands:\n" +
                    "#        - ''"
    )
    val BUFFS: Property<MutableList<GuildBuff>> = PropertyInitializer.listProperty(BeanPropertyType.of(GuildBuff::class.java)).path("guild-buffs.buffs").defaultValue(
            listOf(
                    GuildBuff("1", BuffSettings("&a&lSubstance of the Redmod Graff", "FEATHER", listOf("&aType » &7Haste", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSettings("&a&lSubstance of the Redmod Graff", "FEATHER", listOf("&aType » &7Haste", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("FAST_DIGGING;0;60"), "example.perm.here", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
                    GuildBuff("2", BuffSettings("&a&lBlessing of the Cheetah", "SUGAR", listOf("&aType » &7Speed", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSettings("&a&lBlessing of the Cheetah", "SUGAR", listOf("&aType » &7Speed", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("SPEED;0;60"), "example.perm.here", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
                    GuildBuff("3", BuffSettings("&a&lScales of the Dragon", "BLAZE_POWDER", listOf("&aType » &7Fire-Resistance", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSettings("&a&lScales of the Dragon", "BLAZE_POWDER", listOf("&aType » &7Fire-Resistance", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("FIRE_RESISTANCE;0;60"), "example.perm.here", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
                    GuildBuff("4", BuffSettings("&a&lEyes of the Lurking Demon", "EYE_OF_ENDER", listOf("&aType » &7Night-Vision", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSettings("&a&lEyes of the Lurking Demon", "EYE_OF_ENDER", listOf("&aType » &7Night-Vision", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("NIGHT_VISION;0;60"), "example.perm.here", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
                    GuildBuff("5", BuffSettings("&a&lFeet of the Ghostly Walker", "DIAMOND_BOOTS", listOf("&aType » &7Invisibility", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSettings("&a&lFeet of the Ghostly Walker", "DIAMOND_BOOTS", listOf("&aType » &7Invisibility", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("INVISIBILITY;0;60"), "example.perm.here", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
                    GuildBuff("6", BuffSettings("&a&lMighty Strength of the Pouncing Lion", "DIAMOND_SWORD", listOf("&aType » &7Strength", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSettings("&a&lMighty Strength of the Pouncing Lion", "DIAMOND_SWORD", listOf("&aType » &7Strength", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("INCREASE_DAMAGE;0;60"), "example.perm.here", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
                    GuildBuff("7", BuffSettings("&a&lBounce of the Quick Witted Rabbit", "DIAMOND_BOOTS", listOf("&aType » &7Jump", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSettings("&a&lBounce of the Quick Witted Rabbit", "DIAMOND_BOOTS", listOf("&aType » &7Jump", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("JUMP;0;60"), "example.perm.here", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
                    GuildBuff("8", BuffSettings("&a&lLungs of the Albino Shark", "BUCKET", listOf("&aType » &7Water-Breathing", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSettings("&a&lLungs of the Albino Shark", "BUCKET", listOf("&aType » &7Water-Breathing", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("WATER_BREATHING;0;60"), "example.perm.here", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
                    GuildBuff("9", BuffSettings("&a&lIntegrity of the Mystic Witch", "EMERALD", listOf("&aType » &7Regeneration", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSettings("&a&lIntegrity of the Mystic Witch", "EMERALD", listOf("&aType » &7Regeneration", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("REGENERATION;0;60"), "example.perm.here", BuffCommand(false, listOf("")), BuffCommand(false, listOf("")))
            )
    ).build()
}
