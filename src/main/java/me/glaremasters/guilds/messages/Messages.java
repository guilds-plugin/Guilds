package me.glaremasters.guilds.messages;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

/**
 * Created by GlareMasters
 * Date: 9/12/2018
 * Time: 5:00 PM
 */
public enum Messages implements MessageKeyProvider {

    CREATE__SUCCESSFUL, CREATE__WARNING, CREATE__GUILD_NAME_TAKEN, CREATE__CANCELLED,

    ERROR__ALREADY_IN_GUILD;

    private final MessageKey key = MessageKey.of(this.name().toLowerCase().replace("__", ".").replace("_", "-"));


    public MessageKey getMessageKey() {
        return key;
    }
}
