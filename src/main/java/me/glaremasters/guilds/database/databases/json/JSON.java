package me.glaremasters.guilds.database.databases.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.GsonBuilder;
import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.database.Callback;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.guild.Guild;

import java.io.File;
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
        H
    }


    private HashMap<String, Guild> getGuilds() {
        return guilds.getGuildHandler().getGuilds();
    }

}
