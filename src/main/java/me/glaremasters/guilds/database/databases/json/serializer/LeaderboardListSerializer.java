package me.glaremasters.guilds.database.databases.json.serializer;

import com.google.gson.*;
import me.glaremasters.guilds.leaderboard.Leaderboard;

import java.lang.reflect.Type;
import java.util.List;

public class LeaderboardListSerializer implements JsonSerializer<List<Leaderboard>> {

    @Override
    public JsonElement serialize(List<Leaderboard> src, Type type,
                                 JsonSerializationContext context) {
        Gson gson = new Gson();
        JsonArray array = new JsonArray();

        for (Leaderboard leaderboard : src) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", leaderboard.getName());
            obj.addProperty("leaderboardType", leaderboard.getLeaderboardType().name());
            obj.addProperty("sortType", leaderboard.getSortType().name());
            obj.add("scores", gson.toJsonTree(leaderboard.getScores()));

            array.add(obj);
        }

        return array;
    }
}
