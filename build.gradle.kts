import com.diffplug.gradle.spotless.FormatExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.kyori.indra.licenser.spotless.IndraSpotlessLicenserExtension
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    implementation(libs.kotlin.stdlib)

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
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
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
        target(8)
    }

    github("guilds-plugin", "guilds") {
        publishing(true)
    }

    publishAllTo("guilds", "https://repo.glaremasters.me/repository/guilds/")
}