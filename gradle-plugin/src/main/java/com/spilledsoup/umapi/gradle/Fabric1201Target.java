package com.spilledsoup.umapi.gradle;

import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.util.List;

final class Fabric1201Target {
    private static final String MINECRAFT_VERSION = "1.20.1";
    private static final String PLATFORM_ARTIFACT_ID = "fabric-1.20.1";
    private static final String FABRIC_LOADER_VERSION = "0.19.3";
    private static final String FABRIC_LOADER_DEPENDENCY = ">=0.15.0";
    private static final String JAVA_DEPENDENCY = ">=17";
    private static final String UMAPI_DEPENDENCY = "*";
    private static final String GENERATED_RESOURCES_PATH = "generated/resources/umapi";
    private static final String GENERATE_RESOURCES_TASK = "generateUMAPIResources";

    private Fabric1201Target() {
    }

    static boolean supports(String minecraftVersion) {
        return MINECRAFT_VERSION.equals(minecraftVersion);
    }

    static String declaration() {
        return "fabric(\"" + MINECRAFT_VERSION + "\")";
    }

    static void configure(
            Project project,
            String umapiVersion,
            UMAPIModExtension mod,
            String minecraftVersion
    ) {
        configureDependencies(project, umapiVersion, minecraftVersion);
        configureGeneratedResources(project, mod, minecraftVersion);
    }

    private static void configureDependencies(
            Project project,
            String umapiVersion,
            String minecraftVersion
    ) {
        project.getDependencies().add(
                "minecraft",
                "com.mojang:minecraft:" + minecraftVersion
        );

        var loom = project.getExtensions().getByType(LoomGradleExtensionAPI.class);

        project.getDependencies().add(
                "mappings",
                loom.officialMojangMappings()
        );

        project.getDependencies().add(
                "modImplementation",
                "com.spilledsoup.umapi:" + PLATFORM_ARTIFACT_ID + ":" + umapiVersion
        );

        project.getDependencies().add(
                "modImplementation",
                "net.fabricmc:fabric-loader:" + FABRIC_LOADER_VERSION
        );
    }

    private static void configureGeneratedResources(
            Project project,
            UMAPIModExtension mod,
            String minecraftVersion
    ) {
        var generatedResourcesDirectory = project.getLayout()
                .getBuildDirectory()
                .dir(GENERATED_RESOURCES_PATH);

        var generateResources = project.getTasks().register(
                GENERATE_RESOURCES_TASK,
                GenerateFabricModJsonTask.class,
                task -> {
                    task.getOutputDirectory().set(generatedResourcesDirectory);
                    task.getModId().set(project.provider(mod::getId));
                    task.getModVersion().set(project.provider(() -> project.getVersion().toString()));
                    task.getModName().set(project.provider(mod::getName));
                    task.getModDescription().set(project.provider(mod::getDescription));
                    task.getModAuthors().set(project.provider(() -> List.copyOf(mod.getAuthors())));
                    task.getModEntrypoint().set(project.provider(mod::getEntrypoint));
                    task.getMinecraftVersion().set(minecraftVersion);
                    task.getFabricLoaderDependency().set(FABRIC_LOADER_DEPENDENCY);
                    task.getJavaDependency().set(JAVA_DEPENDENCY);
                    task.getUMAPIDependency().set(UMAPI_DEPENDENCY);
                }
        );

        project.getExtensions().configure(
                SourceSetContainer.class,
                sourceSets -> sourceSets.named("main", main ->
                        main.getResources().srcDir(generatedResourcesDirectory)
                )
        );

        project.getTasks()
                .withType(ProcessResources.class)
                .configureEach(task -> task.dependsOn(generateResources));
    }
}
