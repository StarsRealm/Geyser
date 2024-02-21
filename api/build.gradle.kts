plugins {
    id("geyser.publish-conventions")
    id("maven-publish")
}

dependencies {
    api(libs.base.api)
    api(libs.math)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/StarsRealm/Packages")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        create("gpr", MavenPublication::class.java) {
            from(components.getByName("java"))
            groupId = project.group.toString()
        }
    }
}