/*
 * MIT License
 *
 * Copyright (c) 2023 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.glaremasters.guilds.database;

import me.glaremasters.guilds.database.arenas.ArenaProvider;
import me.glaremasters.guilds.database.arenas.provider.ArenaJsonProvider;
import me.glaremasters.guilds.database.arenas.provider.ArenaMariaDBProvider;
import me.glaremasters.guilds.database.arenas.provider.ArenaMySQLProvider;
import me.glaremasters.guilds.database.arenas.provider.ArenaSQLiteProvider;
import me.glaremasters.guilds.database.challenges.ChallengeProvider;
import me.glaremasters.guilds.database.challenges.provider.ChallengeJsonProvider;
import me.glaremasters.guilds.database.challenges.provider.ChallengeMariaDBProvider;
import me.glaremasters.guilds.database.challenges.provider.ChallengeMySQLProvider;
import me.glaremasters.guilds.database.challenges.provider.ChallengeSQLiteProvider;
import me.glaremasters.guilds.database.cooldowns.CooldownProvider;
import me.glaremasters.guilds.database.cooldowns.provider.CooldownJsonProvider;
import me.glaremasters.guilds.database.cooldowns.provider.CooldownMariaDBProvider;
import me.glaremasters.guilds.database.cooldowns.provider.CooldownMySQLProvider;
import me.glaremasters.guilds.database.cooldowns.provider.CooldownSQLiteProvider;
import me.glaremasters.guilds.database.guild.GuildProvider;
import me.glaremasters.guilds.database.guild.provider.GuildJsonProvider;
import me.glaremasters.guilds.database.guild.provider.GuildMariaDBProvider;
import me.glaremasters.guilds.database.guild.provider.GuildMySQLProvider;
import me.glaremasters.guilds.database.guild.provider.GuildSQLiteProvider;

import java.util.Arrays;

public enum DatabaseBackend {
    JSON("json", GuildJsonProvider.class, ChallengeJsonProvider.class, ArenaJsonProvider.class, CooldownJsonProvider.class),
    MYSQL("mysql", GuildMySQLProvider.class, ChallengeMySQLProvider.class, ArenaMySQLProvider.class, CooldownMySQLProvider.class),
    SQLITE("sqlite", GuildSQLiteProvider.class, ChallengeSQLiteProvider.class, ArenaSQLiteProvider.class, CooldownSQLiteProvider.class),
    MARIADB("mariadb", GuildMariaDBProvider.class, ChallengeMariaDBProvider.class, ArenaMariaDBProvider.class, CooldownMariaDBProvider.class);
    private final String backendName;
    private final Class<? extends GuildProvider> guildProvider;
    private final Class<? extends ChallengeProvider> challengeProvider;
    private final Class<? extends ArenaProvider> arenaProvider;
    private final Class<? extends CooldownProvider> cooldownProvider;

    DatabaseBackend(String backendName, Class<? extends GuildProvider> guildProvider, Class<? extends ChallengeProvider> challengeProvider, Class<? extends ArenaProvider> arenaProvider, Class<? extends CooldownProvider> cooldownProvider) {
        this.backendName = backendName;
        this.guildProvider = guildProvider;
        this.challengeProvider = challengeProvider;
        this.arenaProvider = arenaProvider;
        this.cooldownProvider = cooldownProvider;
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

    public Class<? extends CooldownProvider> getCooldownProvider() {
        return cooldownProvider;
    }

    public static DatabaseBackend getByBackendName(String backendName) {
        return Arrays.stream(values()).filter(n -> n.backendName.equals(backendName.toLowerCase())).findFirst().orElse(null);
    }
}
