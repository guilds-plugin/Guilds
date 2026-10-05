package me.glaremasters.guilds.exceptions

import co.aikar.commands.InvalidCommandArgument
import me.glaremasters.guilds.messages.Messages

/**
 * Thrown when a user's role isn't allowed to do an action.
 */
class InvalidPermissionException : InvalidCommandArgument(Messages.ERROR__ROLE_NO_PERMISSION, false)
