plugins {
    id("androidarchsample.android.application")
    id("androidarchsample.android.compose")
    id("androidarchsample.hilt")
}

android {
    namespace = "ru.marwinka.androidarchsample"

    defaultConfig {
        applicationId = "ru.marwinka.androidarchsample"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets {
        // Exported Room schemas for MigrationTestHelper. Robolectric reads assets of the
        // tested variant, not of the test source set, so they go to debug only, never to release.
        getByName("debug").assets.directories.add("$projectDir/schemas")
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":design-system"))
    implementation(project(":feature:characters:domain"))
    implementation(project(":feature:characters:data"))
    implementation(project(":feature:characters:presentation"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.timber)
    // Images go through the shared OkHttpClient and its rate limit (see App)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    // Dispatchers.Main for DefaultDispatcherProvider bound in CommonModule
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(project(":core:testing"))
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.room.testing)
}
