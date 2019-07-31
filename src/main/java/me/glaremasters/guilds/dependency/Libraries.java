package me.glaremasters.guilds.dependency;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;

public class Libraries {


    /**
     * Load all the dependencies for the plugin
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
                .version("1.3.2")
                .checksum("xnCY1DDFdDEUMnKOvUx8RWcvnM9cZHAutq+4gWwirQg=")
                .relocate("net{}lingala{}zip4j", "me.glaremasters.guilds.libs.zip4j")
                .build());

        loader.loadLibrary(Library.builder()
                .groupId("com{}github{}stefvanschie{}inventoryframework")
                .artifactId("IF")
                .version("0.3.1")
                .checksum("MOPOPYQSpI3jqFrhQkpTABdO2JpoN4kNqFQTxq7KB+E=")
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
    }

}
