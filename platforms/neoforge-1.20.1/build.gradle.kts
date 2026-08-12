plugins {
    id("net.neoforged.gradle.userdev") version "7.1.38"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

dependencies {
    implementation("net.neoforged:forge:1.20.1-47.1.106")
    implementation(project(":api"))
}

tasks.jar {
    dependsOn(project(":api").tasks.named("classes"))
    from(project(":api").extensions.getByType<SourceSetContainer>().named("main").map { it.output })
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("META-INF/mods.toml") {
        expand("version" to project.version)
    }
}
