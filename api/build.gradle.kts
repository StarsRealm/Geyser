plugins {
    // Allow blossom to mark sources root of templates
    idea
    id("geyser.publish-conventions")
    id("maven-publish")
    alias(libs.plugins.blossom)
}

dependencies {
    api(libs.base.api)
    api(libs.math)
}


version = property("version")!!
val apiVersion = (version as String).removeSuffix("-SNAPSHOT")

sourceSets {
    main {
        blossom {
            javaSources {
                property("version", apiVersion)
            }
        }
    }
}


publishing {
    repositories {
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
    publications {
        create("gpr", MavenPublication::class.java) {
            from(components.getByName("java"))
            groupId = project.group.toString()
        }
    }
}
