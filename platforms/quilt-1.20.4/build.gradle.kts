plugins {
    id("org.quiltmc.loom") version "1.14.3"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

extensions.configure<SourceSetContainer> {
    named("main") {
        java.srcDir("../shared/common/src/main/java")
        java.srcDir("../shared/fabriclike-1.20.x/src/main/java")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:1.20.4")
    mappings(loom.officialMojangMappings())

    modImplementation("org.quiltmc:quilt-loader:0.29.2")
    modImplementation("org.quiltmc.quilted-fabric-api:quilted-fabric-api:9.0.0-alpha.8+0.97.0-1.20.4")

    implementation(project(":api"))
}

loom {
    runs {
        named("client") {
            runDir("runs/quilt1204Client")
        }
        named("server") {
            runDir("runs/quilt1204Server")
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
    useUMAPIRunDirectory("quilt1204Client")
}

tasks.named<JavaExec>("runServer") {
    useUMAPIRunDirectory("quilt1204Server")
}

val modVersion = version.toString()

tasks.processResources {
    val resourceProperties = mapOf("version" to modVersion)
    inputs.properties(resourceProperties)

    filesMatching("quilt.mod.json") {
        expand(resourceProperties)
    }
}
