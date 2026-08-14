package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.List;

final class UMAPIForgeFamilyTargetSupport {

    private UMAPIForgeFamilyTargetSupport() {
    }

    static TaskProvider<GenerateForgeFamilyModsTomlTask> registerGeneratedResources(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetCatalog.ForgeFamilyTarget definition,
            Provider<Directory> generatedResourcesDirectory,
            String taskName
    ) {
        return project.getTasks().register(
                taskName,
                GenerateForgeFamilyModsTomlTask.class,
                task -> {
                    task.getOutputDirectory().set(generatedResourcesDirectory);
                    task.getModId().set(project.provider(mod::getId));
                    task.getModVersion().set(project.provider(() -> project.getVersion().toString()));
                    task.getModName().set(project.provider(mod::getName));
                    task.getModDescription().set(project.provider(mod::getDescription));
                    task.getModAuthors().set(project.provider(() -> List.copyOf(mod.getAuthors())));
                    task.getMinecraftVersionRange().set(definition.minecraftVersionRange());
                    task.getLoaderDependencyModId().set(definition.loaderDependencyModId());
                    task.getForgeVersionRange().set(definition.forgeVersionRange());
                    task.getLoaderVersionRange().set(definition.loaderVersionRange());
                    task.getUseModernDependencyType().set(definition.useModernDependencyType());
                    task.getUMAPIVersionRange().set(UMAPITargetCatalog.UMAPI_FORGE_FAMILY_VERSION_RANGE);
                    task.getPackFormat().set(definition.resourcePackFormat());
                    if (definition.resourcePackMinFormat() != null && definition.resourcePackMaxFormat() != null) {
                        task.getPackMinFormat().set(definition.resourcePackMinFormat());
                        task.getPackMaxFormat().set(definition.resourcePackMaxFormat());
                    }
                    task.getPackDescription().set(project.provider(() -> mod.getName() + " resources"));
                    task.getMetadataFileName().set(definition.metadataFileName());
                }
        );
    }

    static void configureGeneratedEntrypoint(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetCatalog.ForgeFamilyTarget definition,
            Provider<Directory> generatedSourcesDirectory,
            String taskName,
            String className
    ) {
        var generateEntrypoint = project.getTasks().register(
                taskName,
                GenerateForgeFamilyEntrypointTask.class,
                task -> {
                    task.getOutputDirectory().set(generatedSourcesDirectory);
                    task.getModId().set(project.provider(mod::getId));
                    task.getModEntrypoint().set(project.provider(mod::getEntrypoint));
                    task.getEntrypointPackage().set(definition.generatedEntrypointPackage());
                    task.getEntrypointClassName().set(className);
                    task.getFmlPackageRoot().set(definition.fmlPackageRoot());
                    task.getModEventBusParameterType().set(definition.modEventBusParameterType());
                }
        );

        project.getExtensions().configure(
                SourceSetContainer.class,
                sourceSets -> sourceSets.named("main", main ->
                        main.getJava().srcDir(generatedSourcesDirectory)
                )
        );

        project.getTasks()
                .withType(JavaCompile.class)
                .configureEach(task -> task.dependsOn(generateEntrypoint));
    }

    static String generatedPath(String basePath, UMAPITargetDefinition target) {
        return basePath
                + "/"
                + UMAPIMinecraftVersion.compact(target.minecraftVersion());
    }

    static String generatedJavaPath(String basePath, UMAPITargetDefinition target) {
        return generatedPath(basePath, target) + "/java";
    }
}
