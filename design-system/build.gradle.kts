plugins {
    id("androidarchsample.android.library")
    id("androidarchsample.android.compose")
}

android {
    namespace = "ru.marwinka.androidarchsample.designsystem"

    buildFeatures {
        compose = true
    }
}
