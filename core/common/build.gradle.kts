plugins {
    id("androidarchsample.android.library")
    id("androidarchsample.hilt")
}

android {
    namespace = "ru.marwinka.androidarchsample.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
}
