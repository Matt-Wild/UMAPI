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
    minecraft("com.mojang:minecraft:1.20.1")
    mappings(loom.officialMojangMappings())

    modImplementation("org.quiltmc:quilt-loader:0.29.2")
    modImplementation("org.quiltmc.quilted-fabric-api:quilted-fabric-api:7.7.0+0.92.2-1.20.1")

    implementation(project(":api"))
}

loom {
    runs {
        named("client") {
            runDir("runs/quilt1201Client")
        }
        named("server") {
            runDir("runs/quilt1201Server")
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
    useUMAPIRunDirectory("quilt1201Client")
}

tasks.named<JavaExec>("runServer") {
    useUMAPIRunDirectory("quilt1201Server")
}

val modVersion = version.toString()

tasks.processResources {
    val resourceProperties = mapOf("version" to modVersion)
    inputs.properties(resourceProperties)

    filesMatching("quilt.mod.json") {
        expand(resourceProperties)
    }
}
