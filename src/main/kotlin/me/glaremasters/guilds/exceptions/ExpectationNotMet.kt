package me.glaremasters.guilds.exceptions

import co.aikar.commands.InvalidCommandArgument
import co.aikar.locales.MessageKeyProvider

/**
 * Created by Glare
 * Date: 4/4/2019
 * Time: 5:36 PM
 */
class ExpectationNotMet : InvalidCommandArgument {
    /**
     * Exception used when an expectation in the plugin is not being met
     * @param message the message to send to the user
     */
    constructor(message: MessageKeyProvider) : super(message.messageKey, false)

    /**
     * Exception used
     * @param key the message to send to the user
     * @param replacements any placeholders to replace
     */
    constructor(key: MessageKeyProvider, vararg replacements: String) : super(key.messageKey, false, *replacements)
}
