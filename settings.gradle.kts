pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Baby Loading"
include(":app")
include(":core:network")
include(":core:storage")
include(":core:coroutines")
include(":core:designsystem")
include(":core:localization")
include(":core:pregnancy")
include(":core:pregnancy-content")
include(":feature:onboarding")
include(":feature:dashboard")
include(":feature:journey")
include(":feature:settings")
include(":feature:gallery")
