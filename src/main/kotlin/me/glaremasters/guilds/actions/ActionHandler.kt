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
}
