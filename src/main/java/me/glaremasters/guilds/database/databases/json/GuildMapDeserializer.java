package me.glaremasters.guilds.database.databases.json;

import com.google.gson.*;
import me.glaremasters.guilds.guild.Guild;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 11:44 AM
 */
public class GuildMapDeserializer implements JsonDeserializer<Map<String, Guild>> {

    @Override
    public Map<String, Guild> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        Map<String, Guild> guilds = new HashMap<>();
        obj.entrySet().forEach(entry -> {
            JsonObject guild = entry.getValue().getAsJsonObject();
            guild.addProperty("name", entry.getKey());
            guilds.put(entry.getKey(), context.deserialize(guild, Guild.class));
        });
        return guilds;
    }

}
