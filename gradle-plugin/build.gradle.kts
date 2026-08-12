plugins {
    `java-gradle-plugin`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

gradlePlugin {
    plugins {
        create("umapi") {
            id = "com.spilledsoup.umapi"
            implementationClass = "com.spilledsoup.umapi.gradle.UMAPIPlugin"
        }
    }
}

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

dependencies {
    implementation("net.fabricmc:fabric-loom:1.17.19")
    implementation("net.neoforged.gradle.userdev:net.neoforged.gradle.userdev.gradle.plugin:7.1.38")
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("umapi.properties") {
        expand("version" to project.version)
    }
}
