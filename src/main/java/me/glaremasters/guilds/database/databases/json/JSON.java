package me.glaremasters.guilds.database.databases.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.Callback;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.guild.Guild;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by GlareMasters
 * Date: 7/18/2018
 * Time: 11:38 AM
 */
public class JSON implements DatabaseProvider {

    private Gson gson;
    private File guildsFile, folder;
    private Type guildsType;

    private Guilds guilds;

    public JSON(Guilds guilds) {
        this.guilds = guilds;
    }

    @Override
    public void initialize() {

        folder = new File(guilds.getDataFolder(), "data/");
        guildsFile = new File(folder, "guild.json");
        guildsType = new TypeToken<Map<String, Guild>>() {
        }.getType();

        gson = new GsonBuilder().registerTypeAdapter(guildsType, new GuildMapDeserializer()).excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

        if (!folder.exists()) folder.mkdirs();

        if (!guildsFile.exists()) {
            try {
                if (!guildsFile.createNewFile()) {
                    throw new IOException("Error");
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
        write(guildsFile, guilds, guildsType);
        Guilds.getGuilds().getGuildHandler().addGuild(guild);
    }

    @Override
    public void removeGuild(Guild guild) {
        HashMap<String, Guild> guilds = getGuilds();
        if (guilds == null || !guilds.keySet().contains(guild.getName())) return;
        guilds.remove(guild.getName());
        write(guildsFile, guilds, guildsType);
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
    public void updateGuild(Guild guild) {
        HashMap<String, Guild> guilds = getGuilds();
        guilds.put(guild.getName(), guild);
        write(guildsFile, guilds, guildsType);
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
        return guilds.getGuildHandler().getGuilds();
    }

}
