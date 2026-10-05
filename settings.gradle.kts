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

rootProject.name = "AndroidArchSample"

include(":app")
include(":core:common")
include(":core:network")
include(":core:settings")
include(":core:testing")
include(":design-system")
include(":feature:characters:domain")
include(":feature:characters:data")
include(":feature:characters:presentation")
