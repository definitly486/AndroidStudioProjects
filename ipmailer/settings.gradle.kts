pluginManagement {
    repositories {
        google()              // ← Для AGP
        mavenCentral()
        gradlePluginPortal()  // ← Для Kotlin и других
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "IP Mailer"
include(":app")