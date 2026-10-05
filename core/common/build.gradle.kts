plugins {
    id("androidarchsample.jvm.library")
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    // JSR-330 annotations only; Hilt bindings live in :app
    implementation(libs.javax.inject)
}
