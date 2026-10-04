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
