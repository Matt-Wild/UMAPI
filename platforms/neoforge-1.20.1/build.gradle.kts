plugins {
    id("net.neoforged.gradle.userdev") version "7.1.38"
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
    implementation("net.neoforged:forge:1.20.1-47.1.106")
    compileOnly(project(":api"))
}

tasks.jar {
    dependsOn(project(":api").tasks.named("classes"))
    from(project(":api").extensions.getByType<SourceSetContainer>().named("main").map { it.output })
}

val modVersion = version.toString()

tasks.processResources {
    val resourceProperties = mapOf("version" to modVersion)
    inputs.properties(resourceProperties)

    filesMatching("META-INF/mods.toml") {
        expand(resourceProperties)
    }
}
