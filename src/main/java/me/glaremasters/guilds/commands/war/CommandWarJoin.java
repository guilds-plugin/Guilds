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

package me.glaremasters.guilds.commands.war;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import me.glaremasters.guilds.challenges.ChallengeHandler;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.guild.GuildHandler;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 7/5/2019
 * Time: 9:34 PM
 */
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandWarJoin extends BaseCommand {

    @Dependency private GuildHandler guildHandler;
    @Dependency private ChallengeHandler challengeHandler;
    @Dependency private SettingsManager settingsManager;

    @Subcommand("war join")
    @Description("{@@descriptions.war-join}")
    @CommandPermission(Constants.WAR_PERM + "join")
    public void execute(Player player, Guild guild) {
        GuildChallenge challenge = challengeHandler.getChallenge(guild);

        // Check to make sure they have a pending challenge
        if (challenge == null)
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NO_PENDING_CHALLENGE));

        // Make sure the war is joinablke
        if (!challenge.isJoinble())
            ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__NOT_JOINABLE));

        // Check if they are the defender
        if (challenge.getDefender() == guild) {
            // Check defending size
            if (challenge.getDefendPlayers().size() == challenge.getMaxPlayersPerSide()) {
                ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__ALREADY_AT_MAX));
            }
            // If not full, add them to it
            challenge.getDefendPlayers().add(player.getUniqueId());
        } else {
            // Assume they are challenging if not defending
            if (challenge.getChallengePlayers().size() == challenge.getMaxPlayersPerSide()) {
                ACFUtil.sneaky(new ExpectationNotMet(Messages.WAR__ALREADY_AT_MAX));
            }
            // Add if they aren't full
            challenge.getChallengePlayers().add(player.getUniqueId());
        }

       // Tell the guild that a player has joined the war.
        guild.sendMessage(getCurrentCommandManager(), Messages.WAR__WAR_JOINED, "{player}", player.getName());
    }

}