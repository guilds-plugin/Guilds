/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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
import ch.jalu.configme.properties.PropertyInitializer.newBeanProperty
import ch.jalu.configme.properties.PropertyInitializer.newProperty
import me.glaremasters.guilds.conf.objects.BuffCommand
import me.glaremasters.guilds.conf.objects.BuffHolder
import me.glaremasters.guilds.conf.objects.BuffSetting
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
    val BUFFS: Property<BuffHolder> = newBeanProperty(BuffHolder::class.java, "guild-buffs.list", BuffHolder(listOf(
            GuildBuff("1", BuffSetting("&a&lSubstance of the Redmod Graff", "FEATHER", listOf("&aType » &7Haste", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSetting("&a&lSubstance of the Redmod Graff", "FEATHER", listOf("&aType » &7Haste", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("FAST_DIGGING;0;60"), "my.special.permission", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
            GuildBuff("2", BuffSetting("&a&lBlessing of the Cheetah", "SUGAR", listOf("&aType » &7Speed", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSetting("&a&lBlessing of the Cheetah", "SUGAR", listOf("&aType » &7Speed", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("SPEED;0;60"), "my.special.permission", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
            GuildBuff("3", BuffSetting("&a&lScales of the Dragon", "BLAZE_POWDER", listOf("&aType » &7Fire-Resistance", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSetting("&a&lScales of the Dragon", "BLAZE_POWDER", listOf("&aType » &7Fire-Resistance", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("FIRE_RESISTANCE;0;60"), "my.special.permission", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
            GuildBuff("4", BuffSetting("&a&lEyes of the Lurking Demon", "EYE_OF_ENDER", listOf("&aType » &7Night-Vision", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSetting("&a&lEyes of the Lurking Demon", "EYE_OF_ENDER", listOf("&aType » &7Night-Vision", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("NIGHT_VISION;0;60"), "my.special.permission", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
            GuildBuff("5", BuffSetting("&a&lFeet of the Ghostly Walker", "DIAMOND_BOOTS", listOf("&aType » &7Invisibility", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSetting("&a&lFeet of the Ghostly Walker", "DIAMOND_BOOTS", listOf("&aType » &7Invisibility", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("INVISIBILITY;0;60"), "my.special.permission", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
            GuildBuff("6", BuffSetting("&a&lMighty Strength of the Pouncing Lion", "DIAMOND_SWORD", listOf("&aType » &7Strength", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSetting("&a&lMighty Strength of the Pouncing Lion", "DIAMOND_SWORD", listOf("&aType » &7Strength", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("INCREASE_DAMAGE;0;60"), "my.special.permission", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
            GuildBuff("7", BuffSetting("&a&lBounce of the Quick Witted Rabbit", "DIAMOND_BOOTS", listOf("&aType » &7Jump", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSetting("&a&lBounce of the Quick Witted Rabbit", "DIAMOND_BOOTS", listOf("&aType » &7Jump", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("JUMP;0;60"), "my.special.permission", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
            GuildBuff("8", BuffSetting("&a&lLungs of the Albino Shark", "BUCKET", listOf("&aType » &7Water-Breathing", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSetting("&a&lLungs of the Albino Shark", "BUCKET", listOf("&aType » &7Water-Breathing", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("WATER_BREATHING;0;60"), "my.special.permission", BuffCommand(false, listOf("")), BuffCommand(false, listOf(""))),
            GuildBuff("9", BuffSetting("&a&lIntegrity of the Mystic Witch", "EMERALD", listOf("&aType » &7Regeneration", "&aLength » &760 Seconds", "&aCost » &7$60")), BuffSetting("&a&lIntegrity of the Mystic Witch", "EMERALD", listOf("&aType » &7Regeneration", "&aLength » &760 Seconds", "&aCost » &7$60")), 60.00, listOf("REGENERATION;0;60"), "my.special.permission", BuffCommand(false, listOf("")), BuffCommand(false, listOf("")))
    )))
}