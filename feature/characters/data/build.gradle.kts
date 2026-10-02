plugins {
    id("androidarchsample.android.library")
    id("androidarchsample.hilt")
}

android {
    namespace = "ru.marwinka.androidarchsample.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:preferences"))
    implementation(project(":feature:characters:domain"))

    implementation(libs.kotlinx.coroutines.android)
    // api: entities and DAOs are compiled into AppDatabase declared in :app
    api(libs.room.runtime)
    implementation(libs.room.ktx)

    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":core:testing"))
}
