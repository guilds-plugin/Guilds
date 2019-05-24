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
import me.glaremasters.guilds.configuration.sections.CooldownSettings;
import me.glaremasters.guilds.configuration.sections.CostSettings;
import me.glaremasters.guilds.cooldowns.Cooldown;
import me.glaremasters.guilds.cooldowns.CooldownHandler;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.exceptions.InvalidPermissionException;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import me.glaremasters.guilds.utils.EconomyUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 11:00 PM
 */
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandSetHome extends BaseCommand {

    @Dependency private Economy economy;
    @Dependency private SettingsManager settingsManager;
    @Dependency private CooldownHandler cooldownHandler;

    /**
     * Set a guild home
     * @param player the player setting the home
     * @param guild the guild that home is being set
     * @param role role of player
     */
    @Subcommand("sethome")
    @Description("{@@descriptions.sethome}")
    @CommandPermission(Constants.BASE_PERM + "sethome")
    public void execute(Player player, Guild guild, GuildRole role) {

        if (!role.isChangeHome())
            ACFUtil.sneaky(new InvalidPermissionException());

        if (cooldownHandler.hasCooldown(Cooldown.TYPES.SetHome.name(), player.getUniqueId()))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.SETHOME__COOLDOWN, "{amount}",
                    String.valueOf(cooldownHandler.getRemaining(Cooldown.TYPES.SetHome.name(), player.getUniqueId()))));

        double cost = settingsManager.getProperty(CostSettings.SETHOME);

        if (!EconomyUtils.hasEnough(guild.getBalance(), cost))
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ERROR__NOT_ENOUGH_MONEY));


        cooldownHandler.addCooldown(player, Cooldown.TYPES.SetHome.name(), settingsManager.getProperty(CooldownSettings.SETHOME), TimeUnit.SECONDS);

        guild.setNewHome(player);

        guild.setBalance(guild.getBalance() - cost);
        getCurrentCommandIssuer().sendInfo(Messages.SETHOME__SUCCESSFUL);
    }

}