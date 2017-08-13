package me.glaremasters.guilds.database.databases.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.database.Callback;
import me.glaremasters.guilds.database.DatabaseProvider;
import me.glaremasters.guilds.database.databases.json.deserializer.GuildMapDeserializer;
import me.glaremasters.guilds.database.databases.json.deserializer.LeaderboardListDeserializer;
import me.glaremasters.guilds.database.databases.json.serializer.LeaderboardListSerializer;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.leaderboard.Leaderboard;
import org.bukkit.Location;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO handle exceptions
public class Json implements DatabaseProvider {

    private Gson gson;
    private File guildsFile;
    private File leaderboardsFile;

    private Type guildsType;
    private Type leaderboardsType;

    @Override
    public void initialize() {
        File folder = new File(Main.getInstance().getDataFolder(), "data/");
        guildsFile = new File(folder, "guilds.json");
        leaderboardsFile = new File(folder, "leaderboards.json");

        guildsType = new TypeToken<Map<String, Guild>>() {
        }.getType();
        leaderboardsType = new TypeToken<ArrayList<Leaderboard>>() {
        }.getType();

        gson = new GsonBuilder().registerTypeAdapter(guildsType, new GuildMapDeserializer())
                .registerTypeAdapter(leaderboardsType, new LeaderboardListDeserializer())
                .registerTypeAdapter(leaderboardsType, new LeaderboardListSerializer())
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

        if (!leaderboardsFile.exists()) {
            try {
                if (!leaderboardsFile.createNewFile()) {
                    throw new IOException(
                            "Something went wrong when creating the leaderboard storage file!");
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

        Main.newChain().asyncFirst(() -> write(guildsFile, guilds, guildsType))
                .syncLast(successful -> callback.call(successful, null)).execute((exception, task) -> {
            if (exception != null) {
                callback.call(false, exception);
            }
        });

        Main.getInstance().getGuildHandler().addGuild(guild);
    }

    @Override
    public void removeGuild(Guild guild, Callback<Boolean, Exception> callback) {
        HashMap<String, Guild> guilds = getGuilds();

        if (guilds == null || !guilds.keySet().contains(guild.getName())) {
            return;
        }

        guilds.remove(guild.getName());

        Main.newChain().asyncFirst(() -> write(guildsFile, guilds, guildsType))
                .syncLast(successful -> callback.call(successful, null)).execute();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void getGuilds(Callback<HashMap<String, Guild>, Exception> callback) {
        Main.newChain().asyncFirst(() -> {
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

        Main.newChain().asyncFirst(() -> write(guildsFile, guilds, guildsType))
                .syncLast(successful -> callback.call(successful, null)).execute();
    }

    @Override
    public void updatePrefix(Guild guild, Callback<Boolean, Exception> callback) {
        HashMap<String, Guild> guilds = getGuilds();
        guilds.put(guild.getName(), guild);

        Main.newChain().asyncFirst(() -> write(guildsFile, guilds, guildsType))
                .syncLast(successful -> callback.call(successful, null)).execute();
    }

    @Override
    public void addAlly(Guild guild, Guild targetGuild, Callback<Boolean, Exception> callback) {
        //TODO: Necessary for json?
    }

    @Override
    public void setHome(Guild guild, Location homeLocation, Callback<Boolean, Exception> callback) {

    }

    @Override
    public void getHome(Guild guild, Callback<Location, Exception> callback) {

    }

    @Override
    public void createLeaderboard(Leaderboard leaderboard, Callback<Boolean, Exception> callback) {
        List<Leaderboard> leaderboards =
                getLeaderboards() == null ? new ArrayList<>() : getLeaderboards();
        leaderboards.add(leaderboard);

        Main.newSharedChain(leaderboardsFile.getName()).asyncFirst(() -> {
            boolean toReturn = write(leaderboardsFile, leaderboards, leaderboardsType);
            Main.getInstance().getLeaderboardHandler().addLeaderboard(leaderboard);

            return toReturn;
        }).syncLast(successful -> callback.call(successful, null)).execute((exception, task) -> {
            if (exception != null) {
                callback.call(false, exception);
            }
        });

        //Writes 2x because of this, moved inside of asyncFirst which seems to solve the issue
        //Main.getInstance().getLeaderboardHandler().addLeaderboard(leaderboard);
    }

    @Override
    public void removeLeaderboard(Leaderboard leaderboard, Callback<Boolean, Exception> callback) {
        List<Leaderboard> leaderboards = getLeaderboards();

        if (leaderboard == null || !leaderboards.contains(leaderboard)) {
            return;
        }

        leaderboards.remove(leaderboard);

        Main.newChain().asyncFirst(() -> write(guildsFile, leaderboards, leaderboardsType))
                .syncLast(successful -> callback.call(successful, null)).execute();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void getLeaderboards(Callback<List<Leaderboard>, Exception> callback) {
        Main.newChain().asyncFirst(() -> {
            JsonReader reader;
            try {
                reader = new JsonReader(new FileReader(leaderboardsFile));
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                return null;
            }

            return gson.fromJson(reader, leaderboardsType);
        }).syncLast(leaderboards -> callback.call((ArrayList<Leaderboard>) leaderboards, null))
                .execute();
    }

    @Override
    public void updateLeaderboard(Leaderboard leaderboard, Callback<Boolean, Exception> callback) {
        List<Leaderboard> leaderboards = getLeaderboards();
        leaderboards.remove(leaderboards.stream().filter(
                l -> l.getName().equals(leaderboard.getName()) && l.getLeaderboardType() == leaderboard
                        .getLeaderboardType()).findFirst().orElse(null));
        leaderboards.add(leaderboard);

        Main.newChain().asyncFirst(() -> write(leaderboardsFile, leaderboards, leaderboardsType))
                .syncLast(successful -> callback.call(successful, null)).execute();
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
        return Main.getInstance().getGuildHandler().getGuilds();
    }

    private List<Leaderboard> getLeaderboards() {
        return Main.getInstance().getLeaderboardHandler().getLeaderboards();
    }
}
