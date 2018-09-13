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

    ERROR__ALREADY_IN_GUILD, ERROR__NO_GUILD, ERROR__ROLE_NO_PERMISSION, ERROR__GUILD_NO_EXIST, ERROR__PLAYER_NOT_FOUND,

    BANK__BALANCE,

    ADMIN__DELETE_WARNING, ADMIN__DELETE_SUCCESSFUL,

    BOOT__PLAYER_KICKED, BOOT__SUCCESSFUL, BOOT__KICKED,

    DECLINE__SUCCESS,

    DELETE__SUCCESS, DELETE__CANCELLED, DELETE__WARNING,

    LEAVE__CANCELLED, LEAVE__PLAYER_LEFT, LEAVE__SUCCESSFUL, LEAVE__WARNING_GUILDMASTER, LEAVE__WARNING, LEAVE__GUILDMASTER_LEFT;

    private final MessageKey key = MessageKey.of(this.name().toLowerCase().replace("__", ".").replace("_", "-"));


    public MessageKey getMessageKey() {
        return key;
    }
}
