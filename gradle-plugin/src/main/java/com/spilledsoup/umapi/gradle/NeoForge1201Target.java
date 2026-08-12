package com.spilledsoup.umapi.gradle;

import net.neoforged.gradle.dsl.common.runs.run.RunManager;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.net.URI;
import java.util.List;

final class NeoForge1201Target {
    private static final String MINECRAFT_VERSION = "1.20.1";
    private static final String PLATFORM_ARTIFACT_ID = "neoforge-1.20.1";
    private static final String NEOFORGE_VERSION = "1.20.1-47.1.106";
    private static final String LOADER_ID = "neoforge";
    private static final String RUNTIME_ID = "neoforge-1.20.1";
    private static final String MINECRAFT_VERSION_RANGE = "[1.20.1,1.21)";
    private static final String FORGE_VERSION_RANGE = "[47.1.106,)";
    private static final String LOADER_VERSION_RANGE = "[47,)";
    private static final String UMAPI_VERSION_RANGE = "[0,)";
    private static final int RESOURCE_PACK_FORMAT = 15;
    private static final String GENERATED_RESOURCES_PATH = "generated/resources/umapi-neoforge";
    private static final String GENERATED_SOURCES_PATH = "generated/sources/umapi-neoforge/java";
    private static final String GENERATE_RESOURCES_TASK = "generateUMAPINeoForgeResources";
    private static final String GENERATE_ENTRYPOINT_TASK = "generateUMAPINeoForgeEntrypoint";
    private static final String EXPORT_DIRECTORY = "umapi/exports";
    private static final String EXPORT_TASK = "exportUMAPI";
    private static final String EXPORT_NEOFORGE_1201_TASK = "exportUMAPINeoForge1201";
    private static final String NEOFORGE_1201_CLIENT_RUN = "neoForge1201Client";
    private static final String NEOFORGE_1201_SERVER_RUN = "neoForge1201Server";
    private static final String NATIVE_NEOFORGE_1201_CLIENT_TASK = "runNeoForge1201Client";
    private static final String NATIVE_NEOFORGE_1201_SERVER_TASK = "runNeoForge1201Server";
    private static final String RUN_NEOFORGE_1201_CLIENT_TASK = "runUMAPINeoForge1201Client";
    private static final String RUN_NEOFORGE_1201_SERVER_TASK = "runUMAPINeoForge1201Server";

    private NeoForge1201Target() {
    }

    static boolean supports(String minecraftVersion) {
        return MINECRAFT_VERSION.equals(minecraftVersion);
    }

    static String declaration() {
        return "neoforge(\"" + MINECRAFT_VERSION + "\")";
    }

    static UMAPIRuntimeTarget configure(
            Project project,
            String umapiVersion,
            UMAPIModExtension mod,
            String minecraftVersion
    ) {
        configureDependencies(project, umapiVersion);
        configureGeneratedResources(project, mod);
        configureGeneratedEntrypoint(project, mod);
        configureRuntime(project);
        configureExport(project, mod, minecraftVersion);

        return runtimeTarget(minecraftVersion);
    }

    static UMAPIRuntimeTarget runtimeTarget(String minecraftVersion) {
        return new UMAPIRuntimeTarget(
                RUNTIME_ID,
                LOADER_ID,
                minecraftVersion,
                EXPORT_NEOFORGE_1201_TASK,
                RUN_NEOFORGE_1201_CLIENT_TASK,
                RUN_NEOFORGE_1201_SERVER_TASK
        );
    }

    private static void configureDependencies(Project project, String umapiVersion) {
        project.getPluginManager().apply("net.neoforged.gradle.userdev");

        project.getRepositories().maven(repository -> {
            repository.setName("NeoForged");
            repository.setUrl(URI.create("https://maven.neoforged.net/releases/"));
        });

        project.getDependencies().add(
                "implementation",
                "net.neoforged:forge:" + NEOFORGE_VERSION
        );

        project.getDependencies().add(
                "implementation",
                "com.spilledsoup.umapi:" + PLATFORM_ARTIFACT_ID + ":" + umapiVersion
        );
    }

    private static void configureGeneratedResources(Project project, UMAPIModExtension mod) {
        var generatedResourcesDirectory = project.getLayout()
                .getBuildDirectory()
                .dir(GENERATED_RESOURCES_PATH);

        var generateResources = project.getTasks().register(
                GENERATE_RESOURCES_TASK,
                GenerateNeoForgeModsTomlTask.class,
                task -> {
                    task.getOutputDirectory().set(generatedResourcesDirectory);
                    task.getModId().set(project.provider(mod::getId));
                    task.getModVersion().set(project.provider(() -> project.getVersion().toString()));
                    task.getModName().set(project.provider(mod::getName));
                    task.getModDescription().set(project.provider(mod::getDescription));
                    task.getModAuthors().set(project.provider(() -> List.copyOf(mod.getAuthors())));
                    task.getMinecraftVersionRange().set(MINECRAFT_VERSION_RANGE);
                    task.getForgeVersionRange().set(FORGE_VERSION_RANGE);
                    task.getLoaderVersionRange().set(LOADER_VERSION_RANGE);
                    task.getUMAPIVersionRange().set(UMAPI_VERSION_RANGE);
                    task.getPackFormat().set(RESOURCE_PACK_FORMAT);
                    task.getPackDescription().set(project.provider(() -> mod.getName() + " resources"));
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

    private static void configureGeneratedEntrypoint(Project project, UMAPIModExtension mod) {
        var generatedSourcesDirectory = project.getLayout()
                .getBuildDirectory()
                .dir(GENERATED_SOURCES_PATH);

        var generateEntrypoint = project.getTasks().register(
                GENERATE_ENTRYPOINT_TASK,
                GenerateNeoForgeEntrypointTask.class,
                task -> {
                    task.getOutputDirectory().set(generatedSourcesDirectory);
                    task.getModId().set(project.provider(mod::getId));
                    task.getModEntrypoint().set(project.provider(mod::getEntrypoint));
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

    private static void configureRuntime(Project project) {
        project.getExtensions().configure(
                SourceSetContainer.class,
                sourceSets -> {
                    var main = sourceSets.getByName("main");
                    var runs = project.getExtensions().getByType(RunManager.class);

                    var client = runs.maybeCreate(NEOFORGE_1201_CLIENT_RUN);
                    client.runType("client");
                    client.modSource(main);

                    var server = runs.maybeCreate(NEOFORGE_1201_SERVER_RUN);
                    server.runType("server");
                    server.modSource(main);
                }
        );

        registerRuntimeTask(
                project,
                RUN_NEOFORGE_1201_CLIENT_TASK,
                NATIVE_NEOFORGE_1201_CLIENT_TASK,
                "Runs the NeoForge 1.20.1 UMAPI client runtime."
        );

        registerRuntimeTask(
                project,
                RUN_NEOFORGE_1201_SERVER_TASK,
                NATIVE_NEOFORGE_1201_SERVER_TASK,
                "Runs the NeoForge 1.20.1 UMAPI server runtime."
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

    private static void configureExport(
            Project project,
            UMAPIModExtension mod,
            String minecraftVersion
    ) {
        var jar = project.getTasks().named("jar");

        var exportTarget = project.getTasks().register(
                EXPORT_NEOFORGE_1201_TASK,
                Copy.class,
                task -> {
                    task.setGroup("umapi");
                    task.setDescription(
                            "Exports the NeoForge 1.20.1 mod jar to the UMAPI exports directory."
                    );
                    task.dependsOn(jar);
                    task.from(jar);
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
                + "-neoforge-mc"
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
