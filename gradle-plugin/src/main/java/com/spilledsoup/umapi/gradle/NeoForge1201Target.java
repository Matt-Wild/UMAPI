package com.spilledsoup.umapi.gradle;

import net.neoforged.gradle.dsl.common.runs.run.RunManager;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;

import java.net.URI;
import java.util.List;

final class NeoForge1201Target {
    private static final String MINECRAFT_VERSION = "1.20.1";
    private static final String PLATFORM_ARTIFACT_ID = "neoforge-1.20.1";
    private static final String NEOFORGE_VERSION = "1.20.1-47.1.106";
    private static final UMAPILoader LOADER = UMAPILoader.NEOFORGE;
    private static final String LOADER_ID = LOADER.id();
    private static final UMAPITargetDescriptor TARGET =
            new UMAPITargetDescriptor(LOADER_ID, MINECRAFT_VERSION);
    private static final String MINECRAFT_VERSION_RANGE = "[1.20.1,1.21)";
    private static final String FORGE_VERSION_RANGE = "[47.1.106,)";
    private static final String LOADER_VERSION_RANGE = "[47,)";
    private static final String UMAPI_VERSION_RANGE = "[0,)";
    private static final int RESOURCE_PACK_FORMAT = 15;
    private static final String GENERATED_RESOURCES_PATH = "generated/resources/umapi-neoforge";
    private static final String GENERATED_SOURCES_PATH = "generated/sources/umapi-neoforge/java";
    private static final String GENERATE_RESOURCES_TASK = "generateUMAPINeoForgeResources";
    private static final String GENERATE_ENTRYPOINT_TASK = "generateUMAPINeoForgeEntrypoint";
    private static final String GENERATED_ENTRYPOINT_PACKAGE = "com.spilledsoup.umapi.generated.neoforge1201";
    private static final String GENERATED_ENTRYPOINT_CLASS = "UMAPINeoForgeEntrypoint";
    private static final String NEOFORGE_1201_CLIENT_RUN = "neoForge1201Client";
    private static final String NEOFORGE_1201_SERVER_RUN = "neoForge1201Server";
    private static final String NATIVE_NEOFORGE_1201_CLIENT_TASK = "runNeoForge1201Client";
    private static final String NATIVE_NEOFORGE_1201_SERVER_TASK = "runNeoForge1201Server";

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
        var target = descriptor(minecraftVersion);

        configureDependencies(project, umapiVersion);
        configureGeneratedResources(project, mod);
        configureGeneratedEntrypoint(project, mod);
        configureRuntime(project, target);
        configureExport(project, mod, target);

        return target.runtimeTarget();
    }

    static UMAPIRuntimeTarget runtimeTarget(String minecraftVersion) {
        return descriptor(minecraftVersion).runtimeTarget();
    }

    private static UMAPITargetDescriptor descriptor(String minecraftVersion) {
        if (MINECRAFT_VERSION.equals(minecraftVersion)) {
            return TARGET;
        }

        return new UMAPITargetDescriptor(LOADER_ID, minecraftVersion);
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
        var generatedResourcesDirectory = UMAPIGeneratedResources.directory(
                project,
                GENERATED_RESOURCES_PATH
        );

        var generateResources = project.getTasks().register(
                GENERATE_RESOURCES_TASK,
                GenerateForgeFamilyModsTomlTask.class,
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

        UMAPIGeneratedResources.wireMainResources(
                project,
                generatedResourcesDirectory,
                generateResources
        );
    }

    private static void configureGeneratedEntrypoint(Project project, UMAPIModExtension mod) {
        var generatedSourcesDirectory = project.getLayout()
                .getBuildDirectory()
                .dir(GENERATED_SOURCES_PATH);

        var generateEntrypoint = project.getTasks().register(
                GENERATE_ENTRYPOINT_TASK,
                GenerateForgeFamilyEntrypointTask.class,
                task -> {
                    task.getOutputDirectory().set(generatedSourcesDirectory);
                    task.getModId().set(project.provider(mod::getId));
                    task.getModEntrypoint().set(project.provider(mod::getEntrypoint));
                    task.getEntrypointPackage().set(GENERATED_ENTRYPOINT_PACKAGE);
                    task.getEntrypointClassName().set(GENERATED_ENTRYPOINT_CLASS);
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

    private static void configureRuntime(Project project, UMAPITargetDescriptor target) {
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

        UMAPIRuntimeTasks.registerWrapper(
                project,
                target.clientTaskName(),
                NATIVE_NEOFORGE_1201_CLIENT_TASK,
                target.loader(),
                target.minecraftVersion(),
                UMAPIRuntimeTasks.Side.CLIENT
        );

        UMAPIRuntimeTasks.registerWrapper(
                project,
                target.serverTaskName(),
                NATIVE_NEOFORGE_1201_SERVER_TASK,
                target.loader(),
                target.minecraftVersion(),
                UMAPIRuntimeTasks.Side.SERVER
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
                project.getTasks().named("jar"),
                "Exports the NeoForge 1.20.1 mod jar to the UMAPI exports directory."
        );
    }

}
