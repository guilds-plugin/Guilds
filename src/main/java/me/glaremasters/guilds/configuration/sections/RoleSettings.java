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

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Created by GlareMasters
 * Date: 1/17/2019
 * Time: 2:29 PM
 */
public class RoleSettings implements SettingsHolder {

    public static final Property<String> ZERO_NAME =
            newProperty("roles.0.name", "GuildMaster");

    public static final Property<String> ZERO_PERM =
            newProperty("roles.0.permission-node", "guilds.roles.master");

    public static final Property<Boolean> ZERO_AB =
            newProperty("roles.0.permissions.activate-buff", true);

    public static final Property<Boolean> ZERO_AA =
            newProperty("roles.0.permissions.add-ally", true);

    public static final Property<Boolean> ZERO_AC =
            newProperty("roles.0.permissions.ally-chat", true);

    public static final Property<Boolean> ZERO_CH =
            newProperty("roles.0.permissions.change-home", true);

    public static final Property<Boolean> ZERO_CP =
            newProperty("roles.0.permissions.change-prefix", true);

    public static final Property<Boolean> ZERO_R =
            newProperty("roles.0.permissions.rename", true);

    public static final Property<Boolean> ZERO_C =
            newProperty("roles.0.permissions.chat", true);

    public static final Property<Boolean> ZERO_D =
            newProperty("roles.0.permissions.demote", true);

    public static final Property<Boolean> ZERO_DM =
            newProperty("roles.0.permissions.deposit-money", true);

    public static final Property<Boolean> ZERO_I =
            newProperty("roles.0.permissions.invite", true);

    public static final Property<Boolean> ZERO_K =
            newProperty("roles.0.permissions.kick", true);

    public static final Property<Boolean> ZERO_OV =
            newProperty("roles.0.permissions.open-vault", true);

    public static final Property<Boolean> ZERO_P =
            newProperty("roles.0.permissions.promote", true);

    public static final Property<Boolean> ZERO_RA =
            newProperty("roles.0.permissions.remove-ally", true);

    public static final Property<Boolean> ZERO_RG =
            newProperty("roles.0.permissions.remove-guild", true);

    public static final Property<Boolean> ZERO_TG =
            newProperty("roles.0.permissions.toggle-guild", true);

    public static final Property<Boolean> ZERO_TRG =
            newProperty("roles.0.permissions.transfer-guild", true);

    public static final Property<Boolean> ZERO_UG =
            newProperty("roles.0.permissions.upgrade-guild", true);

    public static final Property<Boolean> ZERO_WM =
            newProperty("roles.0.permissions.withdraw-money", true);

    public static final Property<Boolean> ZERO_CL =
            newProperty("roles.0.permissions.claim-land", true);

    public static final Property<Boolean> ZERO_UL =
            newProperty("roles.0.permissions.unclaim-land", true);

    public static final Property<Boolean> ZERO_DE =
            newProperty("roles.0.permissions.destroy", true);

    public static final Property<Boolean> ZERO_PL =
            newProperty("roles.0.permissions.place", true);

    public static final Property<Boolean> ZERO_IN =
            newProperty("roles.0.permissions.interact", true);

    public static final Property<Boolean> ZERO_CM =
            newProperty("roles.0.permissions.create-code", true);

    public static final Property<Boolean> ZERO_CD =
            newProperty("roles.0.permissions.delete-code", true);

    public static final Property<Boolean> ZERO_SCR =
            newProperty("roles.0.permissions.see-code-redeemers", true);

    public static final Property<Boolean> ZERO_MOTD =
            newProperty("roles.0.permissions.modify-motd", true);

    public static final Property<Boolean> ZERO_WAR =
            newProperty("roles.0.permissions.initiate-war", true);

    public static final Property<String> ONE_NAME =
            newProperty("roles.1.name", "Officer");

    public static final Property<String> ONE_PERM =
            newProperty("roles.1.permission-node", "guilds.roles.officer");

    public static final Property<Boolean> ONE_AB =
            newProperty("roles.1.permissions.activate-buff", false);

    public static final Property<Boolean> ONE_AA =
            newProperty("roles.1.permissions.add-ally", true);

    public static final Property<Boolean> ONE_AC =
            newProperty("roles.1.permissions.ally-chat", true);

    public static final Property<Boolean> ONE_CH =
            newProperty("roles.1.permissions.change-home", true);

    public static final Property<Boolean> ONE_CP =
            newProperty("roles.1.permissions.change-prefix", false);

    public static final Property<Boolean> ONE_R =
            newProperty("roles.1.permissions.rename", false);

    public static final Property<Boolean> ONE_C =
            newProperty("roles.1.permissions.chat", true);

    public static final Property<Boolean> ONE_D =
            newProperty("roles.1.permissions.demote", true);

    public static final Property<Boolean> ONE_DM =
            newProperty("roles.1.permissions.deposit-money", true);

    public static final Property<Boolean> ONE_I =
            newProperty("roles.1.permissions.invite", true);

    public static final Property<Boolean> ONE_K =
            newProperty("roles.1.permissions.kick", true);

    public static final Property<Boolean> ONE_OV =
            newProperty("roles.1.permissions.open-vault", true);

    public static final Property<Boolean> ONE_P =
            newProperty("roles.1.permissions.promote", true);

    public static final Property<Boolean> ONE_RA =
            newProperty("roles.1.permissions.remove-ally", true);

    public static final Property<Boolean> ONE_RG =
            newProperty("roles.1.permissions.remove-guild", false);

    public static final Property<Boolean> ONE_TG =
            newProperty("roles.1.permissions.toggle-guild", false);

    public static final Property<Boolean> ONE_TRG =
            newProperty("roles.1.permissions.transfer-guild", false);

    public static final Property<Boolean> ONE_UG =
            newProperty("roles.1.permissions.upgrade-guild", false);

    public static final Property<Boolean> ONE_WM =
            newProperty("roles.1.permissions.withdraw-money", true);

    public static final Property<Boolean> ONE_CL =
            newProperty("roles.1.permissions.claim-land", false);

    public static final Property<Boolean> ONE_UL =
            newProperty("roles.1.permissions.unclaim-land", false);

    public static final Property<Boolean> ONE_DE =
            newProperty("roles.1.permissions.destroy", true);

    public static final Property<Boolean> ONE_PL =
            newProperty("roles.1.permissions.place", true);

    public static final Property<Boolean> ONE_IN =
            newProperty("roles.1.permissions.interact", true);

    public static final Property<Boolean> ONE_CM =
            newProperty("roles.1.permissions.create-code", true);

    public static final Property<Boolean> ONE_CD =
            newProperty("roles.1.permissions.delete-code", true);

    public static final Property<Boolean> ONE_SCR =
            newProperty("roles.1.permissions.see-code-redeemers", true);

    public static final Property<Boolean> ONE_MOTD =
            newProperty("roles.1.permissions.modify-motd", false);

    public static final Property<Boolean> ONE_WAR =
            newProperty("roles.1.permissions.initiate-war", true);

    public static final Property<String> TWO_NAME =
            newProperty("roles.2.name", "Veteran");

    public static final Property<String> TWO_PERM =
            newProperty("roles.2.permission-node", "guilds.roles.veteran");

    public static final Property<Boolean> TWO_AB =
            newProperty("roles.2.permissions.activate-buff", false);

    public static final Property<Boolean> TWO_AA =
            newProperty("roles.2.permissions.add-ally", false);

    public static final Property<Boolean> TWO_AC =
            newProperty("roles.2.permissions.ally-chat", true);

    public static final Property<Boolean> TWO_CH =
            newProperty("roles.2.permissions.change-home", false);

    public static final Property<Boolean> TWO_CP =
            newProperty("roles.2.permissions.change-prefix", false);

    public static final Property<Boolean> TWO_R =
            newProperty("roles.2.permissions.rename", false);

    public static final Property<Boolean> TWO_C =
            newProperty("roles.2.permissions.chat", true);

    public static final Property<Boolean> TWO_D =
            newProperty("roles.2.permissions.demote", false);

    public static final Property<Boolean> TWO_DM =
            newProperty("roles.2.permissions.deposit-money", true);

    public static final Property<Boolean> TWO_I =
            newProperty("roles.2.permissions.invite", true);

    public static final Property<Boolean> TWO_K =
            newProperty("roles.2.permissions.kick", false);

    public static final Property<Boolean> TWO_OV =
            newProperty("roles.2.permissions.open-vault", true);

    public static final Property<Boolean> TWO_P =
            newProperty("roles.2.permissions.promote", false);

    public static final Property<Boolean> TWO_RA =
            newProperty("roles.2.permissions.remove-ally", false);

    public static final Property<Boolean> TWO_RG =
            newProperty("roles.2.permissions.remove-guild", false);

    public static final Property<Boolean> TWO_TG =
            newProperty("roles.2.permissions.toggle-guild", false);

    public static final Property<Boolean> TWO_TRG =
            newProperty("roles.2.permissions.transfer-guild", false);

    public static final Property<Boolean> TWO_UG =
            newProperty("roles.2.permissions.upgrade-guild", false);

    public static final Property<Boolean> TWO_WM =
            newProperty("roles.2.permissions.withdraw-money", false);

    public static final Property<Boolean> TWO_CL =
            newProperty("roles.2.permissions.claim-land", false);

    public static final Property<Boolean> TWO_UL =
            newProperty("roles.2.permissions.unclaim-land", false);

    public static final Property<Boolean> TWO_DE =
            newProperty("roles.2.permissions.destroy", true);

    public static final Property<Boolean> TWO_PL =
            newProperty("roles.2.permissions.place", true);

    public static final Property<Boolean> TWO_IN =
            newProperty("roles.2.permissions.interact", true);

    public static final Property<Boolean> TWO_CM =
            newProperty("roles.2.permissions.create-code", false);

    public static final Property<Boolean> TWO_CD =
            newProperty("roles.2.permissions.delete-code", false);

    public static final Property<Boolean> TWO_SCR =
            newProperty("roles.2.permissions.see-code-redeemers", false);

    public static final Property<Boolean> TWO_MOTD =
            newProperty("roles.2.permissions.modify-motd", false);

    public static final Property<Boolean> TWO_WAR =
            newProperty("roles.2.permissions.initiate-war", false);

    public static final Property<String> THREE_NAME =
            newProperty("roles.3.name", "Member");

    public static final Property<String> THREE_PERM =
            newProperty("roles.3.permission-node", "guilds.roles.member");

    public static final Property<Boolean> THREE_AB =
            newProperty("roles.3.permissions.activate-buff", false);

    public static final Property<Boolean> THREE_AA =
            newProperty("roles.3.permissions.add-ally", false);

    public static final Property<Boolean> THREE_AC =
            newProperty("roles.3.permissions.ally-chat", true);

    public static final Property<Boolean> THREE_CH =
            newProperty("roles.3.permissions.change-home", false);

    public static final Property<Boolean> THREE_CP =
            newProperty("roles.3.permissions.change-prefix", false);

    public static final Property<Boolean> THREE_R =
            newProperty("roles.3.permissions.rename", false);

    public static final Property<Boolean> THREE_C =
            newProperty("roles.3.permissions.chat", true);

    public static final Property<Boolean> THREE_D =
            newProperty("roles.3.permissions.demote", false);

    public static final Property<Boolean> THREE_DM =
            newProperty("roles.3.permissions.deposit-money", true);

    public static final Property<Boolean> THREE_I =
            newProperty("roles.3.permissions.invite", false);

    public static final Property<Boolean> THREE_K =
            newProperty("roles.3.permissions.kick", false);

    public static final Property<Boolean> THREE_OV =
            newProperty("roles.3.permissions.open-vault", true);

    public static final Property<Boolean> THREE_P =
            newProperty("roles.3.permissions.promote", false);

    public static final Property<Boolean> THREE_RA =
            newProperty("roles.3.permissions.remove-ally", false);

    public static final Property<Boolean> THREE_RG =
            newProperty("roles.3.permissions.remove-guild", false);

    public static final Property<Boolean> THREE_TG =
            newProperty("roles.3.permissions.toggle-guild", false);

    public static final Property<Boolean> THREE_TRG =
            newProperty("roles.3.permissions.transfer-guild", false);

    public static final Property<Boolean> THREE_UG =
            newProperty("roles.3.permissions.upgrade-guild", false);

    public static final Property<Boolean> THREE_WM =
            newProperty("roles.3.permissions.withdraw-money", false);

    public static final Property<Boolean> THREE_CL =
            newProperty("roles.3.permissions.claim-land", false);

    public static final Property<Boolean> THREE_UL =
            newProperty("roles.3.permissions.unclaim-land", false);

    public static final Property<Boolean> THREE_DE =
            newProperty("roles.3.permissions.destroy", true);

    public static final Property<Boolean> THREE_PL =
            newProperty("roles.3.permissions.place", true);

    public static final Property<Boolean> THREE_IN =
            newProperty("roles.3.permissions.interact", true);

    public static final Property<Boolean> THREE_CM =
            newProperty("roles.3.permissions.create-code", false);

    public static final Property<Boolean> THREE_CD =
            newProperty("roles.3.permissions.delete-code", false);

    public static final Property<Boolean> THREE_SCR =
            newProperty("roles.3.permissions.see-code-redeemers", false);

    public static final Property<Boolean> THREE_MOTD =
            newProperty("roles.3.permissions.modify-motd", false);

    public static final Property<Boolean> THREE_WAR =
            newProperty("roles.3.permissions.initiate-war", false);

    private RoleSettings() {
    }
}
