import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import net.kyori.indra.IndraPlugin

plugins {
    id("java")
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("net.kyori.indra") version "2.0.6"
    id("com.github.johnrengelman.shadow") version "7.1.0"
    id("io.github.slimjar") version "1.3.0"
}

group = "me.glaremasters"
version = "3.5.6.2"

base {
    archivesBaseName = "Guilds"
}

apply {
    plugin<ShadowPlugin>()
    plugin<IndraPlugin>()
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        content { includeGroup("org.bukkit") }
    }
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.aikar.co/content/groups/aikar/") {
        content { includeGroup("co.aikar") }
    }
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.mattstudios.me/artifactory/public/")
    maven("https://repo.mattstudios.me/artifactory/public/")
    maven("https://repo.vshnv.tech/")
    maven("https://repo.codemc.org/repository/maven-public/") {
        content { includeGroup("org.codemc.worldguardwrapper") }
    }
    maven("https://repo.glaremasters.me/repository/public/")

}


dependencies {

    // Slimjar
    implementation("io.github.slimjar:slimjar:1.2.6")

    // Command Handling
    implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")

    // Bstats Data Collecting
    implementation("org.bstats:bstats-bukkit:2.2.1")

    // Taskchain Scheduling
    implementation("co.aikar:taskchain-bukkit:3.7.2")

    // Worldguard Claim Handling
    implementation("org.codemc.worldguardwrapper:worldguardwrapper:1.1.9-SNAPSHOT")

    // Configuration Lib
    implementation("ch.jalu:configme:1.3.0")

    // Serializing Inventories via JSON
    implementation("com.dumptruckman.minecraft:JsonConfiguration:1.1")

    // Cross-Version Support
    implementation("com.github.cryptomorin:XSeries:8.4.0")

    // Adventure
    implementation("net.kyori:adventure-platform-bukkit:4.0.0")

    // GUI Lib
    implementation("dev.triumphteam:triumph-gui:3.0.4")

    // Vault Support
    compileOnly("net.milkbowl:vault:1.7")

    // Database Stuff
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.jdbi:jdbi3-core:3.8.2")
    implementation("org.jdbi:jdbi3-sqlobject:3.8.2")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.7.2")



    // Logger
    implementation("org.slf4j:slf4j-api:1.7.25")

    // Kotlin collections
    slim("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Bukkit Version
    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")

    // Mojang Library
    compileOnly("com.mojang:authlib:1.5.25")

    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.10.10")

}

tasks {
    build {
        dependsOn(named("shadowJar"))
        dependsOn(named("slimJar"))
    }

    indra {
        mitLicense()

        javaVersions {
            target(8)
        }

        github("guilds-plugin", "guilds")
    }

    compileKotlin {
        kotlinOptions.javaParameters = true
        kotlinOptions.jvmTarget = "1.8"
    }

    compileJava {
        options.compilerArgs = listOf("-parameters")
    }

    shadowJar {
        fun relocates(vararg dependencies: String) {
            dependencies.forEach {
                val split = it.split(".")
                val name = split.last()
                relocate(it, "me.glaremasters.guilds.libs.$name")
            }
        }

        relocates(
            "io.github.slimjar"
        )

        minimize()

        archiveClassifier.set(null as String?)
        archiveFileName.set("Guilds-${project.version}.jar")
        destinationDirectory.set(rootProject.tasks.shadowJar.get().destinationDirectory.get())
    }

    slimJar {
        fun relocates(vararg dependencies: String) {
            dependencies.forEach {
                val split = it.split(".")
                val name = split.last()
                relocate(it, "me.glaremasters.guilds.libs.$name")
            }
        }

        relocates(
            "org.bstats",
            "co.aikar.commands",
            "co.aikar.locales",
            "co.aikar.taskchain",
            "ch.jalu.configme",
            "com.zaxxer.hikari",
            "org.jdbi",
            "org.mariadb.jdbc",
            "dev.triumphteam.gui",
        )
    }

    processResources {
        expand("version" to rootProject.version)
    }
}
