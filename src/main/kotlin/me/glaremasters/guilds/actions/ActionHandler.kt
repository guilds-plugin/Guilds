/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
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
package me.glaremasters.guilds.actions

import org.bukkit.command.CommandSender

/**
 * A class that handles and manages actions performed by [CommandSender].
 */
class ActionHandler {

    /**
     * A map that contains [CommandSender] as key and [ConfirmAction] as value.
     */
    private val actions = mutableMapOf<CommandSender, ConfirmAction>()

    /**
     * Adds an action to the map.
     *
     * @param sender [CommandSender] that performs the action.
     * @param action [ConfirmAction] to be performed.
     */
    fun addAction(sender: CommandSender, action: ConfirmAction) {
        actions[sender] = action
    }

    /**
     * Removes an action from the map.
     *
     * @param sender [CommandSender] that performs the action.
     */
    fun removeAction(sender: CommandSender?) {
        actions.remove(sender)
    }

    /**
     * Returns the action performed by the [CommandSender].
     *
     * @param sender [CommandSender] that performs the action.
     * @return [ConfirmAction] performed by the [CommandSender].
     */
    fun getAction(sender: CommandSender?): ConfirmAction? {
        return actions[sender]
    }

    /**
     * Returns all the actions stored in the map.
     *
     * @return Map containing [CommandSender] as key and [ConfirmAction] as value.
     */
    fun getActions(): Map<CommandSender, ConfirmAction> {
        return actions
    }
}
