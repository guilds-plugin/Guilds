package me.glaremasters.guilds.database.databases.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.Callback;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.database.databases.json.deserializer.GuildMapDeserializer;
import me.glaremasters.guilds.guild.Guild;

//TODO handle exceptions
public class Json implements DatabaseProvider {

    private Gson gson;
    private File guildsFile;

    private Type guildsType;

    @Override
    public void initialize() {
        File folder = new File(Guilds.getInstance().getDataFolder(), "data/");
        guildsFile = new File(folder, "guilds.json");

        guildsType = new TypeToken<Map<String, Guild>>() {
        }.getType();

        gson = new GsonBuilder().registerTypeAdapter(guildsType, new GuildMapDeserializer())
                .excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (!guildsFile.exists()) {
            try {
                if (!guildsFile.createNewFile()) {
                    throw new IOException(
                            "Something went wrong when creating the guild storage file!");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void createGuild(Guild guild, Callback<Boolean, Exception> callback) {
        HashMap<String, Guild> guilds = getGuilds() == null ? new HashMap<>() : getGuilds();
        guilds.put(guild.getName(), guild);

        Guilds.newChain().asyncFirst(() -> write(guildsFile, guilds, guildsType))
                .syncLast(successful -> callback.call(successful, null))
                .execute((exception, task) -> {
                    if (exception != null) {
                        callback.call(false, exception);
                    }
                });

        Guilds.getInstance().getGuildHandler().addGuild(guild);
    }

    @Override
    public void removeGuild(Guild guild, Callback<Boolean, Exception> callback) {
        HashMap<String, Guild> guilds = getGuilds();

        if (guilds == null || !guilds.keySet().contains(guild.getName())) {
            return;
        }

        guilds.remove(guild.getName());

        Guilds.newChain().asyncFirst(() -> write(guildsFile, guilds, guildsType))
                .syncLast(successful -> callback.call(successful, null)).execute();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void getGuilds(Callback<HashMap<String, Guild>, Exception> callback) {
        Guilds.newChain().asyncFirst(() -> {
            JsonReader reader;
            try {
                reader = new JsonReader(new FileReader(guildsFile));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                return null;
            }

            return gson.fromJson(reader, guildsType);
        }).syncLast(guilds -> callback.call((HashMap<String, Guild>) guilds, null)).execute();
    }

    @Override
    public void updateGuild(Guild guild, Callback<Boolean, Exception> callback) {
        HashMap<String, Guild> guilds = getGuilds();
        guilds.put(guild.getName(), guild);

        Guilds.newChain().asyncFirst(() -> write(guildsFile, guilds, guildsType))
                .syncLast(successful -> callback.call(successful, null)).execute();
    }

    @Override
    public void updatePrefix(Guild guild, Callback<Boolean, Exception> callback) {
        HashMap<String, Guild> guilds = getGuilds();
        guilds.put(guild.getName(), guild);

        Guilds.newChain().asyncFirst(() -> write(guildsFile, guilds, guildsType))
                .syncLast(successful -> callback.call(successful, null)).execute();
    }

    @Override
    public void addAlly(Guild guild, Guild targetGuild, Callback<Boolean, Exception> callback) {
        //TODO: Necessary for json?
    }

    @Override
    public void removeAlly(Guild guild, Guild targetguild, Callback<Boolean, Exception> callback) {

    }


    private boolean write(File file, Object toWrite, Type typeOfSrc) {
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(toWrite, typeOfSrc, writer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private HashMap<String, Guild> getGuilds() {
        return Guilds.getInstance().getGuildHandler().getGuilds();
    }


}
