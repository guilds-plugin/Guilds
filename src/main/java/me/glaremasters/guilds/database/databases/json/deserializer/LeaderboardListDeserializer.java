package me.glaremasters.guilds.database.databases.json.deserializer;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import me.glaremasters.guilds.leaderboard.Leaderboard;
import me.glaremasters.guilds.leaderboard.Score;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardListDeserializer implements JsonDeserializer<List<Leaderboard>> {

    @Override public List<Leaderboard> deserialize(JsonElement json, Type type,
        JsonDeserializationContext context) throws JsonParseException {
        JsonArray object = json.getAsJsonArray();

        List<Leaderboard> leaderboards = new ArrayList<>();
        object.forEach(obj -> {
            JsonObject leaderboard = obj.getAsJsonObject();

            List<Score> scores =
                context.deserialize(leaderboard.get("scores"), new TypeToken<ArrayList<Score>>() {
                }.getType());

            leaderboards.add(new Leaderboard(leaderboard.get("name").getAsString(),
                Leaderboard.LeaderboardType
                    .valueOf(leaderboard.get("leaderboardType").getAsString()),
                Leaderboard.SortType.valueOf(leaderboard.get("sortType").getAsString()), scores) {
            });
        });

        return leaderboards;
    }
}
