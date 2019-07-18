package me.glaremasters.guilds.challenges.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
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
        return null;
    }
}
