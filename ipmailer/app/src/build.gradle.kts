plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.ipmailer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ipmailer"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // Это важно для WorkManager + Coroutines
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Почта
    implementation(libs.android.mail)
    implementation(libs.android.activation)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Опционально: если хочешь UI на Compose
    // implementation(platform(libs.androidx.compose.bom))
    // implementation("androidx.compose.ui:ui")
    // implementation("androidx.compose.material3:material3")
}