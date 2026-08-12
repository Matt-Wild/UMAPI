pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "NeoForged"
            url = uri("https://maven.neoforged.net/releases/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "UMAPI"

include("api")
include("gradle-plugin")
include("settings-plugin")
include("platforms:fabric-1.20.1")
include("platforms:neoforge-1.20.1")
