package me.glaremasters.guilds.database.databases.json.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import me.glaremasters.guilds.guild.Guild;

public class GuildMapDeserializer implements JsonDeserializer<Map<String, Guild>> {

    @Override
    public Map<String, Guild> deserialize(JsonElement json, Type type,
            JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();

        Map<String, Guild> guilds = new HashMap<>();
        object.entrySet().forEach(entry -> {
            JsonObject guild = entry.getValue().getAsJsonObject();
            guild.addProperty("name", entry.getKey());

            guilds.put(entry.getKey(), context.deserialize(guild, Guild.class));
        });

        return guilds;
    }
}
