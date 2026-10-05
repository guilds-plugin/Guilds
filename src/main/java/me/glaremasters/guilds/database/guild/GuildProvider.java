package me.glaremasters.guilds.database.guild;

import me.glaremasters.guilds.guild.Guild;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * Stores guilds in whichever backend is configured.
 */
public interface GuildProvider {

    /** Creates the guild table, or the guild data folder on the JSON backend. */
    void createContainer(@Nullable String tablePrefix) throws IOException;

    boolean guildExists(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    List<String> getAllGuildIds(@Nullable String tablePrefix) throws IOException;

    List<Guild> getAllGuilds(@Nullable String tablePrefix) throws IOException;

    /** Returns null when no guild has that id, so callers have to null-check. */
    Guild getGuild(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /** Stores a new guild; data is the guild serialised by Gson. */
    void createGuild(@Nullable String tablePrefix, String id, String data) throws IOException;

    /** Replaces an existing guild; data is the guild serialised by Gson. */
    void updateGuild(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException;

    void deleteGuild(@Nullable String tablePrefix, @NotNull String id) throws IOException;
}
