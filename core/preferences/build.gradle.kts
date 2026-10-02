plugins {
    id("androidarchsample.android.library")
    id("androidarchsample.hilt")
}

android {
    namespace = "ru.marwinka.androidarchsample.core.preferences"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    // api: DataStore<Preferences> is part of UserPreferences' public constructor
    api(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(project(":core:testing"))
}
