plugins {
    id("androidarchsample.android.library")
    id("androidarchsample.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.marwinka.androidarchsample.feature.characters.data"

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:settings"))
    implementation(project(":feature:characters:domain"))

    // api: entities and DAOs are compiled into AppDatabase declared in :app
    api(libs.room.runtime)
    implementation(libs.room.ktx)

    testImplementation(project(":core:testing"))
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    kspTest(libs.room.compiler)
}
