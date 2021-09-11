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
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.events.GuildClaimEvent;
import me.glaremasters.guilds.claim.ClaimEditor;
import me.glaremasters.guilds.claim.ClaimRegionHandler;
import me.glaremasters.guilds.claim.GuildClaim;
import me.glaremasters.guilds.configuration.sections.ClaimSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.guild.GuildRolePerm;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.claim.ClaimUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;
import org.codemc.worldguardwrapper.selection.ICuboidSelection;

/**
 * Created by Glare
 * Date: 5/29/2019
 * Time: 7:01 AM
 */
public class ClaimSignListener implements Listener {

    private final Guilds guilds;
    private final SettingsManager settingsManager;
    private final GuildHandler guildHandler;
    private final WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

    public ClaimSignListener(Guilds guilds, SettingsManager settingsManager, GuildHandler guildHandler) {
        this.guilds = guilds;
        this.settingsManager = settingsManager;
        this.guildHandler = guildHandler;
    }

    //TODO Fix all of this because Claim Signs...

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (!event.getLine(0).equalsIgnoreCase(settingsManager.getProperty(ClaimSettings.CLAIM_SIGN_TEXT)))
            return;

        if (!settingsManager.getProperty(ClaimSettings.CLAIM_SIGNS)) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__SIGN_NOT_ENABLED);
            event.setCancelled(true);
            return;
        }

        if (!player.hasPermission("guilds.claimsigns.place") && !player.hasPermission("worldguard.region.redefine.*")) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__SIGN_NO_PERMISSION);
            event.setCancelled(true);
            return;
        }

        if (event.getLine(1).isEmpty() || event.getLine(2).isEmpty()) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__SIGN_INVALID_FORMAT);
            event.setCancelled(true);
            return;
        }


        if (!ClaimUtils.checkOverlap(wrapper, player)) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__SIGN_INVALID_REGION);
            event.setCancelled(true);
            return;
        }

        guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__SIGN_PLACED, "{region}", event.getLine(1), "{price}", event.getLine(2));
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {

        if (!event.hasBlock())
            return;

        Block block = event.getClickedBlock();

        BlockState state = block.getState();

        if (!(state instanceof Sign))
            return;

        Sign sign = (Sign) state;

        Player player = event.getPlayer();

        if (!sign.getLine(0).equalsIgnoreCase("[Guild Claim]"))
            return;

        if (!settingsManager.getProperty(ClaimSettings.CLAIM_SIGNS)) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__SIGN_NOT_ENABLED);
            event.setCancelled(true);
            return;
        }

        Guild guild = guildHandler.getGuild(player);

        if (guild == null) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.ERROR__NO_GUILD);
            return;
        }

        if (!guild.memberHasPermission(player, GuildRolePerm.CLAIM_LAND)) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.ERROR__ROLE_NO_PERMISSION);
            return;
        }

        if (ClaimUtils.checkMaxAlreadyExist(wrapper, guild)) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__ALREADY_EXISTS);
            return;
        }

        if (guild.getBalance() < Double.parseDouble(sign.getLine(2))) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__SIGN_NOT_ENOUGH);
            return;
        }

        IWrappedRegion region = ClaimUtils.getRegionFromName(wrapper, sign.getLine(1));

        if (region == null) {
            guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__SIGN_INVALID_REGION);
            return;
        }

        ICuboidSelection selection = ClaimUtils.getSelection(wrapper, player, region.getId());
        wrapper.removeRegion(player.getWorld(), region.getId());

        GuildClaim claim = ClaimRegionHandler.createClaim(wrapper, guild, selection);
        guild.addGuildClaim(claim);

        ClaimEditor.addOwner(wrapper, claim, guild);
        ClaimEditor.addMembers(wrapper, claim, guild);
        ClaimEditor.setEnterMessage(wrapper, claim, settingsManager, guild);
        ClaimEditor.setExitMessage(wrapper, claim, settingsManager, guild);

        player.getWorld().getBlockAt(block.getLocation()).breakNaturally();

        guild.setBalance(guild.getBalance() - Double.parseDouble(sign.getLine(2)));

        Event firedEvent = new GuildClaimEvent(player, guild, claim);
        Bukkit.getPluginManager().callEvent(firedEvent);

        guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.CLAIM__SIGN_BUY_SUCCESS);
    }

}
