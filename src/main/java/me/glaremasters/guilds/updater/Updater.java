package me.glaremasters.guilds.updater;

import com.google.gson.Gson;
import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.database.Callback;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class Updater {

    private static final String USER_AGENT = "Guilds_Update_Checker";
    private static final String API_URL = "https://api.spiget.org/v2/resources";
    private static final String SPIGOT_URL = "https://www.spigotmc.org/resources";
    private static int RESOURCE_ID = 13388;
    private static Gson gson = new Gson();

    public static void checkForUpdates(Callback<String, Exception> callback) {
        Main.newChain().asyncFirst(Updater::getLatestVersion).syncLast(latestVersion -> {
            long creationTime = Main.getCreationTime();
            callback.call((creationTime != 0 && latestVersion.getReleaseDate() > creationTime) ?
                    String.format("%s/%s/download?version=%s", SPIGOT_URL, RESOURCE_ID,
                            latestVersion.getId()) :
                    null, null);
        }).execute();
    }

    private static UpdateResponse getLatestVersion() {
        try {
            URL url = new URL(
                    String.format("%s/%s/versions?size=1&sort=-releaseDate", API_URL, RESOURCE_ID));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", USER_AGENT);

            InputStream stream = connection.getInputStream();

            UpdateResponse[] result =
                    gson.fromJson(IOUtils.toString(stream, Charset.defaultCharset()),
                            UpdateResponse[].class);
            return result[0];
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
