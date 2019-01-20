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

package me.glaremasters.guilds.actions;

import lombok.Getter;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;


//todo hmm?
public class ActionHandler {

    @Getter
    private Map<CommandSender, ConfirmAction> actions = new HashMap<>();

    /**
     * Adds an action to the action list
     * @param sender user
     * @param action action
     */
    public void addAction(CommandSender sender, ConfirmAction action) {
        actions.put(sender, action);
    }

    /**
     * Removes an action from the action list
     * @param sender user
     */
    public void removeAction(CommandSender sender) {
        actions.remove(sender);
    }

    /**
     * Retrieve an action via a CommandSender instance.
     *
     * @param sender the commandSender
     * @return an instance of
     * @see ConfirmAction
     * Used to decline or confirm a command request
     * @see ConfirmAction#accept()
     * @see ConfirmAction#decline()
     */
    public ConfirmAction getAction(CommandSender sender) {
        return actions.get(sender);
    }
}
