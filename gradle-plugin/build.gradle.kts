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
    mavenCentral()
}

dependencies {
    implementation("net.fabricmc:fabric-loom:1.17.19")
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("umapi.properties") {
        expand("version" to project.version)
    }
}
