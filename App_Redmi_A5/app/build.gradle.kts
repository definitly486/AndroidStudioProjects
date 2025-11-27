import com.android.build.api.dsl.ApkSigningConfig
import org.gradle.kotlin.dsl.debug

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Время сборки
        buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")

        // === Git-информация (вычисляется один раз, до использования) ===
        val gitBranch = gitBranch()
        val gitCommitShort = gitCommitShort()
        val gitCommitFull = gitCommitFull()

        // Суффикс версии (например: 1.0-main)
        versionNameSuffix = "-$gitBranch"

        // Добавляем в BuildConfig
        buildConfigField("String", "GIT_BRANCH", "\"$gitBranch\"")
        buildConfigField("String", "GIT_COMMIT_SHORT", "\"$gitCommitShort\"")
        buildConfigField("String", "GIT_COMMIT_FULL", "\"$gitCommitFull\"")
        buildConfigField("String", "VERSION_NAME_SUFFIX", "\"-$gitBranch\"")
        buildConfigField("String", "FULL_VERSION_NAME", "\"$versionName-$gitBranch\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            // минификация выключена — как и было
        }

        release {
            // Главное — включаем R8 (он же новый ProGuard)
            isMinifyEnabled = true          // было false → теперь true
            isShrinkResources = true        // удаляет неиспользуемые ресурсы

            isDebuggable = false            // нельзя будет отлаживать

            // Подпись — можно оставить debug-ключом (Android Studio подпишет автоматически)
            signingConfig = signingConfigs.getByName("debug")            // Убираем любые debug-суффиксы из названия пакета и версии
            applicationIdSuffix = null
            versionNameSuffix = null

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

    buildFeatures {
        viewBinding = true
        buildConfig = true // явно включаем (на всякий случай)
    }
}

// === Надёжные функции для получения Git-данных ===
private fun gitBranch(): String = runGitCommand("git rev-parse --abbrev-ref HEAD") ?: "unknown"
private fun gitCommitShort(): String = runGitCommand("git rev-parse --short HEAD") ?: "unknown"
private fun gitCommitFull(): String = runGitCommand("git rev-parse HEAD") ?: "unknown"

private fun runGitCommand(command: String): String? {
    return try {
        providers.exec {
            commandLine(command.split(" "))
            // Указываем рабочую директорию — корень проекта (важно!)
            workingDir = project.rootProject.projectDir
        }.standardOutput.asText.get().trim().takeIf { it.isNotEmpty() && it != "HEAD" }
            ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }
}

dependencies {
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("org.bouncycastle:bcpg-jdk18on:1.78.1")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation(libs.material)
    implementation("com.google.android.material:material:1.11.0")
    implementation(libs.org.eclipse.jgit)
    implementation(libs.androidx.viewpager2)
    implementation(libs.commons.compress)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.xz)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.runtime.saved.instance.state)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}