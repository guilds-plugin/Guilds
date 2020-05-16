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

package me.glaremasters.guilds.commands.actions

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandIssuer
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Dependency
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import me.glaremasters.guilds.Guilds
import me.glaremasters.guilds.actions.ActionHandler
import me.glaremasters.guilds.exceptions.ExpectationNotMet
import me.glaremasters.guilds.messages.Messages
import me.glaremasters.guilds.utils.Constants
import org.bukkit.command.CommandSender

@CommandAlias("%guilds")
internal class CommandActions : BaseCommand() {
    @Dependency lateinit var guilds: Guilds
    @Dependency lateinit var actionHandler: ActionHandler

    @Subcommand("cancel")
    @Description("{@@descriptions.cancel}")
    @CommandPermission(Constants.BASE_PERM + "cancel")
    fun cancel(player: CommandIssuer) {
        val action = actionHandler.getAction(player.getIssuer<CommandSender>()) ?: throw ExpectationNotMet(Messages.CANCEL__ERROR)
        currentCommandIssuer.sendInfo(Messages.CANCEL__SUCCESS)
        action.decline()
    }

    @Subcommand("confirm")
    @Description("{@@descriptions.confirm}")
    @CommandPermission(Constants.BASE_PERM + "confirm")
    fun confirm(player: CommandIssuer) {
        val action = actionHandler.getAction(player.getIssuer<CommandSender>()) ?: throw ExpectationNotMet(Messages.CONFIRM__ERROR)
        currentCommandIssuer.sendInfo(Messages.CONFIRM__SUCCESS)
        action.accept()
    }
}
