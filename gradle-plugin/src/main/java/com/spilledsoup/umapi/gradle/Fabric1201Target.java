package com.spilledsoup.umapi.gradle;

import net.fabricmc.loom.api.LoomGradleExtensionAPI;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.util.List;
import java.util.Set;

final class Fabric1201Target {
    private static final String MINECRAFT_VERSION = "1.20.1";
    private static final String PLATFORM_ARTIFACT_ID = "fabric-1.20.1";
    private static final String FABRIC_LOADER_VERSION = "0.19.3";
    private static final String LOADER_ID = "fabric";
    private static final String RUNTIME_ID = "fabric-1.20.1";
    private static final String FABRIC_LOADER_DEPENDENCY = ">=0.15.0";
    private static final String JAVA_DEPENDENCY = ">=17";
    private static final String UMAPI_DEPENDENCY = "*";
    private static final String GENERATED_RESOURCES_PATH = "generated/resources/umapi";
    private static final String GENERATE_RESOURCES_TASK = "generateUMAPIResources";
    private static final String EXPORT_DIRECTORY = "umapi/exports";
    private static final String EXPORT_TASK = "exportUMAPI";
    private static final String EXPORT_FABRIC_1201_TASK = "exportUMAPIFabric1201";
    private static final String REMAPPED_UMAPI_CACHE_PATH = ".gradle/loom-cache/remapped_mods/remapped/com/spilledsoup/umapi";
    private static final String RUN_CLIENT_TASK = "runClient";
    private static final String RUN_SERVER_TASK = "runServer";
    private static final String RUN_FABRIC_1201_CLIENT_TASK = "runUMAPIFabric1201Client";
    private static final String RUN_FABRIC_1201_SERVER_TASK = "runUMAPIFabric1201Server";
    private static final Set<String> RUNTIME_TASKS = Set.of(
            RUN_CLIENT_TASK,
            RUN_SERVER_TASK,
            RUN_FABRIC_1201_CLIENT_TASK,
            RUN_FABRIC_1201_SERVER_TASK,
            "runUMAPIClient",
            "runUMAPIServer"
    );

    private Fabric1201Target() {
    }

    static boolean supports(String minecraftVersion) {
        return MINECRAFT_VERSION.equals(minecraftVersion);
    }

    static String declaration() {
        return "fabric(\"" + MINECRAFT_VERSION + "\")";
    }

    static UMAPIRuntimeTarget configure(
            Project project,
            String umapiVersion,
            UMAPIModExtension mod,
            String minecraftVersion
    ) {
        configureDependencies(project, umapiVersion, minecraftVersion);
        configureGeneratedResources(project, mod, minecraftVersion);
        configureExport(project, mod, minecraftVersion);
        configureRuntime(project);

        return runtimeTarget(minecraftVersion);
    }

    static UMAPIRuntimeTarget runtimeTarget(String minecraftVersion) {
        return new UMAPIRuntimeTarget(
                RUNTIME_ID,
                LOADER_ID,
                minecraftVersion,
                EXPORT_FABRIC_1201_TASK,
                RUN_FABRIC_1201_CLIENT_TASK,
                RUN_FABRIC_1201_SERVER_TASK
        );
    }

    private static void configureDependencies(
            Project project,
            String umapiVersion,
            String minecraftVersion
    ) {
        project.getPluginManager().apply("net.fabricmc.fabric-loom-remap");

        project.getDependencies().add(
                "minecraft",
                "com.mojang:minecraft:" + minecraftVersion
        );

        var loom = project.getExtensions().getByType(LoomGradleExtensionAPI.class);
        invalidateLocalRuntimeRemapCache(project);

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

    private static void invalidateLocalRuntimeRemapCache(Project project) {
        if (!isLocalUMAPIBuild(project) || !isRuntimeTaskRequested(project)) {
            return;
        }

        var cacheDirectory = project.getLayout()
                .getProjectDirectory()
                .dir(REMAPPED_UMAPI_CACHE_PATH)
                .getAsFile();

        if (cacheDirectory.exists() && project.delete(cacheDirectory)) {
            project.getLogger().lifecycle(
                    "UMAPI local development runtime detected; invalidated Fabric Loom's remapped UMAPI cache."
            );
        }
    }

    private static boolean isLocalUMAPIBuild(Project project) {
        return project.getGradle()
                .getIncludedBuilds()
                .stream()
                .anyMatch(includedBuild ->
                        "UMAPI".equalsIgnoreCase(includedBuild.getName())
                                || "UMAPI".equalsIgnoreCase(includedBuild.getProjectDir().getName())
                );
    }

    private static boolean isRuntimeTaskRequested(Project project) {
        return project.getGradle()
                .getStartParameter()
                .getTaskNames()
                .stream()
                .map(Fabric1201Target::simpleTaskName)
                .anyMatch(RUNTIME_TASKS::contains);
    }

    private static String simpleTaskName(String taskName) {
        int separator = taskName.lastIndexOf(':');

        if (separator == -1) {
            return taskName;
        }

        return taskName.substring(separator + 1);
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

    private static void configureExport(
            Project project,
            UMAPIModExtension mod,
            String minecraftVersion
    ) {
        var remapJar = project.getTasks().named("remapJar");

        var exportTarget = project.getTasks().register(
                EXPORT_FABRIC_1201_TASK,
                Copy.class,
                task -> {
                    task.setGroup("umapi");
                    task.setDescription(
                            "Exports the Fabric 1.20.1 mod jar to the UMAPI exports directory."
                    );
                    task.dependsOn(remapJar);
                    task.from(remapJar);
                    task.include("*.jar");
                    task.into(project.getLayout().getBuildDirectory().dir(EXPORT_DIRECTORY));
                    task.rename(ignored -> exportFileName(project, mod, minecraftVersion));
                }
        );

        var exportUMAPI = getOrCreateExportTask(project);
        exportUMAPI.configure(task -> task.dependsOn(exportTarget));

        project.getTasks()
                .named("assemble")
                .configure(task -> task.dependsOn(exportUMAPI));
    }

    private static void configureRuntime(Project project) {
        registerRuntimeTask(
                project,
                RUN_FABRIC_1201_CLIENT_TASK,
                RUN_CLIENT_TASK,
                "Runs the Fabric 1.20.1 UMAPI client runtime."
        );

        registerRuntimeTask(
                project,
                RUN_FABRIC_1201_SERVER_TASK,
                RUN_SERVER_TASK,
                "Runs the Fabric 1.20.1 UMAPI server runtime."
        );
    }

    private static void registerRuntimeTask(
            Project project,
            String taskName,
            String nativeTaskName,
            String description
    ) {
        project.getTasks().register(taskName, task -> {
            task.setGroup("umapi");
            task.setDescription(description);
            task.dependsOn(nativeTaskName);
        });
    }

    private static TaskProvider<Task> getOrCreateExportTask(Project project) {
        if (project.getTasks().findByName(EXPORT_TASK) != null) {
            return project.getTasks().named(EXPORT_TASK);
        }

        return project.getTasks().register(EXPORT_TASK, task -> {
            task.setGroup("umapi");
            task.setDescription(
                    "Exports all configured UMAPI target jars to the UMAPI exports directory."
            );
        });
    }

    private static String exportFileName(
            Project project,
            UMAPIModExtension mod,
            String minecraftVersion
    ) {
        return sanitizeFileNamePart(mod.getName())
                + "-v"
                + sanitizeFileNamePart(project.getVersion().toString())
                + "-fabric-mc"
                + sanitizeFileNamePart(minecraftVersion)
                + ".jar";
    }

    private static String sanitizeFileNamePart(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        String sanitized = value.trim()
                .replaceAll("[^A-Za-z0-9._-]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");

        if (sanitized.isBlank()) {
            return "unknown";
        }

        return sanitized;
    }
}
