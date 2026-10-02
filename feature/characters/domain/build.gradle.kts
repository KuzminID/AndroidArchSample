plugins {
    id("androidarchsample.android.library")
}

android {
    namespace = "ru.marwinka.androidarchsample.domain"

    testFixtures {
        enable = true
    }
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.kotlinx.coroutines.core)

    testFixturesImplementation(project(":core:common"))
    testFixturesImplementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(project(":core:testing"))
}
