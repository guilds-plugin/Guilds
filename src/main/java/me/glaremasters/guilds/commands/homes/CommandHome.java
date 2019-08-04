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

package me.glaremasters.guilds.commands.homes;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.configuration.sections.CooldownSettings;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 11:00 PM
 */
@CommandAlias("%guilds")
public class CommandHome extends BaseCommand {

    @Dependency private CooldownHandler cooldownHandler;
    @Dependency private SettingsManager settingsManager;
    @Dependency private Guilds guilds;

    /**
     * Go to guild home
     * @param player the player teleporting
     * @param guild the guild to teleport to
     */
    @Subcommand("home")
    @Description("{@@descriptions.home}")
    @CommandPermission(Constants.BASE_PERM + "home")
    public void execute(Player player, Guild guild) {
        if (guild.getHome() == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.HOME__NO_HOME_SET));

        if (cooldownHandler.hasCooldown(Cooldown.TYPES.Home.name(), player.getUniqueId()))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.HOME__COOLDOWN, "{amount}",
                    String.valueOf(cooldownHandler.getRemaining(Cooldown.TYPES.Home.name(), player.getUniqueId()))));

        cooldownHandler.addCooldown(player, Cooldown.TYPES.Home.name(), settingsManager.getProperty(CooldownSettings.HOME), TimeUnit.SECONDS);

        if (settingsManager.getProperty(CooldownSettings.WU_HOME_ENABLED) && !player.hasPermission("guilds.warmup.bypass")) {
            Location initial = player.getLocation();
            getCurrentCommandIssuer().sendInfo(Messages.HOME__WARMUP, "{amount}", String.valueOf(settingsManager.getProperty(CooldownSettings.WU_HOME)));
            Guilds.newChain().delay(settingsManager.getProperty(CooldownSettings.WU_HOME), TimeUnit.SECONDS).sync(() -> {
                Location curr = player.getLocation();
                if (initial.distance(curr) > 1) {
                    guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.HOME__CANCELLED);
                }
                else {
                    player.teleport(guild.getHome().getAsLocation());
                    guilds.getCommandManager().getCommandIssuer(player).sendInfo(Messages.HOME__TELEPORTED);
                }
            }).execute();
        } else {
            player.teleport(guild.getHome().getAsLocation());
            getCurrentCommandIssuer().sendInfo(Messages.HOME__TELEPORTED);
        }

    }

}