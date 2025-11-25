// app/build.gradle.kts — 100% рабочая версия без Version Catalog

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // ←←← ВОТ ЭТОТ БЛОК УБИРАЕТ ПРЕДУПРЕЖДЕНИЕ
    packaging {
        resources {
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE"
        }
    }
}

dependencies {
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Почта
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
implementation("androidx.appcompat:appcompat:1.7.0")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}