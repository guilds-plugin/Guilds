package me.glaremasters.guilds.database;

import me.glaremasters.guilds.database.challenges.ChallengeProvider;
import me.glaremasters.guilds.database.challenges.provider.ChallengeJsonProvider;
import me.glaremasters.guilds.database.challenges.provider.ChallengeMySQLProvider;
import me.glaremasters.guilds.database.challenges.provider.ChallengeSQLiteProvider;
import me.glaremasters.guilds.database.guild.GuildProvider;
import me.glaremasters.guilds.database.guild.provider.GuildJsonProvider;
import me.glaremasters.guilds.database.guild.provider.GuildMySQLProvider;
import me.glaremasters.guilds.database.guild.provider.GuildSQLiteProvider;

import java.util.Arrays;

public enum DatabaseBackend {
    JSON("json", GuildJsonProvider.class, ChallengeJsonProvider.class),
    MYSQL("mysql", GuildMySQLProvider.class, ChallengeMySQLProvider.class),
    SQLITE("sqlite", GuildSQLiteProvider.class, ChallengeSQLiteProvider.class);

    private final String backendName;
    private final Class<? extends GuildProvider> guildProvider;

    private final Class<? extends ChallengeProvider> challengeProvider;

    DatabaseBackend(String backendName, Class<? extends GuildProvider> guildProvider, Class<? extends ChallengeProvider> challengeProvider) {
        this.backendName = backendName;
        this.guildProvider = guildProvider;
        this.challengeProvider = challengeProvider;
    }

    public String getBackendName() {
        return backendName;
    }

    public Class<? extends GuildProvider> getGuildProvider() {
        return guildProvider;
    }

    public Class<? extends ChallengeProvider> getChallengeProvider() {
        return challengeProvider;
    }

    public static DatabaseBackend getByBackendName(String backendName) {
        return Arrays.stream(values()).filter(n -> n.backendName.equals(backendName.toLowerCase())).findFirst().orElse(null);
    }
}
