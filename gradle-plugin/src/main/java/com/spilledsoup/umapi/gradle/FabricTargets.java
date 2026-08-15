package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

final class FabricTargets {
    private static final UMAPILoader LOADER = UMAPILoader.FABRIC;
    private static final String GENERATED_RESOURCES_PATH = "generated/resources/umapi";
    private static final String GENERATE_RESOURCES_TASK = "generateUMAPIResources";

    private FabricTargets() {
    }

    static UMAPIRuntimeTarget configure(
            Project project,
            String umapiVersion,
            UMAPIModExtension mod,
            String minecraftVersion
    ) {
        var definition = UMAPITargetCatalog.fabric(minecraftVersion);
        var target = definition.target();
        var descriptor = target.descriptor();

        configureDependencies(project, umapiVersion, definition);
        configureGeneratedResources(project, mod, definition);
        configureExport(project, mod, definition, descriptor);
        configureRuntime(project, mod, target, descriptor);

        return descriptor.runtimeTarget();
    }

    private static void configureDependencies(
            Project project,
            String umapiVersion,
            UMAPITargetCatalog.FabricTarget definition
    ) {
        configureJava(project, definition.javaLanguageVersion());

        UMAPILoomTargetSupport.configureLoomModDependencies(
                project,
                umapiVersion,
                definition.target(),
                definition.loomPluginId(),
                LOADER,
                definition.fabricLoaderDependencyNotation(),
                definition.dependencyConfiguration(),
                definition.useExplicitOfficialMojangMappings()
        );
    }

    private static void configureGeneratedResources(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetCatalog.FabricTarget definition
    ) {
        var generatedResourcesDirectory = UMAPIGeneratedResources.directory(
                project,
                GENERATED_RESOURCES_PATH
        );

        var generateResources = project.getTasks().register(
                GENERATE_RESOURCES_TASK,
                GenerateFabricModJsonTask.class,
                task -> {
                    task.getOutputDirectory().set(generatedResourcesDirectory);
                    task.getModId().set(project.provider(mod::getId));
                    task.getModVersion().set(project.provider(() -> project.getVersion().toString()));
                    task.getModName().set(project.provider(mod::getName));
                    task.getModDescription().set(project.provider(mod::getDescription));
                    task.getModAuthors().set(project.provider(() -> java.util.List.copyOf(mod.getAuthors())));
                    task.getModEntrypoint().set(project.provider(mod::getEntrypoint));
                    task.getMinecraftVersion().set(definition.target().minecraftVersion());
                    task.getFabricLoaderDependency().set(definition.fabricLoaderDependency());
                    task.getJavaDependency().set(definition.javaDependency());
                    task.getUMAPIDependency().set(UMAPITargetCatalog.UMAPI_ANY_DEPENDENCY);
                }
        );

        var generateContentResources = UMAPIContentResources.register(
                project,
                mod,
                definition.target().minecraftVersion(),
                generatedResourcesDirectory
        );

        UMAPIGeneratedResources.wireMainResources(
                project,
                generatedResourcesDirectory,
                java.util.List.of(generateResources, generateContentResources)
        );

        UMAPIGeneratedResources.cleanStaleCompiledLoaderMetadataBeforeJar(project);
    }

    private static void configureExport(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetCatalog.FabricTarget definition,
            UMAPITargetDescriptor target
    ) {
        UMAPIExportTasks.registerJarExport(
                project,
                mod,
                target,
                project.getTasks().named(definition.exportJarTaskName()),
                "Exports the Fabric " + target.minecraftVersion() + " mod jar to the UMAPI exports directory."
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

    private static void configureJava(Project project, int javaLanguageVersion) {
        project.getExtensions().configure(
                JavaPluginExtension.class,
                java -> java.getToolchain()
                        .getLanguageVersion()
                        .set(JavaLanguageVersion.of(javaLanguageVersion))
        );
    }
}
