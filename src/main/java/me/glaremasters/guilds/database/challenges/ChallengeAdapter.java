package me.glaremasters.guilds.database.challenges;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.database.challenges.provider.ChallengeJsonProvider;
import me.glaremasters.guilds.guild.GuildChallenge;
import me.glaremasters.guilds.utils.LoggingUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ChallengeAdapter {
    private final ChallengeProvider provider;
    private String sqlTablePrefix;

    public ChallengeAdapter(Guilds guilds, DatabaseAdapter adapter) {
        DatabaseBackend backend = adapter.getBackend();
        switch (backend) {
            default:
            case JSON:
                File fileDataFolder = new File(guilds.getDataFolder(), "challenges");
                provider = new ChallengeJsonProvider(fileDataFolder);
                break;
            case MYSQL:
            case SQLITE:
            case MARIADB:
                sqlTablePrefix = adapter.getSqlTablePrefix();
                provider = adapter.getDatabaseManager().getJdbi().onDemand(backend.getChallengeProvider());
                break;
        }
    }

   public void createContainer() throws IOException {
        provider.createContainer(sqlTablePrefix);
   }

    public boolean challengeExists(@NotNull String id) throws IOException {
        return provider.challengeExists(sqlTablePrefix, id);
    }

   public Set<GuildChallenge> getAllChallenges() throws IOException {
        return provider.getAllChallenges(sqlTablePrefix);
   }

   public GuildChallenge getChallenge(@NotNull String id) throws IOException {
        return provider.getChallenge(sqlTablePrefix, id);
   }

   /**
    * Saves every challenge, one record at a time.
    *
    * <p>A record that fails to save is logged with its id and the cause, then the batch carries on.
    * Previously the first failure propagated out of the loop and every challenge after it was never
    * written. Summarised at the end rather than rethrown, since rethrowing is what abandoned the
    * rest of the batch.
    *
    * @param challenges the challenges to save
    * @throws IOException retained for source compatibility; a single failed record no longer aborts
    *         the batch
    */
   public void saveChallenges(@NotNull Set<GuildChallenge> challenges) throws IOException {
       int failed = 0;

       for (GuildChallenge challenge : challenges) {
           try {
               saveChallenge(challenge);
           } catch (IOException | RuntimeException e) {
               failed++;
               LoggingUtils.warn("Failed to save challenge " + challenge.getId()
                       + "; the other challenges are still being saved.", e);
           }
       }

       if (failed > 0) {
           LoggingUtils.severe(failed + " of " + challenges.size()
                   + " challenges failed to save. See the warnings above.");
       }
   }

   public void saveChallenge(@NotNull GuildChallenge challenge) throws IOException {
       if (!challengeExists(challenge.getId().toString())) {
           createChallenge(challenge);
       } else {
           updateChallenge(challenge);
       }
   }

   public void createChallenge(@NotNull GuildChallenge challenge) throws IOException {
        provider.createChallenge(sqlTablePrefix, challenge.getId().toString(), Guilds.getGson().toJson(challenge, GuildChallenge.class));
   }

    public void updateChallenge(@NotNull GuildChallenge challenge) throws IOException {
        provider.updateChallenge(sqlTablePrefix, challenge.getId().toString(), Guilds.getGson().toJson(challenge, GuildChallenge.class));
    }

   public void deleteChallenge(@NotNull String id) throws IOException {
        provider.deleteChallenge(sqlTablePrefix, id);
   }
}
