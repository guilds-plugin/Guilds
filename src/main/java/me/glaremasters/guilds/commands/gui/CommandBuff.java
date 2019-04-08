/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
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

package me.glaremasters.guilds.commands.gui;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/8/2019
 * Time: 10:50 AM
 */
@AllArgsConstructor @CommandAlias(Constants.ROOT_ALIAS)
public class CommandBuff extends BaseCommand {

    /**
     * Open the guild buff menu
     * @param player the player opening the menu
     * @param guild the guild which's player is opening the menu
     * @param role the role of the player
     */
    @Subcommand("buff")
    @Description("{@@descriptions.buff}")
    @CommandPermission(Constants.BASE_PERM + "buff")
    public void onBuff(Player player, Guild guild, GuildRole role) {
        // Handle the GUI
    }

}
