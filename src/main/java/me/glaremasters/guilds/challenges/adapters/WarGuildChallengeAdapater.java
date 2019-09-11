package me.glaremasters.guilds.challenges.adapters;

import com.github.stefvanschie.inventoryframework.Gui;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.api.GuildsAPI;
import me.glaremasters.guilds.guild.Guild;

import java.io.IOException;

public class WarGuildChallengeAdapater extends TypeAdapter<Guild> {

    @Override
    public void write(JsonWriter out, Guild guild) throws IOException {
        out.beginObject();
        out.name("uuid");
        out.value(guild.getId().toString());
        out.endObject();
    }

    @Override
    public Guild read(JsonReader in) throws IOException {
        JsonElement element = new JsonParser().parse(in);

        GuildsAPI api = Guilds.getApi();
        if (api == null) {
            throw new IllegalStateException("WarGuildChallengeAdapter attempted to access the API before the API was created. " +
                    "This is probably a race condition.");
        }

        return api.getGuild(element.getAsJsonObject().get("uuid").getAsString());
    }
}
