pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "AndroidArchSample"

include(":app")
include(":core:common")
include(":core:ui")
include(":core:network")
include(":core:preferences")
include(":core:testing")
include(":feature:characters:domain")
include(":feature:characters:data")
include(":feature:characters:presentation")
