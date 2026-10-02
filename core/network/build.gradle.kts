plugins {
    id("androidarchsample.android.library")
    id("androidarchsample.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.marwinka.androidarchsample.core.network"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // api: AppError is part of toAppError()'s public signature
    api(project(":core:common"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit)
}
