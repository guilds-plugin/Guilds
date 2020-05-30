/*
 * MIT License
 *
 * Copyright (c) 2019 Glare
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

package me.glaremasters.guilds.dependency;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;

public class Libraries {

    public void loadSQL(BukkitLibraryManager loader) {
        loader.loadLibrary(Library.builder()
                .groupId("com.zaxxer")
                .artifactId("HikariCP")
                .version("3.3.1")
                .relocate("com{}zaxxer{}hikari", "me.glaremasters.guilds.libs.hikari")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("org{}jdbi")
                .artifactId("jdbi3-core")
                .version("3.8.2")
                .relocate("org{}jdbi", "me.glaremasters.guilds.libs.jdbi")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("org{}jdbi")
                .artifactId("jdbi3-sqlobject")
                .version("3.8.2")
                .relocate("org{}jdbi", "me.glaremasters.guilds.libs.jdbi")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("io.leangen.geantyref")
                .artifactId("geantyref")
                .version("1.3.7")
                .relocate("org{}jdbi", "me.glaremasters.guilds.libs.jdbi")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("org.antlr")
                .artifactId("antlr4-runtime")
                .version("4.7.2")
                .relocate("org{}jdbi", "me.glaremasters.guilds.libs.jdbi")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("org.slf4j")
                .artifactId("slf4j-api")
                .version("1.7.25")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("org{}mariadb{}jdbc")
                .artifactId("mariadb-java-client")
                .version("2.4.0")
                .relocate("org{}mariadb{}jdbc", "me.glaremasters.guilds.libs.mariadb")
                .build());
    }

}
