package me.glaremasters.guilds.database.databases.json.deserializer;

import com.google.gson.*;
import me.glaremasters.guilds.guild.Guild;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class GuildMapDeserializer implements JsonDeserializer<Map<String, Guild>> {

	@Override
	public Map<String, Guild> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
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
