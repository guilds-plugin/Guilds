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

package me.glaremasters.guilds.commands.member;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Values;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.messages.Messages;
import me.glaremasters.guilds.utils.Constants;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Created by Glare
 * Date: 4/14/2019
 * Time: 12:39 AM
 */
@CommandAlias(Constants.ROOT_ALIAS)
public class CommandLanguage extends BaseCommand {

    @Dependency private Guilds guilds;

    /**
     * Set the language for the plugin
     * @param player the player to set the language for
     * @param language the language to set it as
     */
    @Subcommand("language")
    @Description("{@@descriptions.language}")
    @CommandPermission(Constants.BASE_PERM + "language")
    @CommandCompletion("@languages")
    public void execute(Player player, @Values("@languages") @Single String language) {
        guilds.getCommandManager().setIssuerLocale(player, Locale.forLanguageTag(language));
        getCurrentCommandIssuer().sendInfo(Messages.LANGUAGES__SET, "{language}", language);
    }

}