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
        buildConfigField("long", "BUILD_TIME", "${System.currentTimeMillis()}L")
        versionNameSuffix = "-${gitBranch()}"
        buildConfigField("String", "GIT_BRANCH", "\"${gitBranch()}\"")
        buildConfigField("String", "GIT_COMMIT_SHORT", "\"$gitCommitShort\"")   // ← теперь будет!
        buildConfigField("String", "GIT_COMMIT", "\"${gitCommit()}\"")
        buildConfigField("String", "VERSION_NAME_SUFFIX", "\"${gitBranch()}\"")

     

        // По желанию можно ещё и полное versionName закинуть
        buildConfigField("String", "FULL_VERSION_NAME", "\"${defaultConfig.versionName}${versionNameSuffix ?: ""}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        android.buildFeatures.buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    }
}


val gitCommitShort = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
}.standardOutput.asText.get().trim().ifEmpty { "unknown" }

val gitCommitFull = providers.exec {
    commandLine("git", "rev-parse", "HEAD")
}.standardOutput.asText.get().trim().ifEmpty { "unknown" }

fun gitBranch(): String = try {
    providers.exec {
        commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
    }.standardOutput.asText.get().trim().let { branch ->
        if (branch.isEmpty() || branch == "HEAD") "detached" else branch
    }
} catch (e: Exception) {
    "unknown"
}

fun gitCommit(): String = try {
    providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
    }.standardOutput.asText.get().trim()
} catch (e: Exception) {
    "unknown"
}

dependencies {

    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
// Use jdk18on for modern Android (API 24+)
// Bouncy Castle OpenPGP (for bcpg)
    implementation("org.bouncycastle:bcpg-jdk18on:1.78.1")
// If you need the full PGP suite (includes openpgp)
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")

implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
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