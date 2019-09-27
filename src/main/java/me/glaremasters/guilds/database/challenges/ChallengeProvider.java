package me.glaremasters.guilds.database.challenges;

import me.glaremasters.guilds.guild.GuildChallenge;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public interface ChallengeProvider {

    /**
     * Creates the container that will hold challenges
     * @param tablePrefix the prefix, if any, to use
     * @throws IOException
     */
    void createContainer(@Nullable String tablePrefix) throws IOException;

    /**
     * Gets all challenges from the database
     * @param tablePrefix the prefix, if any, to use
     * @return a list of challenges
     */
    List<GuildChallenge> getAllChallenges(@Nullable String tablePrefix) throws IOException;

    /**
     * Checks whether or not a guild with the specified id exists
     * @param id the guild id
     * @return true or false
     * @throws IOException
     */
    boolean challengeExists(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /**
     * Gets a single challenge by id
     * @param tablePrefix the prefix, if any, to use
     * @param id the id of the challenge to load
     * @return the found challenge or null
     * @throws IOException
     */
    GuildChallenge getChallenge(@Nullable String tablePrefix, @NotNull String id) throws IOException;

    /**
     * Saves a new challenge to the database
     * @param tablePrefix the prefix, if any, to use
     * @param id the id of the new challenge
     * @param data the data of the new challenge
     * @throws IOException
     */
    void createChallenge(@Nullable String tablePrefix, String id, String data) throws  IOException;

    /**
     * Updates a challenge in the database
     * @param tablePrefix the prefix, if any, to use
     * @param id the id of the challenge to update
     * @param data the updated data of the challenge
     * @throws IOException
     */
    void updateChallenge(@Nullable String tablePrefix, @NotNull String id, @NotNull String data) throws IOException;

    /**
     * Deletes a challenge from the database
     * @param tablePrefix the prefix, if any, to use
     * @param id the challenge id to delete
     * @throws IOException
     */
    void deleteChallenge(@Nullable String tablePrefix, @NotNull String id) throws IOException;

}
