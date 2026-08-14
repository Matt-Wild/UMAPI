import net.neoforged.moddevgradle.dsl.NeoForgeExtension

plugins {
    `java-library`
    id("net.neoforged.moddev") version "2.0.143" apply false
}

disableModDevGradleIdeIntegration()
pluginManager.apply("net.neoforged.moddev")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

extensions.configure<SourceSetContainer> {
    named("main") {
        java.srcDir("../shared/common/src/main/java")
        java.srcDir("../shared/neoforge-1.20.4-plus/src/main/java")
        java.srcDir("../shared/neoforge-player-gameprofile-getname/src/main/java")
    }
}

dependencies {
    compileOnly(project(":api"))
}

extensions.configure<NeoForgeExtension>("neoForge") {
    enable {
        version = "21.3.96"
        setDisableRecompilation(true)
    }

    mods {
        create("umapi") {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        create("neoForge1213Client") {
            client()
            sourceSet.set(sourceSets.main.get())
            gameDirectory.set(layout.projectDirectory.dir("runs/neoForge1213Client"))
        }

        create("neoForge1213Server") {
            server()
            sourceSet.set(sourceSets.main.get())
            gameDirectory.set(layout.projectDirectory.dir("runs/neoForge1213Server"))
        }
    }
}

fun Project.disableModDevGradleIdeIntegration() {
    if (extensions.findByName("mdgInternalIdeIntegration") != null) {
        return
    }

    val classLoader = NeoForgeExtension::class.java.classLoader
    val brandingClass = classLoader.loadClass("net.neoforged.moddevgradle.internal.Branding")
    val branding = brandingClass.getField("MDG").get(null)
    val noIdeIntegrationClass = classLoader.loadClass("net.neoforged.moddevgradle.internal.NoIdeIntegration")
    val constructor = noIdeIntegrationClass.getDeclaredConstructor(Project::class.java, brandingClass)
    constructor.isAccessible = true

    extensions.add(
        "mdgInternalIdeIntegration",
        constructor.newInstance(this, branding)
    )
}

tasks.jar {
    dependsOn(project(":api").tasks.named("classes"))
    from(project(":api").extensions.getByType<SourceSetContainer>().named("main").map { it.output })
}

val modVersion = version.toString()

tasks.processResources {
    val resourceProperties = mapOf("version" to modVersion)
    inputs.properties(resourceProperties)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(resourceProperties)
    }
}
