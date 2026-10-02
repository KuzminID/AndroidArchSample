plugins {
    id("androidarchsample.android.library")
    id("androidarchsample.android.compose")
}

android {
    namespace = "ru.marwinka.androidarchsample.core.ui"

    buildFeatures {
        compose = true
    }
}
