plugins {
    id("androidarchsample.android.library")
    id("androidarchsample.hilt")
}

android {
    namespace = "ru.marwinka.androidarchsample.core.settings"
}

dependencies {
    // api: DataStore<Preferences> is what features inject
    api(libs.androidx.datastore.preferences)
}
