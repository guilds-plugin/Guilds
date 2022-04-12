import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import net.kyori.indra.IndraPlugin
import net.kyori.indra.IndraPublishingPlugin

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.20"
    id("net.kyori.indra") version "2.1.1"
    id("net.kyori.indra.publishing") version "2.1.1"
    id("net.kyori.indra.license-header") version "2.1.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.github.slimjar") version "1.3.0"
    id("xyz.jpenilla.run-paper") version "1.0.6"
}

group = "me.glaremasters"
version = "3.5.6.4-SNAPSHOT"

base {
    archivesBaseName = "Guilds"
}

apply {
    plugin<ShadowPlugin>()
    plugin<IndraPlugin>()
    plugin<IndraPublishingPlugin>()
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

    maven("https://repo.codemc.org/repository/maven-public/") {
        content { includeGroup("org.codemc.worldguardwrapper") }
    }
    maven("https://repo.glaremasters.me/repository/public/")
}

dependencies {
    implementation("io.github.slimjar:slimjar:1.2.7")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.0")
    implementation("co.aikar:taskchain-bukkit:3.7.2")
    implementation("org.codemc.worldguardwrapper:worldguardwrapper:1.1.9-SNAPSHOT")
    implementation("ch.jalu:configme:1.3.0")
    implementation("com.dumptruckman.minecraft:JsonConfiguration:1.1")
    implementation("com.github.cryptomorin:XSeries:8.7.1")
    implementation("net.kyori:adventure-platform-bukkit:4.1.0")
    implementation("dev.triumphteam:triumph-gui:3.1.2")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.jdbi:jdbi3-core:3.8.2")
    implementation("org.jdbi:jdbi3-sqlobject:3.8.2")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.7.2")
    implementation("org.slf4j:slf4j-api:1.7.25")

    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("net.milkbowl:vault:1.7")
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("me.clip:placeholderapi:2.11.1")

    slim("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
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

        github("guilds-plugin", "guilds") {
            publishing(true)
        }

        publishAllTo("guilds", "https://repo.glaremasters.me/repository/guilds/")
    }

    compileKotlin {
        kotlinOptions.javaParameters = true
        kotlinOptions.jvmTarget = "1.8"
    }

    compileJava {
        options.compilerArgs = listOf("-parameters")
    }

    runServer {
        minecraftVersion("1.18.1")
    }

    license {
        header.set(resources.text.fromFile(rootProject.file("LICENSE")))
        exclude("me/glaremasters/guilds/scanner/ZISScanner.java")
        exclude("me/glaremasters/guilds/updater/UpdateChecker.java")
        exclude("me/glaremasters/guilds/utils/PremiumFun.java")
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
            "net.kyori"
        )
    }

    processResources {
        expand("version" to rootProject.version)
    }
}
