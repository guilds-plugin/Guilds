/*
 * MIT License
 *
 * Copyright (c) 2018 Glare
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
