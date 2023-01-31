enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "Guilds"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.racci.dev/releases") { mavenContent { releasesOnly() } }
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories.maven("https://repo.racci.dev/releases") { mavenContent { releasesOnly() } }

    versionCatalogs.create("libMinix") {
        val build: String by settings
        val kotlinVersion: String by settings
        val conventions = "$kotlinVersion-$build"
        from("dev.racci:catalog:$conventions")
    }
}