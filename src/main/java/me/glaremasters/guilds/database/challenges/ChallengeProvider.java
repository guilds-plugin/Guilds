package me.glaremasters.guilds.database.challenges;

import me.glaremasters.guilds.guild.GuildChallenge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Set;

/**
 * Stores guild challenges in whichever backend is configured.
 */
public interface ChallengeProvider {

    /** Creates the challenge table, or the challenge data folder on the JSON backend. */
    void createContainer(@Nullable String tablePrefix) throws IOException;

    Set<GuildChallenge> getAllChallenges(@Nullable String tablePrefix) throws IOException;

    boolean challengeExists(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /** Returns null when no challenge has that id, so callers have to null-check. */
    GuildChallenge getChallenge(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /** Stores a new challenge; data is the challenge serialised by Gson. */
    void createChallenge(@Nullable String tablePrefix, String id, String data) throws IOException;

    /** Replaces an existing challenge; data is the challenge serialised by Gson. */
    void updateChallenge(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException;

    void deleteChallenge(@Nullable String tablePrefix, @NotNull String id) throws IOException;

}
