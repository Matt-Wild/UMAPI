plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

extensions.configure<SourceSetContainer> {
    named("main") {
        java.srcDir("../shared/common/src/main/java")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:1.18.2")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:0.19.3")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.77.0+1.18.2")

    implementation(project(":api"))
}

loom {
    runs {
        named("client") {
            runDir("runs/fabric1182Client")
        }
        named("server") {
            runDir("runs/fabric1182Server")
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
    useUMAPIRunDirectory("fabric1182Client")
}

tasks.named<JavaExec>("runServer") {
    useUMAPIRunDirectory("fabric1182Server")
}

val modVersion = version.toString()

tasks.processResources {
    val resourceProperties = mapOf("version" to modVersion)
    inputs.properties(resourceProperties)

    filesMatching("fabric.mod.json") {
        expand(resourceProperties)
    }
}
