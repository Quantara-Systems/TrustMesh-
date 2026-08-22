plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.trustmesh.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore Settings
    implementation(libs.androidx.datastore.preferences)

    // Security & Biometric
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.biometric)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Network API Calls
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    // Plaid SDK
    implementation(libs.plaid.link)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}
