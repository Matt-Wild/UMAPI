import net.minecraftforge.gradle.MinecraftExtensionForProject
import net.minecraftforge.gradle.shadow.net.minecraftforge.gradleutils.shared.ToolsExtension
import net.minecraftforge.renamer.gradle.RenamerExtension

plugins {
    java
    id("net.minecraftforge.gradle") version "7.0.31"
    id("net.minecraftforge.renamer") version "1.1.5"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

extensions.configure<SourceSetContainer> {
    named("main") {
        java.srcDir("../shared/common/src/main/java")
        java.srcDir("../shared/forge-1.20.x/src/main/java")
    }
}

extensions.getByType(ToolsExtension::class.java).configure("slimelauncher") {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
    })
}

val forge = extensions.getByType(MinecraftExtensionForProject::class.java).run {
    mappings("official", "1.20.1")
    val forgeDependency = dependency("net.minecraftforge:forge:1.20.1-47.4.10")
    mavenizer(repositories)
    forgeDependency
}

dependencies {
    compileOnly(project(":api"))
    addProvider("implementation", forge.asProvider())
}

tasks.jar {
    dependsOn(project(":api").tasks.named("classes"))
    from(project(":api").extensions.getByType<SourceSetContainer>().named("main").map { it.output })
}

extensions.getByType(RenamerExtension::class.java).classes(
    "reobfJar",
    tasks.named<Jar>("jar")
) {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(17)
    })
    mappings(forge.getToSrg())
}

val modVersion = version.toString()

tasks.processResources {
    val resourceProperties = mapOf("version" to modVersion)
    inputs.properties(resourceProperties)

    filesMatching("META-INF/mods.toml") {
        expand(resourceProperties)
    }
}
