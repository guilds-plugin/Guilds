package me.glaremasters.guilds.actions

/**
 * Interface for representing a confirmable action.
 */
interface ConfirmAction {
    /**
     * Accept the action.
     */
    fun accept()

    /**
     * Decline the action.
     */
    fun decline()
}
