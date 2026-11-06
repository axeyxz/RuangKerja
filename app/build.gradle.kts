plugins {
    id("com.android.application")
}

android {
    namespace = "com.axey.ruangkerja"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.axey.ruangkerja"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
