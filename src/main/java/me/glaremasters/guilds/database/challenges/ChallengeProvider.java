package me.glaremasters.guilds.database.challenges;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;

public interface ChallengeProvider {

    /**
     * Creates the container that will hold challenges
     * @param tablePrefix the prefix, if any, to use
     * @throws IOException
     */
    void createContainer(@Nullable String tablePrefix) throws IOException;

    /**
     * Saves a new challenge to the database
     * @param tablePrefix the prefix, if any, to use
     * @param id the id of the new challenge
     * @param data the data of the new challenge
     * @throws IOException
     */
    void createChallenge(@Nullable String tablePrefix, String id, String data) throws  IOException;

    /**
     * Deletes a challenge from the database
     * @param tablePrefix the prefix, if any, to use
     * @param id the challenge id to delete
     * @throws IOException
     */
    void deleteChallenge(@Nullable String tablePrefix, @NotNull String id) throws IOException;

}
