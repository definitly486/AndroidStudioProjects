import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.noip"
    compileSdk = 36

    // ←←← ОБЯЗАТЕЛЬНО ВКЛЮЧАЕМ ГЕНЕРАЦИЮ BuildConfig ←←←
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.noip"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

// ────────────────────────────────
// Чтение secrets.properties → BuildConfig
// ────────────────────────────────
val secretsFile = rootProject.file("secrets.properties")
val secrets = Properties().apply {
    if (secretsFile.exists()) {
        secretsFile.inputStream().use { load(it) }
    }
}

android.defaultConfig {
    buildConfigField("String", "NOIP_USERNAME", "\"${secrets.getProperty("NOIP_USERNAME") ?: "default_user"}\"")
    buildConfigField("String", "NOIP_PASSWORD", "\"${secrets.getProperty("NOIP_PASSWORD") ?: "default_token"}\"")
    buildConfigField("String", "NOIP_HOSTNAME", "\"${secrets.getProperty("NOIP_HOSTNAME") ?: "example.ddns.net"}\"")
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}