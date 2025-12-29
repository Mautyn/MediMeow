pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MediMeow"
include(":app")
include(":feature_auth")
include(":core")
include(":feature_dashboard")
include(":feature_medicine_availability")
include(":feature_interactions")
include(":feature_settings")
include(":feature_medical_records")
include(":feature_profile")
include(":feature_share")
