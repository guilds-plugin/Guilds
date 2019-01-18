package me.glaremasters.guilds.updater;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by GlareMasters
 * Date: 7/19/2018
 * Time: 1:19 PM
 */
public class SpigotUpdater {

    //todo rewrite
    //this is done so oddly, you have unused methods, conflicting variables etc unnecessary method calls etc

    private int project;
    private URL checkURL;
    private String version;

    /**
     * The main part of the SpigotUpdater
     * @param projectID the ID of the spigot project
     */
    public SpigotUpdater(String version, int projectID) {
        this.version = version;
        this.project = projectID;

        try {
            this.checkURL = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + projectID);
        } catch (MalformedURLException ex) {
            System.out.println("Could not check for plugin update.");
        }
    }

    /**
     * Check for the latest version of the plugin
     * @return the latest version of the plugin
     * @throws Exception the exception
     */
    public String getLatestVersion() throws Exception {
        URLConnection con = checkURL.openConnection();
        this.version = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
        return version;
    }

    /**
     * Get the URL of the plugin
     * @return URL of plugin
     */
    public String getResourceURL() {
        return "https://www.spigotmc.org/resources/" + project;
    }

    /**
     * Check for updates
     * @return if plugin version is the latest plugin version
     * @throws Exception I/O Exception
     */
    public boolean checkForUpdates() throws Exception {
        URLConnection con = checkURL.openConnection();
        this.version = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
        return !version.equals(version);
    }

}
