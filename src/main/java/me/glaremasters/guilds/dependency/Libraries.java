package me.glaremasters.guilds.dependency;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;

public class Libraries {


    /**
     * Load all the dependencies for the plugin
     *
     * @param loader the loader to add to
     */
    public void loadDepLibs(BukkitLibraryManager loader) {

        loader.loadLibrary(Library.builder()
                .groupId("commons-io")
                .artifactId("commons-io")
                .version("2.6")
                .checksum("+HfTBGYKwqFC84ZbrfyXHex+1zx0fH+NXS9ROcpzZRM=")
                .relocate("org{}apache{}commons{}io", "me.glaremasters.guilds.libs.commonsio")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("org.apache.commons")
                .artifactId("commons-collections4")
                .version("4.4")
                .relocate("org{}apache{}commons{}collections4", "me.glaremasters.guilds.libs.collections")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("co.aikar")
                .artifactId("taskchain-core")
                .version("3.7.2")
                .checksum("OpSCCN+7v6gqFpsU/LUNOOXzjImwjyE2ShHZ5xFUj/Q=")
                .relocate("co{}aikar{}taskchain", "me.glaremasters.guilds.libs.taskchain")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("co.aikar")
                .artifactId("taskchain-bukkit")
                .version("3.7.2")
                .checksum("B/O3+zWGalLs8otAr8tdNnIc/39FDRh6tN5qvNgfEaI=")
                .relocate("co{}aikar{}taskchain", "me.glaremasters.guilds.libs.taskchain")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("net{}lingala{}zip4j")
                .artifactId("zip4j")
                .version("2.1.2")
                .checksum("J7wqxAk066coV79fCMwzPGL8rHZ9Jz7mwtJ9QMhg9I8=")
                .relocate("net{}lingala{}zip4j", "me.glaremasters.guilds.libs.zip4j")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("com{}github{}stefvanschie{}inventoryframework")
                .artifactId("IF")
                .version("0.5.8")
                .checksum("t09sCQxreghG3a3pqA7rYLuFXK/Uf3OL5mhhvCM1yFA=")
                .relocate("com{}github{}stefvanschie{}inventoryframework", "me.glaremasters.guilds.libs.if")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("com.dumptruckman.minecraft")
                .artifactId("JsonConfiguration")
                .version("1.1")
                .checksum("aEEn9nIShT4mvJlF538Mnv+hbP/Yv17ANGchaaBoyCw=")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("net.minidev")
                .artifactId("json-smart")
                .version("1.1.1")
                .checksum("zr2iXDGRqkQWc8Q9elqVZ6pdhqEBAa6RWohckLzuh3E=")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("org.codemc.worldguardwrapper")
                .artifactId("worldguardwrapper")
                .version("1.1.6-SNAPSHOT")
                .checksum("G023FrJyvpmZxVWeXcGUCBipNB3BSA3rKcKPnCP7Sac=")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("org.javassist")
                .artifactId("javassist")
                .version("3.21.0-GA")
                .checksum("eqWeAx+UGYSvB9rMbKhebcm9OkhemqJJTLwDTvoSJdA=")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("org{}reflections")
                .artifactId("reflections")
                .version("0.9.11")
                .checksum("zKiEKPiokZ34hRBYM9Rf8HvSb5hflu5VaQVRIWtYtKE=")
                .relocate("com{}google{}common", "me.glaremasters.guilds.libs.guava")
                .relocate("org{}reflections", "me.glaremasters.guilds.libs.reflections")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("ch.jalu")
                .artifactId("configme")
                .version("1.1.0")
                .checksum("c3EUKZSs/xPSHwn/K0KMf9hTbN0ijRXyIBtOg5PxUnI=")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("com.google.guava")
                .artifactId("guava")
                .version("21.0")
                .checksum("lyE5cYq8ikiT+njLqM97LJA/Ncl6r0T6MDGwZplItIA=")
                .relocate("com{}google{}common", "me.glaremasters.guilds.libs.guava")
                .build());

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
                .checksum("+WVImk+rb9rFTpV05tpbShAMYgS0UnUNzOc2cZDS4n0=")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("org{}jdbi")
                .artifactId("jdbi3-sqlobject")
                .version("3.8.2")
                .relocate("org{}jdbi", "me.glaremasters.guilds.libs.jdbi")
                .checksum("C1YqefomaJiob2FgRp7YLeofVKTt81D+sIEIKV3zQio=")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("io.leangen.geantyref")
                .artifactId("geantyref")
                .version("1.3.7")
                .relocate("org{}jdbi", "me.glaremasters.guilds.libs.jdbi")
                .checksum("+JH4yXPM0d/LhBv+2EZOiWsbMN3dA9kzxL6p5CihEaI=")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("org.antlr")
                .artifactId("antlr4-runtime")
                .version("4.7.2")
                .relocate("org{}jdbi", "me.glaremasters.guilds.libs.jdbi")
                .checksum("TFGLh9S9/4tEzYy8GvgW6US2Kj/luAt4FQHPH0dZu8Q=")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("org.slf4j")
                .artifactId("slf4j-api")
                .version("1.7.25")
                .build());
    }

}
