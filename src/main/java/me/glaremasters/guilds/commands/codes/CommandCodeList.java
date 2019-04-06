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

package me.glaremasters.guilds.commands.codes;

import ch.jalu.configme.SettingsManager;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.configuration.sections.CodeSettings;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 5:19 PM
 */
@AllArgsConstructor @CommandAlias(Constants.ROOT_ALIAS)
public class CommandCodeList extends BaseCommand {

    private SettingsManager settingsManager;

    /**
     * List all the current invite codes in your guild
     * @param player the player fetching the list
     * @param guild the guild the player is in
     */
    @Subcommand("code list")
    @Description("{@@descriptions.code-list}")
    @CommandPermission(Constants.CODE_PERM + "list")
    public void execute(Player player, Guild guild) {

        if (guild.getCodes().isEmpty()) {
            getCurrentCommandIssuer().sendInfo(Messages.CODES__EMPTY);
            return;
        }

        getCurrentCommandIssuer().sendInfo(Messages.CODES__LIST_HEADER);
        if (settingsManager.getProperty(CodeSettings.LIST_INACTIVE_CODES)) {
            guild.getCodes().forEach(c -> getCurrentCommandIssuer().sendInfo(Messages.CODES__LIST_ITEM,
                    "{code}", c.getId(),
                    "{amount}", String.valueOf(c.getUses()),
                    "{creator}", Bukkit.getOfflinePlayer(c.getCreator()).getName()));
        } else {
            guild.getActiveCodes().forEach(c -> getCurrentCommandIssuer().sendInfo(Messages.CODES__LIST_ITEM,
                    "{code}", c.getId(),
                    "{amount}", String.valueOf(c.getUses()),
                    "{creator}", Bukkit.getOfflinePlayer(c.getCreator()).getName()));
        }

    }

}