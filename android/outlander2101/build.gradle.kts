plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.autodiag.outlander2101"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.autodiag.outlander2101"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
    }
}

kotlin {
    jvmToolchain(17)
}
