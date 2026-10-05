plugins {
    id("androidarchsample.android.library")
    id("androidarchsample.hilt")
}

android {
    namespace = "ru.marwinka.androidarchsample.core.network"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // api: AppError is part of toNetworkError()'s public signature
    api(project(":core:common"))
    // api: features declare Retrofit interfaces and @Serializable DTOs against these
    api(libs.retrofit.core)
    api(libs.kotlinx.serialization.json)

    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
}
