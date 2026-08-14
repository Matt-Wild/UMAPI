pluginManagement {
    repositories {
        maven {
            name = "Quilt"
            url = uri("https://maven.quiltmc.org/repository/release/")
        }
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "NeoForged"
            url = uri("https://maven.neoforged.net/releases/")
        }
        maven {
            name = "MinecraftForge"
            url = uri("https://maven.minecraftforge.net/")
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
include("platforms:fabric-1.20.4")
include("platforms:fabric-1.20.6")
include("platforms:fabric-1.21.1")
include("platforms:fabric-1.21.3")
include("platforms:fabric-1.21.5")
include("platforms:fabric-1.21.8")
include("platforms:fabric-1.21.10")
include("platforms:fabric-1.21.11")
include("platforms:neoforge-1.20.1")
include("platforms:neoforge-1.20.4")
include("platforms:neoforge-1.20.6")
include("platforms:neoforge-1.21.1")
include("platforms:neoforge-1.21.3")
include("platforms:neoforge-1.21.5")
include("platforms:neoforge-1.21.8")
include("platforms:neoforge-1.21.10")
include("platforms:neoforge-1.21.11")
include("platforms:forge-1.20.1")
include("platforms:forge-1.20.4")
include("platforms:forge-1.20.6")
include("platforms:quilt-1.20.1")
include("platforms:quilt-1.20.4")
