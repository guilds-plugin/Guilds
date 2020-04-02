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

package me.glaremasters.guilds.guis;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.PaperCommandManager;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.guild.GuildHandler;

/**
 * Created by Glare
 * Date: 5/15/2019
 * Time: 10:58 AM
 */
public class GUIHandler {

    private BuffGUI buffGUI;
    private ListGUI listGUI;
    private InfoGUI infoGUI;
    private InfoMembersGUI infoMembersGUI;
    private VaultGUI vaultGUI;

    public GUIHandler(Guilds guilds, SettingsManager settingsManager, GuildHandler guildHandler, PaperCommandManager commandManager, CooldownHandler cooldownHandler) {

        buffGUI = new BuffGUI(guilds, guilds.getSettingsHandler().getBuffSettings(), settingsManager, guildHandler, commandManager, cooldownHandler);
        listGUI = new ListGUI(guilds, settingsManager, guildHandler);
        infoGUI = new InfoGUI(guilds, settingsManager, guildHandler, cooldownHandler, commandManager);
        infoMembersGUI = new InfoMembersGUI(guilds, settingsManager, guildHandler);
        vaultGUI = new VaultGUI(guilds, settingsManager, guildHandler);
    }

    public BuffGUI getBuffGUI() {
        return this.buffGUI;
    }

    public ListGUI getListGUI() {
        return this.listGUI;
    }

    public InfoGUI getInfoGUI() {
        return this.infoGUI;
    }

    public InfoMembersGUI getInfoMembersGUI() {
        return this.infoMembersGUI;
    }

    public VaultGUI getVaultGUI() {
        return this.vaultGUI;
    }
}
