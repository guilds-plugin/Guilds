import com.diffplug.gradle.spotless.FormatExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.kyori.indra.licenser.spotless.IndraSpotlessLicenserExtension
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.indra)
    alias(libs.plugins.indra.publishing)
    alias(libs.plugins.spotless)
    alias(libs.plugins.indra.licenser.spotless)
    alias(libs.plugins.shadow)
    alias(libs.plugins.versions)
    alias(libs.plugins.dokka)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.quark)
}

group = "me.glaremasters"
version = "3.5.7.2-SNAPSHOT"

val pluginVersion = version.toString()

base {
    archivesName.set("Guilds")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmToolchain(21)
}

quark {
    /*
     * Use Bukkit for the main SpigotMC artifact.
     * Paper can run Bukkit plugins, but Spigot cannot run Paper-specific loaders.
     */
    platform = "bukkit"

    repositories {
        maven("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    /*
     * Bundled into the final plugin jar by shadowJar.
     */
    implementation(libs.acf.paper)
    implementation(libs.bstats.bukkit)
    implementation(libs.taskchain.bukkit)
    implementation(libs.worldguardwrapper)
    implementation(libs.configme)
    implementation(libs.jsonconfiguration)
    implementation(libs.xseries)
    implementation(libs.adventure.platform.bukkit)
    implementation(libs.triumph.gui)
    implementation(libs.hikaricp)
    implementation(libs.jdbi.core)
    implementation(libs.jdbi.sqlobject)
    implementation(libs.mariadb.client)
    implementation(libs.quark.bukkit)

    /*
     * Kotlin is compiled against locally, but downloaded and loaded at runtime
     * by Quark to reduce the SpigotMC upload jar size.
     */
    compileOnly(libs.kotlin.stdlib)
    quark(libs.kotlin.stdlib)

    /*
     * Provided by the server or by other plugins at runtime.
     * These must not be bundled or relocated.
     */
    compileOnly(libs.spigot.api)
    compileOnly(libs.vault)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.jsr305)
    compileOnly(libs.authlib) {
        isTransitive = false
    }
}

extensions.configure<SpotlessExtension> {
    fun FormatExtension.standardOptions() {
        endWithNewline()
        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        toggleOffOn("@formatter:off", "@formatter:on")
    }

    java {
        target("src/**/*.java")

        targetExclude(
            "src/**/me/glaremasters/guilds/scanner/ZISScanner.java",
            "src/**/me/glaremasters/guilds/updater/UpdateChecker.java",
            "src/**/me/glaremasters/guilds/utils/PremiumFun.java"
        )

        standardOptions()
        formatAnnotations()
        removeUnusedImports()
    }

    kotlin {
        target("src/**/*.kt")

        standardOptions()
    }

    kotlinGradle {
        target("*.gradle.kts", "gradle/**/*.gradle.kts")

        standardOptions()
    }
}

extensions.configure<IndraSpotlessLicenserExtension> {
    /*
     * Create HEADER.txt at the project root.
     *
     * Example:
     *
     * /*
     *  * This file is part of Guilds.
     *  *
     *  * Guilds is free software: you can redistribute it and/or modify
     *  * it under the terms of the MIT License.
     *  *
     *  * Copyright (c) GlareMasters
     *  */
     */
    licenseHeaderFile(rootProject.file("HEADER.txt"))

    property("name", "Guilds")
    property("organization", "GlareMasters")
    property("url", "https://github.com/guilds-plugin/guilds")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        javaParameters.set(true)
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(11)
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(
        listOf(
            "-parameters",
            "-Xlint:-classfile"
        )
    )
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<ProcessResources>("processResources") {
    filteringCharset = "UTF-8"

    /*
     * Configuration-cache safe:
     * - compute a plain serializable value during configuration
     * - declare it as an input
     * - capture only this map in the CopySpec action
     */
    val resourceTokens = mapOf(
        "version" to pluginVersion
    )

    inputs.properties(resourceTokens)

    filesMatching("plugin.yml") {
        expand(resourceTokens)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    minimize()

    archiveClassifier.set("")
    archiveBaseName.set("Guilds")
    archiveVersion.set(pluginVersion)

    /*
     * Shadow's default is already runtimeClasspath, but keeping this explicit makes
     * the intent clear: implementation/runtime dependencies are shaded; compileOnly
     * APIs are not.
     */
    configurations = project.configurations.runtimeClasspath.map { listOf(it) }

    /*
     * Required for libraries that use META-INF/services, such as JDBC drivers and
     * libraries with service-loader based discovery.
     */
    mergeServiceFiles()

    /*
     * Avoid invalid signature metadata after classes/resources are transformed.
     */
    exclude(
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "META-INF/INDEX.LIST",
        "module-info.class"
    )

    /*
     * Reproducible jar output.
     */
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false

    val relocationRoot = "me.glaremasters.guilds.libs"

    /*
     * ACF
     */
    relocate("co.aikar.commands", "$relocationRoot.acf.commands") {
        skipStringConstants = true
    }
    relocate("co.aikar.locales", "$relocationRoot.acf.locales") {
        skipStringConstants = true
    }

    /*
     * TaskChain
     */
    relocate("co.aikar.taskchain", "$relocationRoot.taskchain") {
        skipStringConstants = true
    }

    /*
     * bStats
     */
    relocate("org.bstats", "$relocationRoot.bstats") {
        skipStringConstants = true
    }

    /*
     * WorldGuardWrapper.
     *
     * Do not relocate WorldGuard, WorldEdit, Bukkit, or Spigot APIs themselves.
     * Only relocate the wrapper library.
     */
    relocate("org.codemc.worldguardwrapper", "$relocationRoot.worldguardwrapper") {
        skipStringConstants = true
    }

    /*
     * Config libraries
     */
    relocate("ch.jalu.configme", "$relocationRoot.configme") {
        skipStringConstants = true
    }
    relocate("com.dumptruckman.minecraft", "$relocationRoot.jsonconfiguration") {
        skipStringConstants = true
    }

    /*
     * XSeries
     */
    relocate("com.cryptomorin.xseries", "$relocationRoot.xseries") {
        skipStringConstants = true
    }

    /*
     * Adventure platform and Kyori internals.
     *
     * This is safe when Adventure is used internally by the plugin.
     * If your public API exposes Adventure Component types to other plugins,
     * do not relocate net.kyori.adventure.
     */
    relocate("net.kyori.adventure", "$relocationRoot.adventure") {
        skipStringConstants = true
    }
    relocate("net.kyori.examination", "$relocationRoot.examination") {
        skipStringConstants = true
    }
    relocate("net.kyori.option", "$relocationRoot.kyori.option") {
        skipStringConstants = true
    }

    /*
     * Triumph GUI
     */
    relocate("dev.triumphteam.gui", "$relocationRoot.triumph.gui") {
        skipStringConstants = true
    }

    /*
     * Database stack
     */
    relocate("com.zaxxer.hikari", "$relocationRoot.hikari") {
        skipStringConstants = true
    }
    relocate("org.jdbi", "$relocationRoot.jdbi") {
        skipStringConstants = true
    }
    relocate("org.mariadb.jdbc", "$relocationRoot.mariadb") {
        skipStringConstants = true
    }

    /*
     * Common transitive libraries pulled by the database/config stack.
     * These are intentionally narrow to avoid relocating server/plugin APIs.
     */
    relocate("org.antlr", "$relocationRoot.antlr") {
        skipStringConstants = true
    }
    relocate("org.checkerframework", "$relocationRoot.checkerframework") {
        skipStringConstants = true
    }
    relocate("org.intellij.lang.annotations", "$relocationRoot.intellij.annotations") {
        skipStringConstants = true
    }
    relocate("org.jetbrains.annotations", "$relocationRoot.jetbrains.annotations") {
        skipStringConstants = true
    }
}

tasks.named("assemble") {
    dependsOn(tasks.named("shadowJar"))
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}

tasks.named("check") {
    dependsOn(tasks.named("spotlessCheck"))
}

indra {
    mitLicense()

    javaVersions {
        target(11)
    }

    github("guilds-plugin", "guilds") {
        publishing(true)
    }

    publishAllTo("guilds", "https://repo.glaremasters.me/repository/guilds/")
}

val javaToolchains = extensions.getByType<JavaToolchainService>()

data class MinecraftRunTarget(
    val minecraftVersion: String,
    val javaVersion: Int,
    val directoryName: String = minecraftVersion
)

val supportedMinecraftVersions = listOf(
    MinecraftRunTarget("1.8.8", 11),
    MinecraftRunTarget("1.16.5", 16),
    MinecraftRunTarget("1.18.2", 17),
    MinecraftRunTarget("1.19.4", 17),
    MinecraftRunTarget("1.20.6", 21),
    MinecraftRunTarget("1.21.1", 21),
    MinecraftRunTarget("1.21.4", 21),
    MinecraftRunTarget("1.21.8", 21),
    MinecraftRunTarget("26.1.2", 25)
)

fun RunServer.configureGuildsRunServer(target: MinecraftRunTarget) {
    minecraftVersion(target.minecraftVersion)
    runDirectory.set(layout.projectDirectory.dir("run/${target.directoryName}"))

    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(target.javaVersion))
        }
    )

    pluginJars.from(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
    dependsOn(tasks.named("shadowJar"))

    downloadPlugins {
        /*
         * EssentialsX:
         * Hangar marks this release as an external download, so run-task's
         * Hangar downloader returns 404. Use GitHub Releases instead.
         */
        url("https://ci.ender.zone/job/EssentialsX/lastSuccessfulBuild/artifact/jars/EssentialsX-2.22.0-dev+112-5baf239.jar")

        /*
         * LuckPerms:
         * Official Bukkit loader URL.
         */
        url("https://download.luckperms.net/1638/bukkit/loader/LuckPerms-Bukkit-5.5.50.jar")

        /*
         * Vault:
         * No clean native Hangar source; use pinned release jar.
         */
        url("https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar")
    }

    doFirst {
        val serverDir = runDirectory.get().asFile
        serverDir.mkdirs()

        serverDir.resolve("eula.txt").writeText(
            """
            # Generated by Gradle run-paper for local Guilds development.
            # By changing this setting to TRUE you are indicating your agreement to the Minecraft EULA.
            # https://aka.ms/MinecraftEULA
            eula=true
            """.trimIndent() + System.lineSeparator()
        )
    }
}

tasks {
    runServer {
        configureGuildsRunServer(
            supportedMinecraftVersions.last().copy(directoryName = "latest")
        )
    }

    supportedMinecraftVersions.forEach { target ->
        val taskSuffix = target.minecraftVersion.replace(".", "_")

        register<RunServer>("runServer$taskSuffix") {
            group = "run paper"
            description =
                "Runs a Paper test server for Minecraft ${target.minecraftVersion} using Java ${target.javaVersion}."

            configureGuildsRunServer(target)
        }
    }
}