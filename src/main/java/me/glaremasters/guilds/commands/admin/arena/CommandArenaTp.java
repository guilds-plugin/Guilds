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

package me.glaremasters.guilds.commands.admin.arena;

import co.aikar.commands.ACFBukkitUtil;
import co.aikar.commands.ACFUtil;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.annotation.Values;
import me.glaremasters.guilds.arena.Arena;
import me.glaremasters.guilds.arena.ArenaHandler;
import me.glaremasters.guilds.exceptions.ExpectationNotMet;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

@CommandAlias("%guilds")
public class CommandArenaTp extends BaseCommand {

    @Dependency
    ArenaHandler arenaHandler;

    /**
     * Teleport to the location of a team for an arena
     * @param player the player running the command
     * @param arena the arena to select
     * @param location the team to teleport to
     */
    @Subcommand("arena tp")
    @CommandPermission(Constants.ADMIN_PERM)
    @Description("{@@descriptions.arena-tp}")
    @Syntax("<arena> <position>")
    @CommandCompletion("@arenas @locations")
    public void execute(Player player, @Values("@arenas") Arena arena, @Values("@locations") @Single String location) {

        // Make sure it's not null
        if (arena == null) {
            ACFUtil.sneaky(new ExpectationNotMet(Messages.ARENA__NO_EXIST));
        }

        // Make sure the selected point exist
        if (location.equalsIgnoreCase("challenger")) {
            if (arena.getChallenger() == null) {
                // Tell them that the point wasn't set yet
                ACFUtil.sneaky(new ExpectationNotMet(Messages.ARENA__POSITION_NOT_SET));
            }
            // Teleport them to the location
            player.teleport(ACFBukkitUtil.stringToLocation(arena.getChallenger()));
            // Make sure the selected point exist
        } else if (location.equalsIgnoreCase("defender")) {
            if (arena.getDefender() == null) {
                // Tell them that the point wasn't set yet
                ACFUtil.sneaky(new ExpectationNotMet(Messages.ARENA__POSITION_NOT_SET));
            }
            // Teleport them to the location
            player.teleport(ACFBukkitUtil.stringToLocation(arena.getDefender()));
        }

        // Tell them they've been teleported to the selected location
        getCurrentCommandIssuer().sendInfo(Messages.ARENA__TELEPORTED_TO_SELECTION, "{team}", location, "{arena}", arena.getName());
    }

}
