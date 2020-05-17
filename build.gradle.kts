import org.apache.tools.ant.filters.ReplaceTokens
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.3.72" // Deprecated, look into MPP plugin once 1.4-M2 drops (fixes IntelliJ bug making it unusable)
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("org.sonarqube") version "2.7"
}

group = "me.glaremasters"
version = "3.5.5.0"

val relocBase = "me.glaremasters.guilds.libs."

repositories {
    mavenCentral()

    maven {
        name = "spigot-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")

        content {
            includeGroup("org.bukkit")
        }
    }

    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        name = "aikar-repo"
        url = uri("https://repo.aikar.co/content/groups/aikar/")

        content {
            includeGroup("co.aikar")
        }
    }

    maven {
        name = "glare-repo"
        url = uri("https://repo.glaremasters.me/repository/public/")
    }
}

tasks.withType<ShadowJar> {
    minimize()
    fun relocs(vararg packages: String) =
            packages.forEach { relocate(it, "$relocBase.$it") }
    relocs(
            "org.bstats",
            "co.aikar.commands",
            "co.aikar.locales",
            "co.aikar.taskchain", // Consider skedule
            "ch.jalu.configme",
            "net.byteflux.libby",
            "org.jdbi",
            "org.mariadb.jdbc",
            "me.mattstudios.mfgui",
            "me.rayzr522.jsonmessage"
    )
    archiveFileName.set("Guilds-$version.jar")
}

dependencies {
    // Command Handling
    implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")

    // Bstats Data Collecting
    implementation("org.bstats:bstats-bukkit:1.7")

    // Runtime Dependency Downloading
    implementation("net.byteflux:libby-bukkit:0.0.2-SNAPSHOT")

    // Taskchain Scheduling
    implementation("co.aikar:taskchain-bukkit:3.7.2") // Consider skedule

    // Worldguard Claim Handling
    implementation("org.codemc.worldguardwrapper:worldguardwrapper:1.1.6-SNAPSHOT")

    // Configuration Lib
    implementation("ch.jalu:configme:1.2.0")

    // Serializing Inventories via JSON
    implementation("com.dumptruckman.minecraft:JsonConfiguration:1.1")

    // Cross-Version Support
    implementation("com.github.cryptomorin:XSeries:5.3.1")

    // GUI Lib
    implementation("me.mattstudios.utils:matt-framework-gui:1.2.7")

    // Json Messages
    implementation("me.rayzr522:jsonmessage:1.2.0")

    // Kotlin collections
    implementation(kotlin("stdlib-jdk8"))

    // Vault Support
    compileOnly("net.milkbowl:vault:1.7")

    // Database Stuff
    compileOnly("com.zaxxer:HikariCP:3.3.1")
    compileOnly("org.jdbi:jdbi3-core:3.8.2")
    compileOnly("org.jdbi:jdbi3-sqlobject:3.8.2")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:2.4.0")

    // Logger
    compileOnly("org.slf4j:slf4j-api:1.7.25")

    // Bukkit Version
    compileOnly("org.bukkit:bukkit:1.15.2-R0.1-SNAPSHOT")

    // Mojang Library
    compileOnly("com.mojang:authlib:1.5.25")

    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.10.6")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
    options.forkOptions.executable = "javac"
    options.encoding = "UTF-8"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.javaParameters = true
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

val processResources by tasks.getting(Copy::class) {
    filter<ReplaceTokens>("tokens" to mapOf("version" to project.version.toString()))
}