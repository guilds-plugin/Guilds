/*
 *
 *  * MIT License
 *  *
 *  * Copyright (c) 2018-2019 Guilds - Adding RPG to your server has never been more fun and action-packed!
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package me.glaremasters.guilds.database.migration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildMember;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.guild.GuildTier;
import net.reflxction.simplejson.json.JsonFile;
import net.reflxction.simplejson.json.JsonReader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class MigrationManager {

    private Guilds guilds;

    public void checkOld(List<Guild> oldGuilds) throws IOException {
        File file = new File(guilds.getDataFolder(), "data/guilds.json");
        File config = new File(guilds.getDataFolder(), "config.yml");
        File languages = new File(guilds.getDataFolder(), "languages");
        if (file.exists()) {
            guilds.warn("Old data found.... attempting to convert!");
            JsonFile jsonFile = new JsonFile(file);
            JsonReader reader = new JsonReader(jsonFile);
            Set<Map.Entry<String, JsonElement>> guilds = reader.getJsonObject().entrySet();
            guilds.forEach(s -> {
                JsonObject guild = s.getValue().getAsJsonObject();
                Guild.GuildBuilder gb = Guild.builder();
                // New ID
                gb.id(UUID.randomUUID());
                // Set the name
                gb.name(s.getKey());
                // Set the prefix
                gb.prefix(guild.get("prefix").getAsString());
                // Defult back to Private
                gb.status(Guild.Status.Private);
                // Check home
                gb.home(null);
                // Set master
                gb.guildMaster(findOldMaster(guild, "members"));
                // Set the balance
                gb.balance(guild.get("balance").getAsDouble());
                // Check members
                gb.members(handleMembers(guild, "members"));
                // Set the vault
                gb.vaults(new ArrayList<>());
                // Set ally
                gb.allies(handleArrays(guild, "allies"));
                // Set invited members
                gb.invitedMembers(handleArrays(guild, "invitedMembers"));
                // Set pending allies
                gb.pendingAllies(handleArrays(guild, "pendingAllies"));
                // Set the tier
                gb.tier(GuildTier.builder().level(guild.get("tier").getAsInt()).build());
                // Set the codes
                gb.codes(new ArrayList<>());
                oldGuilds.add(gb.build());
            });
            reader.close();
            file.delete();
            config.delete();
            FileUtils.forceDelete(languages);
            this.guilds.warn("Converting completed!");
        }
    }

    public List<UUID> handleArrays(JsonObject guild, String key) {
        Type listType = new TypeToken<List<UUID>>() {}.getType();
        return new Gson().fromJson(guild.get(key), listType);
    }

    public GuildMember findOldMaster(JsonObject guild, String key) {
        List<GuildMember> members = handleMembers(guild, key);
        List<GuildMember> s = members.stream().filter(p -> p.getRole().getLevel() == 0).collect(Collectors.toList());
        return s.get(0);
    }

    public List<GuildMember> handleMembers(JsonObject guild, String key) {
        List<GuildMember> members = new ArrayList<>();
        JsonArray array = guild.get(key).getAsJsonArray();
        array.forEach(g -> {
            JsonObject obj = g.getAsJsonObject();
            UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
            members.add(new GuildMember(uuid, GuildRole.builder().level(obj.get("role").getAsInt()).build()));
        });
        return members;
    }


}
