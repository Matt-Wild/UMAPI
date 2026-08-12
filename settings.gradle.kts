pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
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
