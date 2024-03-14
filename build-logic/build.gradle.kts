plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()

    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.architectury.dev/")

    maven("https://maven.aliyun.com/repository/public")
    maven {
        name = "AliYun-Release"
        url = uri("https://packages.aliyun.com/maven/repository/2421751-release-ZmwRAc/")
        credentials {
            username = project.findProperty("aliyun.package.user") as String? ?: System.getenv("ALY_USER")
            password = project.findProperty("aliyun.package.password") as String? ?: System.getenv("ALY_PASSWORD")
        }
    }
    maven {
        name = "AliYun-Snapshot"
        url = uri("https://packages.aliyun.com/maven/repository/2421751-snapshot-i7Aufp/")
        credentials {
            username = project.findProperty("aliyun.package.user") as String? ?: System.getenv("ALY_USER")
            password = project.findProperty("aliyun.package.password") as String? ?: System.getenv("ALY_PASSWORD")
        }
    }
}

dependencies {
    implementation(libs.indra)
    implementation(libs.shadow)
    implementation(libs.architectury.plugin)
    implementation(libs.architectury.loom)
    implementation(libs.minotaur)
}
