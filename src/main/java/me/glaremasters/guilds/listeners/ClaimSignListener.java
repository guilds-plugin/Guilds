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

package me.glaremasters.guilds.listeners;

import ch.jalu.configme.SettingsManager;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.ClaimSettings;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.ClaimUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;

/**
 * Created by Glare
 * Date: 5/29/2019
 * Time: 7:01 AM
 */
@AllArgsConstructor
public class ClaimSignListener implements Listener {

    private Guilds guilds;
    private SettingsManager settingsManager;
    private final WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (!event.getLine(0).equalsIgnoreCase("[Guild Claim]"))
            return;

        if (!player.hasPermission("guilds.claimsigns.place")) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__SIGN_NO_PERMISSION);
            event.setCancelled(true);
            return;
        }

        if (!settingsManager.getProperty(ClaimSettings.CLAIM_SIGNS)) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__SIGN_NOT_ENABLED);
            event.setCancelled(true);
            return;
        }

        if (event.getLine(1).isEmpty() || event.getLine(2).isEmpty()) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__SIGN_INVALID_FORMAT);
            event.setCancelled(true);
            return;
        }

        if (!ClaimUtils.checkAlreadyExist(wrapper, player, event.getLine(1))) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__SIGN_INVALID_REGION);
            event.setCancelled(true);
            return;
        }
        guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__SIGN_PLACED, "{region}", event.getLine(1), "{price}", event.getLine(2));
    }

}
