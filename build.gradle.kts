plugins {
    base
}

allprojects {
    group = "com.spilledsoup.umapi"
    version = "0.0.1"

    repositories {
        maven {
            name = "NeoForged"
            url = uri("https://maven.neoforged.net/releases/")
        }
        mavenCentral()
    }
}
