import com.diffplug.gradle.spotless.FormatExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import me.drownek.plugwright.PlugwrightTestTask
import me.drownek.plugwright.api.ConfigNodeBuilder
import me.drownek.plugwright.api.ConfigValue
import me.drownek.plugwright.api.EnvironmentSpec
import me.drownek.plugwright.api.PlugwrightMode
import me.drownek.plugwright.local.LocalEnvironmentSpec
import me.drownek.plugwright.local.LocalMode
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import xyz.jpenilla.runpaper.task.RunServer
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.TimeUnit

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

    // End-to-end testing with real Paper server and real player bots. 3.0.0 is the latest
    // stable release; the Gradle plugin only resolves from the Plugin Portal, which `plugins {}`
    // consults implicitly.
    id("io.github.drownek.plugwright") version "3.0.0"
}

group = "me.glaremasters"
version = "3.5.7.4-SNAPSHOT"

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
    compileOnly(libs.authlib) {
        isTransitive = false
    }
    compileOnly(libs.guava)

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
    MinecraftRunTarget("1.12.2", 11),
    MinecraftRunTarget("1.16.5", 16),
    MinecraftRunTarget("1.18.2", 17),
    MinecraftRunTarget("1.19.4", 17),
    MinecraftRunTarget("1.20.6", 21),
    MinecraftRunTarget("1.21.1", 21),
    MinecraftRunTarget("1.21.4", 21),
    MinecraftRunTarget("1.21.8", 21),
    MinecraftRunTarget("26.1.2", 25),
    MinecraftRunTarget("26.3", 25)
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
 * The versions the runtime smoke suite runs on.
 *
 * The full suite only runs on a modern server; the oldest supported versions get the smoke suite
 * instead, which proves the plugin loads and answers there without dragging the whole suite along.
 * The newest supported release is included so a regression in the current Paper build is caught
 * here too.
 *
 * The JDK each version needs is the one `supportedMinecraftVersions` already maps it to, so the
 * two lists cannot drift apart.
 */
data class SmokeEnvironment(
    val name: String,
    val minecraftVersion: String,
    val javaVersion: Int,
    /**
     * Whether CraftBukkit on this version refuses the `--nogui` flag the runner passes.
     *
     * The old builds parse their command line with joptsimple and reject the flag as an unknown
     * option, which stops the server before it loads a single plugin.
     */
    val refusesNoGui: Boolean,
    /**
     * Whether the server's Netty cannot use direct buffers on a modern JVM.
     *
     * Netty 4.0, which 1.8.8 ships, decides whether it can by reading the address of a heap
     * buffer, which no longer reads as zero on Java 9 and later. Without that support its native
     * transport cannot work, and every connection the server accepts dies with "Unable to access
     * address of buffer".
     */
    val needsNioTransport: Boolean,
    /**
     * The EssentialsX build this environment runs.
     *
     * EssentialsX 2.21 and newer need an Adventure API that Paper only started shipping with
     * 1.18, so on the older servers the plugin fails to enable with a NoClassDefFoundError for
     * MiniMessage's TagResolver. 2.20.1 is the newest build that does not need it.
     */
    val essentialsX: String,
    /**
     * The Minecraft version the bot connects with.
     *
     * minecraft-data — the protocol table mineflayer reads — has no entry for 26.3 yet, and a bot
     * asked for a version it does not know refuses to start at all. 26.1 and 26.2 are the same
     * protocol, so the bot is pointed at 26.1 for those.
     */
    val botVersion: String,
    /**
     * Whether a bot can be driven on this version at all.
     *
     * 26.3 is a protocol newer than anything minecraft-data knows, so the server refuses the bot
     * as an outdated client. The server itself is fine, so that environment gets a server-level
     * check instead of the bot-driven one.
     */
    val botless: Boolean,
)

/*
 * The EssentialsX builds the environments run.
 *
 * EssentialsX 2.21 and newer need an Adventure API that Paper only started shipping with 1.18,
 * so on the older servers the plugin fails to enable with a NoClassDefFoundError for MiniMessage's
 * TagResolver. 2.20.1 is the newest build that does not need it, and it is what the legacy
 * environments run.
 */
val CURRENT_ESSENTIALS_X = "https://cdn.modrinth.com/data/hXiIvTyT/versions/nY6VN1XH/EssentialsX-2.22.0.jar"
val LEGACY_ESSENTIALS_X = "https://cdn.modrinth.com/data/hXiIvTyT/versions/puUfqBpY/EssentialsX-2.20.1.jar"

val smokeEnvironments = listOf(
    "1.8.8",
    "1.12.2",
    "1.16.5",
    "1.21.8",
    "26.1.2",
    supportedMinecraftVersions.last().minecraftVersion,
)
    .distinct()
    .map { version ->
        val target = supportedMinecraftVersions.first { it.minecraftVersion == version }
        val legacy = version == "1.8.8" || version == "1.12.2" || version == "1.16.5"

        SmokeEnvironment(
            name = "smoke-$version",
            minecraftVersion = version,
            javaVersion = target.javaVersion,
            refusesNoGui = version == "1.8.8" || version == "1.12.2",
            needsNioTransport = version == "1.8.8",
            essentialsX = if (legacy) LEGACY_ESSENTIALS_X else CURRENT_ESSENTIALS_X,
            botVersion = if (version.startsWith("26.")) "26.1" else version,
            botless = version == "26.3",
        )
    }

/*
 * End-to-end testing.
 *
 * Plugwright downloads a Paper server, stages Guilds' shadowJar plus the three plugins Guilds
 * refuses to start without, starts the server and drives it with real mineflayer bots.
 *
 * Two sets of environments live here:
 *
 * - "local" runs the whole suite on one version, 1.21.8 by default and whatever
 *   -Pe2eMinecraftVersion names otherwise. This is what the pull-request job runs.
 *
 * - The smoke environments run the same plugin, the same staged config and the same dependencies
 *   on the versions the full suite cannot run on: the oldest ones the plugin supports and the
 *   newest one. They run a single spec file and have their own task, so the pull-request job is
 *   left exactly as it was.
 */
plugwright {
    testsDir.set(file("src/test/e2e"))
    primaryEnvironment.set("local")

    environments {
        create("local", LocalMode) {
            minecraftVersion.set(
                providers.gradleProperty("e2eMinecraftVersion").getOrElse("1.21.8")
            )
            configureGuildsEnvironment(listOf("-Xmx3G"), CURRENT_ESSENTIALS_X)
        }

        smokeEnvironments.forEach { smoke ->
            create(smoke.name, LocalMode) {
                minecraftVersion.set(smoke.minecraftVersion)

                /*
                 * The pull-request job runs the full suite on "local" and nothing else. The smoke
                 * environments have their own task, so leaving them out of the matrix keeps that
                 * job's runtime and its failure surface exactly what it was.
                 */
                includeInMatrix.set(false)

                configureGuildsEnvironment(listOf("-Xmx1500M"), smoke.essentialsX)
            }
        }
    }
}

/**
 * The plugin set and the staged configuration every Guilds E2E environment runs with.
 *
 * Guilds has no plugin.yml defaults for its commands, so a non-op player has none of
 * `guilds.command.*`. Staging the config here is what makes every test deterministic:
 * `plugwrightClean` wipes the run directory before each provision, so the plugin only ever sees
 * this config and a freshly generated tiers.yml/roles.yml.
 */
fun LocalEnvironmentSpec.configureGuildsEnvironment(jvmArgs: List<String>, essentialsX: String) {
    acceptEula.set(true)
    this.jvmArgs.set(jvmArgs)

    /*
     * The same pins `configureGuildsRunServer` already downloads for the manual runServer* tasks.
     * Vault is not on Hangar or any Maven repo, so it comes from the upstream release jar;
     * LuckPerms' loader URL moved, so Modrinth's copy is used.
     */
    downloadPlugins {
        url("https://github.com/MilkBowl/Vault/releases/download/1.7.3/Vault.jar")
        url(essentialsX)
        url("https://cdn.modrinth.com/data/Vebnzrzj/versions/b0mk8uS6/LuckPerms-Bukkit-5.5.71.jar")
    }

    writeFiles {
        file("plugins/Guilds/config.yml", file("src/test/e2e/server/plugins/Guilds/config.yml"))
        file("plugins/Guilds/tiers.yml", file("src/test/e2e/server/plugins/Guilds/tiers.yml"))
        file("plugins/Guilds/buffs.yml", file("src/test/e2e/server/plugins/Guilds/buffs.yml"))
    }

    plugins {
        local("guilds")
    }
}

/**
 * The script a smoke environment's server is started with.
 *
 * It exists for two reasons, both of them things plugwright's local mode cannot do per
 * environment:
 *
 * - It runs the server on the JDK that the Minecraft version needs. Plugwright starts the server
 *   with the JVM of the project's Java toolchain, which is one setting for the whole build, and a
 *   Paper 1.8.8 server does not run on Java 21.
 *
 * - It makes the legacy servers start at all. Their CraftBukkit refuses the `--nogui` flag the
 *   runner always passes.
 */
fun smokeLauncherScript(javaPath: String, refusesNoGui: Boolean): String {
    val dropNoGui = if (refusesNoGui) {
        """
        # CraftBukkit on this version rejects --nogui as an unknown option, and the runner always
        # passes it, so it is dropped here.
        count=${'$'}#
        index=0
        while [ "${'$'}index" -lt "${'$'}count" ]; do
            argument="${'$'}1"
            shift
            if [ "${'$'}argument" != "--nogui" ]; then
                set -- "${'$'}@" "${'$'}argument"
            fi
            index=${'$'}((index + 1))
        done
        """.trimIndent()
    } else {
        ""
    }

    return """
    #!/bin/sh
    # Starts the Paper server for one of the Guilds smoke environments.
    $dropNoGui
    exec "$javaPath" "${'$'}@"
    """.trimIndent() + System.lineSeparator()
}

/**
 * The server.properties the 1.8.8 environment runs with.
 *
 * That server ships Netty 4.0, which decides whether it can use direct buffers by reading the
 * address of a heap buffer, which no longer reads as zero on Java 9 and later. Without that support
 * the native transport cannot work, and every connection the server accepts dies with "Unable to
 * access address of buffer" — the bot never gets a reply and the smoke test fails on a socket
 * error rather than on anything to do with Guilds.
 *
 * Turning the native transport off puts the server on the same NIO transport every Windows server
 * uses, which works on a Java 11 JVM.
 */
fun writeLegacyServerProperties(properties: File) {
    val line = "use-native-transport=false"
    val existing = if (properties.exists()) properties.readText() else ""

    val updated = if (existing.lines().any { it.trim().startsWith("use-native-transport=") }) {
        existing.lines().joinToString("\n") {
            if (it.trim().startsWith("use-native-transport=")) line else it
        }
    } else {
        listOf(existing.trimEnd(), line).filter { it.isNotEmpty() }.joinToString("\n")
    }

    properties.parentFile.mkdirs()
    properties.writeText(updated + System.lineSeparator())
}

/**
 * Starts a Paper server and checks that Guilds came up on it.
 *
 * This is what a version no bot can be driven on gets instead of the smoke spec. It cannot prove
 * that the command layer works — that needs a player — but it does prove that the plugin loads,
 * that its hard dependencies load with it, and that Guilds finds the economy and the permission
 * provider it refuses to start without.
 */
fun checkServerLoads(smoke: SmokeEnvironment, launcher: File, runDir: File) {
    val server = ProcessBuilder(
        "/bin/sh",
        launcher.absolutePath,
        "-Xmx1500M",
        "-Dcom.mojang.eula.agree=true",
        "-jar",
        "server.jar",
    )
        .directory(runDir)
        .redirectErrorStream(true)
        .start()

    val log = StringBuilder()
    val reader = Thread {
        server.inputStream.bufferedReader().forEachLine { line ->
            synchronized(log) { log.appendLine(line) }
        }
    }
    reader.isDaemon = true
    reader.start()

    try {
        val deadline = System.currentTimeMillis() + 300_000
        while (System.currentTimeMillis() < deadline) {
            val ready = synchronized(log) { log.contains("Done (") }
            if (ready || !server.isAlive) break
            Thread.sleep(500)
        }

        val text = synchronized(log) { log.toString() }
        check(text.contains("Done (")) {
            "The Paper server for Minecraft ${smoke.minecraftVersion} did not finish starting.\n$text"
        }
        for (plugin in listOf("Vault", "Essentials", "LuckPerms", "Guilds")) {
            check(text.contains("Enabling $plugin")) {
                "$plugin was not enabled on Minecraft ${smoke.minecraftVersion}.\n$text"
            }
        }
        check(!text.contains("Disabling Guilds")) {
            "Guilds disabled itself on Minecraft ${smoke.minecraftVersion}.\n$text"
        }
        check(text.contains("Economy Found:")) {
            "Guilds found no economy on Minecraft ${smoke.minecraftVersion}.\n$text"
        }
        check(text.contains("Permissions Found:")) {
            "Guilds found no permission provider on Minecraft ${smoke.minecraftVersion}.\n$text"
        }
    } finally {
        server.outputStream.write("stop\n".toByteArray())
        server.outputStream.flush()
        if (!server.waitFor(60, TimeUnit.SECONDS)) {
            server.destroyForcibly()
        }
    }
}

/*
 * The smoke environments need a Paper build of their own as well.
 *
 * Plugwright's provision task downloads the oldest build the API lists for a version. For 1.8.8
 * and 1.12.2 that build is a paperclip jar whose patch data points at a Mojang URL that no longer
 * serves the vanilla server jar, so the server exits before it starts. The smoke environments
 * download the newest build instead, and the provision task leaves a server jar that is already in
 * the run directory alone.
 */
fun downloadNewestPaperBuild(version: String, serverJar: File, versionMarker: File) {
    val client = HttpClient.newHttpClient()

    fun get(url: String): String {
        val request = HttpRequest.newBuilder(URI.create(url)).GET().build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() == 200) { "GET $url returned ${response.statusCode()}" }
        return response.body()
    }

    @Suppress("UNCHECKED_CAST")
    val builds = groovy.json.JsonSlurper()
        .parseText(get("https://fill.papermc.io/v3/projects/paper/versions/$version/builds")) as List<Map<String, Any>>
    val newest = builds.maxByOrNull { (it["id"] as Number).toInt() }
        ?: error("Paper publishes no build for Minecraft $version")

    @Suppress("UNCHECKED_CAST")
    val build = groovy.json.JsonSlurper()
        .parseText(
            get("https://fill.papermc.io/v3/projects/paper/versions/$version/builds/${newest["id"]}")
        ) as Map<String, Any>
    @Suppress("UNCHECKED_CAST")
    val downloads = build["downloads"] as Map<String, Map<String, Any>>
    val download = downloads["server:default"] ?: downloads["application"]
        ?: error("Paper build ${newest["id"]} of Minecraft $version has no server download")

    serverJar.parentFile.mkdirs()
    URI.create(download["url"] as String).toURL().openStream().use { input ->
        serverJar.outputStream().use { output -> input.copyTo(output) }
    }
    versionMarker.writeText(version)
}

/*
 * Wires the smoke environments up.
 *
 * afterEvaluate, because the plugwright plugin registers the environments and their tasks from
 * its own afterEvaluate block, which runs first.
 */
afterEvaluate {
    val smokeServerChecks = mutableListOf<TaskProvider<Task>>()
    val smokeTestTasks = smokeEnvironments.map { smoke ->
        val entry = plugwright.environments.get(smoke.name)!!
        @Suppress("UNCHECKED_CAST")
        val spec = entry.spec as LocalEnvironmentSpec
        val runDir = spec.runDir.get().asFile
        val suffix = smoke.name.replaceFirstChar { it.uppercase() }

        /*
         * The launcher script. The environment's runner configuration points at it, so the server
         * runs on this version's JDK rather than on the project's toolchain.
         */
        val launcherScript = layout.buildDirectory.file("smoke/${smoke.name}/launcher.sh")
        val launcher = tasks.register("smokeLauncher$suffix") {
            val javaLauncher = javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(smoke.javaVersion))
            }

            inputs.property("minecraftVersion", smoke.minecraftVersion)
            inputs.property("javaVersion", smoke.javaVersion)
            outputs.file(launcherScript)

            doLast {
                val script = launcherScript.get().asFile
                script.parentFile.mkdirs()
                script.writeText(
                    smokeLauncherScript(
                        javaLauncher.get().executablePath.asFile.absolutePath,
                        smoke.refusesNoGui,
                    )
                )
                script.setExecutable(true)
            }
        }

        /* The Paper build, downloaded before the provision task looks for a server jar. */
        val serverJar = File(runDir, "server.jar")
        val versionMarker = File(runDir, ".minecraft-version")
        val paper = tasks.register("smokePaper$suffix") {
            inputs.property("minecraftVersion", smoke.minecraftVersion)
            outputs.file(serverJar)

            doLast {
                downloadNewestPaperBuild(smoke.minecraftVersion, serverJar, versionMarker)
                logger.lifecycle("Paper ${smoke.minecraftVersion} downloaded to $serverJar")
            }
        }

        val clean = tasks.named("plugwrightClean$suffix")

        /*
         * The server properties the versions that need them run with. The provision task rewrites
         * the keys it knows about and leaves the rest alone, so writing this first is enough.
         */
        val properties = File(runDir, "server.properties")
        val serverProperties = tasks.register("smokeServerProperties$suffix") {
            outputs.file(properties)

            doLast {
                if (smoke.needsNioTransport) {
                    writeLegacyServerProperties(properties)
                }
            }
        }

        tasks.named("plugwrightProvision$suffix") {
            dependsOn(paper)
            dependsOn(serverProperties)
        }
        paper.configure {
            mustRunAfter(clean)
        }
        serverProperties.configure {
            mustRunAfter(clean)
        }

        val test = tasks.named<PlugwrightTestTask>("plugwrightTest$suffix")
        test.configure {
            dependsOn(launcher)

            /*
             * Only the smoke spec runs here. The rest of the suite is written against a modern
             * server and its expectations do not hold on 1.8.8.
             */
            testFiles.set("smoke")

            /*
             * The server runs on the launcher script instead of on the JVM of the project's Java
             * toolchain, and the bot connects with the protocol version mineflayer knows. Everything
             * else in the environment configuration is plugwright's own, so only those two change.
             */
            environmentConfig.set(
                providers.provider {
                    val node = ConfigNodeBuilder()
                    @Suppress("UNCHECKED_CAST")
                    (entry.mode as PlugwrightMode<EnvironmentSpec>).serialize(entry.spec as EnvironmentSpec, node)

                    val original = node.build()
                    original.copy(
                        entries = original.entries + mapOf(
                            "javaPath" to ConfigValue.Str(launcherScript.get().asFile.absolutePath),
                            "minecraftVersion" to ConfigValue.Str(smoke.botVersion),
                        )
                    )
                }
            )
        }

        if (smoke.botless) {
            /*
             * No bot can be driven on this version, so the bot-driven task has nothing to do. The
             * environment stays, because it is what provisions the server the check below runs.
             */
            test.configure {
                enabled = false
            }

            tasks.register("smokeServerCheck$suffix") {
                group = "verification"
                description =
                    "Checks that Guilds loads and finds its providers on Minecraft ${smoke.minecraftVersion}."

                dependsOn(launcher)
                dependsOn(tasks.named("plugwrightProvision$suffix"))

                doLast {
                    checkServerLoads(smoke, launcherScript.get().asFile, runDir)
                }
            }.also { smokeServerChecks.add(it) }
        }

        test
    }

    tasks.register("plugwrightSmokeTest") {
        group = "verification"
        description = "Runs the runtime smoke suite on every supported Minecraft version."

        dependsOn(smokeTestTasks)
        dependsOn(smokeServerChecks)
    }

    /*
     * One server at a time. Every environment binds the same port, and two servers on one port
     * means one of them fails to bind and the run fails for the wrong reason.
     */
    smokeTestTasks.zipWithNext().forEach { (earlier, later) ->
        later.configure { mustRunAfter(earlier) }
    }

    val matrix = tasks.named("plugwrightTest")
    smokeTestTasks.forEach { smokeTask ->
        smokeTask.configure { mustRunAfter(matrix) }
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
