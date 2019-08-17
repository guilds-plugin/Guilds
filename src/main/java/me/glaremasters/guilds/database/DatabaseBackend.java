package me.glaremasters.guilds.database;

import me.glaremasters.guilds.database.arenas.ArenaProvider;
import me.glaremasters.guilds.database.arenas.provider.ArenaJsonProvider;
import me.glaremasters.guilds.database.arenas.provider.ArenaMySQLProvider;
import me.glaremasters.guilds.database.arenas.provider.ArenaSQLiteProvider;
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
    JSON("json", GuildJsonProvider.class, ChallengeJsonProvider.class, ArenaJsonProvider.class),
    MYSQL("mysql", GuildMySQLProvider.class, ChallengeMySQLProvider.class, ArenaMySQLProvider.class),
    SQLITE("sqlite", GuildSQLiteProvider.class, ChallengeSQLiteProvider.class, ArenaSQLiteProvider.class);

    private final String backendName;
    private final Class<? extends GuildProvider> guildProvider;
    private final Class<? extends ChallengeProvider> challengeProvider;
    private final Class<? extends ArenaProvider> arenaProvider;

    DatabaseBackend(String backendName, Class<? extends GuildProvider> guildProvider, Class<? extends ChallengeProvider> challengeProvider, Class<? extends ArenaProvider> arenaProvider) {
        this.backendName = backendName;
        this.guildProvider = guildProvider;
        this.challengeProvider = challengeProvider;
        this.arenaProvider = arenaProvider;
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

    public Class<? extends ArenaProvider> getArenaProvider() {
        return arenaProvider;
    }

    public static DatabaseBackend getByBackendName(String backendName) {
        return Arrays.stream(values()).filter(n -> n.backendName.equals(backendName.toLowerCase())).findFirst().orElse(null);
    }
}
