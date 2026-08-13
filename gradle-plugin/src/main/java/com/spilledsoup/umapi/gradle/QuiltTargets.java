package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;

import java.net.URI;
import java.util.List;

final class QuiltTargets {
    private static final UMAPILoader LOADER = UMAPILoader.QUILT;
    private static final String GENERATED_RESOURCES_BASE_PATH = "generated/resources/umapi-quilt";
    private static final String GENERATE_RESOURCES_TASK = "generateUMAPIQuiltResources";

    private QuiltTargets() {
    }

    static UMAPIRuntimeTarget configure(
            Project project,
            String umapiVersion,
            UMAPIModExtension mod,
            String minecraftVersion
    ) {
        var definition = UMAPITargetCatalog.quilt(minecraftVersion);
        var target = definition.target();
        var descriptor = target.descriptor();

        configureDependencies(project, umapiVersion, definition);
        configureGeneratedResources(project, mod, definition);
        configureExport(project, mod, descriptor);
        configureRuntime(project, mod, target, descriptor);

        return descriptor.runtimeTarget();
    }

    private static void configureDependencies(
            Project project,
            String umapiVersion,
            UMAPITargetCatalog.QuiltTarget definition
    ) {
        project.getRepositories().maven(repository -> {
            repository.setName("Quilt");
            repository.setUrl(URI.create("https://maven.quiltmc.org/repository/release/"));
        });
        project.getRepositories().maven(repository -> {
            repository.setName("Fabric");
            repository.setUrl(URI.create("https://maven.fabricmc.net/"));
        });

        UMAPILoomTargetSupport.configureLoomModDependencies(
                project,
                umapiVersion,
                definition.target(),
                UMAPITargetCatalog.QUILT_LOOM_PLUGIN,
                LOADER,
                definition.quiltLoaderDependencyNotation()
        );

        project.getDependencies().add("modImplementation", definition.quiltedFabricApiDependencyNotation());
    }

    private static void configureGeneratedResources(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetCatalog.QuiltTarget definition
    ) {
        var target = definition.target();
        var generatedResourcesDirectory = UMAPIGeneratedResources.directory(
                project,
                generatedResourcesPath(target)
        );

        var generateResources = project.getTasks().register(
                GENERATE_RESOURCES_TASK,
                GenerateQuiltModJsonTask.class,
                task -> {
                    task.getOutputDirectory().set(generatedResourcesDirectory);
                    task.getModGroup().set(project.provider(() -> project.getGroup().toString()));
                    task.getModId().set(project.provider(mod::getId));
                    task.getModVersion().set(project.provider(() -> project.getVersion().toString()));
                    task.getModName().set(project.provider(mod::getName));
                    task.getModDescription().set(project.provider(mod::getDescription));
                    task.getModAuthors().set(project.provider(() -> List.copyOf(mod.getAuthors())));
                    task.getModEntrypoint().set(project.provider(mod::getEntrypoint));
                    task.getMinecraftVersion().set(target.minecraftVersion());
                    task.getQuiltLoaderDependency().set(definition.quiltLoaderDependency());
                    task.getJavaDependency().set(UMAPITargetCatalog.JAVA_17_DEPENDENCY);
                    task.getUMAPIDependency().set(UMAPITargetCatalog.UMAPI_ANY_DEPENDENCY);
                }
        );

        UMAPIGeneratedResources.wireMainResources(
                project,
                generatedResourcesDirectory,
                generateResources
        );
    }

    private static void configureExport(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetDescriptor target
    ) {
        UMAPIExportTasks.registerJarExport(
                project,
                mod,
                target,
                project.getTasks().named("remapJar"),
                "Exports the Quilt " + target.minecraftVersion() + " mod jar to the UMAPI exports directory."
        );
    }

    private static void configureRuntime(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetDefinition definition,
            UMAPITargetDescriptor target
    ) {
        UMAPILoomTargetSupport.configureRuntime(
                project,
                target,
                definition.clientWorkingDirectory(),
                definition.serverWorkingDirectory(),
                mod
        );
    }

    private static String generatedResourcesPath(UMAPITargetDefinition target) {
        return GENERATED_RESOURCES_BASE_PATH
                + "/"
                + UMAPIMinecraftVersion.compact(target.minecraftVersion());
    }
}
