package me.glaremasters.guilds.challenges.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import me.glaremasters.guilds.arena.Arena;

import java.io.IOException;
import java.util.UUID;

public class WarArenaChallengeAdapter extends TypeAdapter<Arena> {

    @Override
    public void write(JsonWriter out, Arena arena) throws IOException {
        out.beginObject();
        out.name("uuid");
        out.value(arena.getId().toString());
        out.name("name");
        out.value(arena.getName());
        out.endObject();
    }

    @Override
    public Arena read(JsonReader in) throws IOException {
        in.beginObject();
        in.nextName();
        String id = in.nextString();
        in.nextName();
        String name = in.nextString();
        in.endObject();
        return new Arena(UUID.fromString(id), name);
    }
}
