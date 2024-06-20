@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        // mavenLocal()
        
        // Floodgate, Cumulus etc.
        maven("https://repo.opencollab.dev/main")

        // Paper, Velocity
        maven("https://repo.papermc.io/repository/maven-public")
        // Spigot
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots") {
            mavenContent { snapshotsOnly() }
        }

        // BungeeCord
        maven("https://oss.sonatype.org/content/repositories/snapshots") {
            mavenContent { snapshotsOnly() }
        }

        // NeoForge
        maven("https://maven.neoforged.net/releases") {
            mavenContent { releasesOnly() }
        }

        // Minecraft
        maven("https://libraries.minecraft.net") {
            name = "minecraft"
            mavenContent { releasesOnly() }
        }

        mavenCentral()

        // ViaVersion
        maven("https://repo.viaversion.com") {
            name = "viaversion"
        }

        maven("https://jitpack.io") {
            content { includeGroupByRegex("com\\.github\\..*") }
        }

        // For Adventure snapshots
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")

        maven("https://maven.aliyun.com/repository/public")
        maven {
            name = "AliYun-Release"
            url = uri("https://packages.aliyun.com/maven/repository/2421751-release-ZmwRAc/")
            credentials {
                username = System.getenv("ALY_USER")
                password = System.getenv("ALY_PASSWORD")
            }
        }
        maven {
            name = "AliYun-Snapshot"
            url = uri("https://packages.aliyun.com/maven/repository/2421751-snapshot-i7Aufp/")
            credentials {
                username = System.getenv("ALY_USER")
                password = System.getenv("ALY_PASSWORD")
            }
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()

        maven("https://repo.opencollab.dev/maven-snapshots/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases")
    }
    includeBuild("build-logic")
}

rootProject.name = "geyser-parent"

include(":ap")
include(":api")
include(":standalone")
include(":velocity")
include(":common")
include(":core")

project(":standalone").projectDir = file("bootstrap/standalone")
project(":velocity").projectDir = file("bootstrap/velocity")