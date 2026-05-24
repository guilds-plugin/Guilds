pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenCentral()

        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
            content {
                includeGroup("org.bukkit")
                includeGroup("org.spigotmc")
            }
        }

        maven("https://oss.sonatype.org/content/groups/public/")

        maven("https://repo.aikar.co/content/groups/aikar/") {
            content {
                includeGroup("co.aikar")
            }
        }

        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") {
            content {
                includeGroup("me.clip")
            }
        }

        maven("https://repo.codemc.org/repository/maven-public/") {
            content {
                includeGroup("org.codemc.worldguardwrapper")
            }
        }

        maven("https://repo.glaremasters.me/repository/public/") {
            name = "glaremasters"

            metadataSources {
                mavenPom()
                artifact()
                ignoreGradleMetadataRedirection()
            }
        }

        maven("https://repo.bxteam.org/releases") {
            name = "bxteam-releases"

            content {
                includeGroup("org.bxteam.quark")
            }
        }
    }
}

rootProject.name = "Guilds"