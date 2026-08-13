plugins {
    base
}

allprojects {
    group = "com.spilledsoup.umapi"
    version = "0.0.1"

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
        maven {
            name = "MinecraftLibraries"
            url = uri("https://libraries.minecraft.net/")
        }
        mavenCentral()
    }
}
