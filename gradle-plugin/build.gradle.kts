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

dependencies {
    implementation(project(":api"))
    implementation("net.neoforged.gradle.userdev:net.neoforged.gradle.userdev.gradle.plugin:7.1.38")
    implementation("net.neoforged.moddev:net.neoforged.moddev.gradle.plugin:2.0.143")
    implementation("net.minecraftforge.gradle:net.minecraftforge.gradle.gradle.plugin:7.0.31")
    implementation("net.minecraftforge:renamer-gradle:1.1.5")
}

val umapiVersion = version.toString()

tasks.processResources {
    val resourceProperties = mapOf("version" to umapiVersion)
    inputs.properties(resourceProperties)

    filesMatching("umapi.properties") {
        expand(resourceProperties)
    }
}
