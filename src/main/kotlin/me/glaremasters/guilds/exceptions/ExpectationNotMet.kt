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
