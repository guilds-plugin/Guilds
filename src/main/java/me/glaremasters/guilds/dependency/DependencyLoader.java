/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package me.glaremasters.guilds.dependency;

import me.glaremasters.guilds.Guilds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

public final class DependencyLoader {
    private static final Method ADD_URL_METHOD;

    static {
        try {
            ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            ADD_URL_METHOD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadAll(Class<?> clazz) {
        MavenLibrary[] libs = clazz.getDeclaredAnnotationsByType(MavenLibrary.class);

        if (libs != null) {
            for (MavenLibrary lib : libs) {
                load(new Dependency(lib.groupId(), lib.artifactId(), lib.version(), lib.repo()));
            }
        }
    }

    @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
    private static void load(Dependency d) {
        Guilds plugin = (Guilds) Guilds.getProvidingPlugin(Guilds.class);
        Logger logger = plugin.getLogger();
        String name = d.getArtifactId() + "-" + d.getVersion();
        File lib = new File(plugin.getDataFolder() + "/libs/", name + ".jar");

        logger.info(String.format("Loading dependency %s:%s:%s from %s", d.getGroupId(), d.getArtifactId(), d.getVersion(), d.getRepoUrl()));

        if (!lib.exists()) {
            lib.getParentFile().mkdirs();

            try {
                logger.info("Dependency " + name + " is not currently downloaded, downloading now.");
                URL url = d.getUrl();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

                try (InputStream is = connection.getInputStream()) {
                    FileOutputStream out = new FileOutputStream(lib);
                    copy(is, out, 1024);
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!lib.exists()) {
            throw new RuntimeException("Unable to download dependency: " + d.toString());
        }

        URLClassLoader loader = (URLClassLoader) plugin.getClass().getClassLoader();
        try {
            ADD_URL_METHOD.invoke(loader, lib.toURI().toURL());
        } catch (Exception e) {
            throw new RuntimeException("Unable to load dependency: " + d.toString());
        }

        logger.info("Successfully loaded dependency " + name);
    }

    public static void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int n = input.read(buf);
        while (n >= 0) {
            output.write(buf, 0, n);
            n = input.read(buf);
        }
        output.flush();
    }

    private static final class Dependency {
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String repoUrl;

        private Dependency(String groupId, String artifactId, String version, String repoUrl) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.repoUrl = repoUrl;
        }

        String getGroupId() {
            return groupId;
        }

        String getArtifactId() {
            return artifactId;
        }

        String getVersion() {
            return version;
        }

        String getRepoUrl() {
            return repoUrl;
        }

        URL getUrl() {
            String repo = repoUrl;

            if (!repo.endsWith("/")) {
                repo += "/";
            }

            repo += "%s/%s/%s/%s-%s.jar";

            try {
                return new URL(String.format(repo, groupId.replace(".", "/"), artifactId, version, artifactId, version));
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}