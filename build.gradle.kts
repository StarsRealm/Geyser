plugins {
    `java-library`
    // Ensure AP works in eclipse (no effect on other IDEs)
    eclipse
    id("geyser.build-logic")
    alias(libs.plugins.lombok) apply false
}

allprojects {
    group = properties["group"] as String + "." + properties["id"] as String
    version = properties["version"] as String
    description = properties["description"] as String
}

val basePlatforms = setOf(
    projects.standalone,
    projects.velocity,
).map { it.dependencyProject }

val modrinthPlatforms = setOf(
    projects.velocity
).map { it.dependencyProject }

subprojects {
    apply {
        plugin("java-library")
        plugin("io.freefair.lombok")
        plugin("geyser.build-logic")
    }

    when (this) {
        in basePlatforms -> plugins.apply("geyser.platform-conventions")
        else -> plugins.apply("geyser.base-conventions")
    }

    // Not combined with platform-conventions as that also contains
    // platforms which we cant publish to modrinth
    if (modrinthPlatforms.contains(this)) {
        plugins.apply("geyser.modrinth-uploading-conventions")
    }
}