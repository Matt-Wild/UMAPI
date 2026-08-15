plugins {
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

extensions.configure<SourceSetContainer> {
    named("main") {
        java.srcDir("../shared/common/src/main/java")
        java.srcDir("../shared/fabriclike-1.19.2-plus/src/main/java")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:26.1.2")

    implementation("net.fabricmc:fabric-loader:0.19.3")
    implementation("net.fabricmc.fabric-api:fabric-api:0.155.2+26.1.2")

    implementation(project(":api"))
}

loom {
    runs {
        named("client") {
            runDir("runs/fabric2612Client")
        }
        named("server") {
            runDir("runs/fabric2612Server")
        }
    }
}

fun JavaExec.useUMAPIRunDirectory(name: String) {
    val runDirectory = project.layout.projectDirectory.dir("runs/$name").asFile
    workingDir = runDirectory
    doFirst {
        workingDir = runDirectory
        runDirectory.mkdirs()
    }
}

tasks.named<JavaExec>("runClient") {
    useUMAPIRunDirectory("fabric2612Client")
}

tasks.named<JavaExec>("runServer") {
    useUMAPIRunDirectory("fabric2612Server")
}

val modVersion = version.toString()

tasks.processResources {
    val resourceProperties = mapOf("version" to modVersion)
    inputs.properties(resourceProperties)

    filesMatching("fabric.mod.json") {
        expand(resourceProperties)
    }
}
