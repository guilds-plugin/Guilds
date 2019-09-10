package me.glaremasters.guilds.database.challenges;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.DatabaseAdapter;
import me.glaremasters.guilds.database.DatabaseBackend;
import me.glaremasters.guilds.database.challenges.provider.ChallengeJsonProvider;
import me.glaremasters.guilds.guild.GuildChallenge;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

   public List<GuildChallenge> getAllChallenges() throws IOException {
        return provider.getAllChallenges(sqlTablePrefix);
   }

   public GuildChallenge getChallenge(@NotNull String id) throws IOException {
        return provider.getChallenge(sqlTablePrefix, id);
   }

   public void saveChallenges(@NotNull List<GuildChallenge> challenges) throws IOException {
        for (GuildChallenge challenge : challenges) {
            saveChallenge(challenge);
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
