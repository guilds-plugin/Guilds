import com.diffplug.gradle.spotless.FormatExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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
    alias(libs.plugins.shadow)
    alias(libs.plugins.versions)
    alias(libs.plugins.dokka)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.quark)
}

group = "me.glaremasters"
version = "3.5.7.3-SNAPSHOT"

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
    implementation(libs.quark.bukkit)

    /*
     * Large runtime libraries are compiled against locally, but downloaded and loaded
     * by Quark to keep the file size slim.
     */
    compileOnly(libs.kotlin.stdlib)
    quark(libs.kotlin.stdlib)

    compileOnly(libs.hikaricp)
    quark(libs.hikaricp)

    compileOnly(libs.jdbi.core)
    quark(libs.jdbi.core)

    compileOnly(libs.jdbi.sqlobject)
    quark(libs.jdbi.sqlobject)

    compileOnly(libs.mariadb.client)
    quark(libs.mariadb.client)

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

    /*
     * Tests run without a server, so spigot-api and vault have to be on the test classpath even
     * though the server provides them at runtime.
     *
     * The API is 1.16.5 rather than the one the plugin compiles against. The current Spigot API
     * ships Java 17+ bytecode, which a Java 11 JVM cannot load, so the testJava11 run would fail
     * before reaching a single assertion. The tests only touch API that has been stable since 1.8.
     */
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.spigot.api.test)
    testImplementation(libs.vault)
    // compileOnly above, because Quark loads it at plugin startup. Tests run as plain JVM code.
    testImplementation(libs.kotlin.stdlib)
    testRuntimeOnly(libs.junit.platform.launcher)
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

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        javaParameters.set(true)
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
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

/*
 * Compiles every org.bukkit.Material constant referenced from src/main against the 1.8.8 API, so a
 * constant that only exists on newer servers cannot reach a release. Material is the hazard that
 * matters: the 1.13 flattening renamed several hundred constants at once.
 *
 * Deliberately does not compile the whole source set against 1.8.8. The plugin has version-guarded
 * code that has to keep working on modern servers, and that is not expressible in a 1.8.8-only
 * compilation. See gradle/legacy-api-allowlist.txt for the escape hatch and the trade-offs.
 */
val legacyApiAllowlist = layout.projectDirectory.file("gradle/legacy-api-allowlist.txt")

val legacyApi = configurations.create("legacyApi") {
    isCanBeConsumed = false
    isCanBeResolved = true
}

/*
 * Matches `Material.SOMETHING` but not `XMaterial.SOMETHING`, which is the abstraction to prefer.
 * Comments are stripped first so that documenting why a constant is unsafe on 1.8.8 does not by
 * itself fail the build. Not a real lexer: a line comment marker inside a string literal over-strips
 * and can hide a reference, which is a false negative and acceptable for a tripwire.
 */
val materialConstantPattern = Regex("""(?<![A-Za-z0-9_$.])Material\.([A-Z][A-Z_0-9]*)""")
val commentPattern = Regex("""/\*.*?\*/|//[^\n]*""", RegexOption.DOT_MATCHES_ALL)

val generateLegacyApiProbe = tasks.register("generateLegacyApiProbe") {
    val allowlistFile = legacyApiAllowlist
    val outputDir = layout.buildDirectory.dir("generated/legacyApiProbe")
    val mainSourceFiles = fileTree(layout.projectDirectory.dir("src/main"))

    inputs.files(mainSourceFiles)
        .withPropertyName("mainSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(allowlistFile).withPropertyName("allowlist").withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(outputDir).withPropertyName("probeSources")

    doLast {
        val allowed = allowlistFile.asFile
            .readLines()
            .map { it.substringBefore('#').trim() }
            .filter { it.isNotEmpty() }
            .toSet()

        val referenced = mainSourceFiles
            .filter { it.isFile }
            .flatMap { file ->
                val code = commentPattern.replace(file.readText(), " ")
                materialConstantPattern.findAll(code).map { it.groupValues[1] }.toList()
            }
            .distinct()
            .sorted()

        val denied = referenced.filterNot { it in allowed }
        val skipped = referenced.filter { it in allowed }

        val target = outputDir.get().dir("me/glaremasters/guilds/compat").asFile
        target.deleteRecursively()
        target.mkdirs()

        val body = denied
            .map { "        probe(org.bukkit.Material.$it);" }
            .ifEmpty { listOf("        // No Bukkit Material constants are referenced directly from src/main.") }

        File(target, "LegacyMaterialProbe.java").writeText(
            listOf(
                "package me.glaremasters.guilds.compat;",
                "",
                "/**",
                " * Generated by generateLegacyApiProbe. Compiled against the 1.8.8 API on purpose.",
                " *",
                " * If this fails to compile, a Material constant is unavailable on 1.8.8. Use XSeries",
                " * instead, or add a documented entry to gradle/legacy-api-allowlist.txt.",
                " */",
                "final class LegacyMaterialProbe {",
                "",
                "    private LegacyMaterialProbe() {",
                "    }",
                "",
                "    private static void probe(Object ignored) {",
                "    }",
                "",
                "    static void probe() {",
                *body.toTypedArray(),
                "    }",
                "}",
                "",
            ).joinToString("\n")
        )

        logger.lifecycle(
            "Legacy API probe: ${denied.size} Material constant(s) checked against 1.8.8, " +
                "${skipped.size} allow-listed." +
                if (skipped.isEmpty()) "" else " Allow-listed: ${skipped.joinToString()}."
        )
    }
}

val legacyApiProbeSourceSet = sourceSets.create("legacyApiProbe") {
    java.srcDir(generateLegacyApiProbe.map { it.outputs.files.singleFile })
}

configurations[legacyApiProbeSourceSet.compileClasspathConfigurationName]
    .extendsFrom(configurations[legacyApi.name])

dependencies {
    legacyApi(libs.spigot.api.legacy) {
        // The 1.8.8 POM pulls in bungeecord-chat:1.8-SNAPSHOT, purged from Sonatype. The probe does
        // not need it.
        isTransitive = false
    }
}

tasks.named("check") {
    dependsOn(tasks.named(legacyApiProbeSourceSet.classesTaskName))
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
     * Common transitive libraries pulled by shaded dependencies.
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
         */
        github("EssentialsX", "Essentials", "2.22.0", "EssentialsX-2.22.0.jar")

        /*
         * LuckPerms:
         * The pinned download.luckperms.net loader URL now 404s, which broke every runServer* task.
         * b0mk8uS6 is LuckPerms 5.5.71 for the Bukkit loader.
         */
        modrinth("luckperms", "b0mk8uS6")

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

/*
 * Indra registers testJava11 and wires it into check, but it does not give the task a launcher, so
 * the task runs on whatever JVM Gradle runs on. Java 11 is the documented runtime floor, so point
 * it at a real Java 11 toolchain.
 *
 * afterEvaluate, because indra registers the task from its extension block further down, and
 * tasks.named(...) would not resolve before that.
 */
afterEvaluate {
    tasks.named<Test>("testJava11") {
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(11))
        })
    }
}
