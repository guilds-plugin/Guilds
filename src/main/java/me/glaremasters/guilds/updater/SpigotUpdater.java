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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class SpigotUpdater {

    private int projectId;
    private URL spigotUrl;
    private String currentVersion;

    /**
     * SpigotUpdate constructor. Creates a new URL for future usage
     * @see SpigotUpdater#getLatestVersion() for usage
     * @param currentVersion the current plugin version
     * @param projectId the spigot project id
     * @throws MalformedURLException if url was malformed
     * @see URL
     */
    public SpigotUpdater(String currentVersion, int projectId) throws MalformedURLException {
        this.currentVersion = currentVersion;
        this.projectId = projectId;

        spigotUrl = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + projectId);
    }

    /**
     * Returns the latest version.
     * @return the latest version of the plugin
     * @throws IOException if an I/O Exception occurs
     * @see URL#openConnection()
     * @see URLConnection#getInputStream()
     */
    public String getLatestVersion() throws IOException {
        URLConnection con = spigotUrl.openConnection();
        return new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
    }

    /**
     * Get the resource link of the plugin
     * @return resource link of plugin
     */
    public String getResourceLink() {
        return "https://www.spigotmc.org/resources/" + projectId;
    }

    /**
     * Check for updates
     * @return if plugin version is the latest plugin version
     * @throws IOException if an I/O Exception occurs
     * @see SpigotUpdater#getLatestVersion()
     */
    public boolean checkForUpdates() throws IOException {
        return !currentVersion.equals(getLatestVersion());
    }

}
