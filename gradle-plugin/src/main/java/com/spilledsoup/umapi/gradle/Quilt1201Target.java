package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;

import java.net.URI;
import java.util.List;

final class Quilt1201Target {
    private static final String MINECRAFT_VERSION = "1.20.1";
    private static final String PLATFORM_ARTIFACT_ID = "quilt-1.20.1";
    private static final String QUILT_LOADER_VERSION = "0.29.2";
    private static final String QUILTED_FABRIC_API_VERSION = "7.7.0+0.92.2-1.20.1";
    private static final String QUILT_LOOM_PLUGIN = "org.quiltmc.loom";
    private static final UMAPILoader LOADER = UMAPILoader.QUILT;
    private static final String LOADER_ID = LOADER.id();
    private static final UMAPITargetDescriptor TARGET =
            new UMAPITargetDescriptor(LOADER_ID, MINECRAFT_VERSION);
    private static final String QUILT_LOADER_DEPENDENCY = ">=0.26.0";
    private static final String JAVA_DEPENDENCY = ">=17";
    private static final String UMAPI_DEPENDENCY = "*";
    private static final String GENERATED_RESOURCES_PATH = "generated/resources/umapi-quilt";
    private static final String GENERATE_RESOURCES_TASK = "generateUMAPIQuiltResources";
    private static final String QUILT_1201_CLIENT_WORKING_DIRECTORY = "quilt1201Client";
    private static final String QUILT_1201_SERVER_WORKING_DIRECTORY = "quilt1201Server";

    private Quilt1201Target() {
    }

    static boolean supports(String minecraftVersion) {
        return MINECRAFT_VERSION.equals(minecraftVersion);
    }

    static String declaration() {
        return "quilt(\"" + MINECRAFT_VERSION + "\")";
    }

    static UMAPIRuntimeTarget configure(
            Project project,
            String umapiVersion,
            UMAPIModExtension mod,
            String minecraftVersion
    ) {
        var target = descriptor(minecraftVersion);

        configureDependencies(project, umapiVersion, minecraftVersion, target);
        configureGeneratedResources(project, mod, minecraftVersion);
        configureExport(project, mod, target);
        configureRuntime(project, target);

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

    private static void configureDependencies(
            Project project,
            String umapiVersion,
            String minecraftVersion,
            UMAPITargetDescriptor target
    ) {
        UMAPILoomTargetSupport.configureMinecraft(
                project,
                QUILT_LOOM_PLUGIN,
                minecraftVersion,
                LOADER.displayName(),
                UMAPILoomTargetSupport.runtimeTasks(target)
        );

        project.getRepositories().maven(repository -> {
            repository.setName("Quilt");
            repository.setUrl(URI.create("https://maven.quiltmc.org/repository/release/"));
        });
        project.getRepositories().maven(repository -> {
            repository.setName("Fabric");
            repository.setUrl(URI.create("https://maven.fabricmc.net/"));
        });

        UMAPILoomTargetSupport.addPlatformDependency(
                project,
                PLATFORM_ARTIFACT_ID,
                umapiVersion
        );

        project.getDependencies().add(
                "modImplementation",
                "org.quiltmc:quilt-loader:" + QUILT_LOADER_VERSION
        );

        project.getDependencies().add(
                "modImplementation",
                "org.quiltmc.quilted-fabric-api:quilted-fabric-api:" + QUILTED_FABRIC_API_VERSION
        );
    }

    private static void configureGeneratedResources(
            Project project,
            UMAPIModExtension mod,
            String minecraftVersion
    ) {
        var generatedResourcesDirectory = UMAPIGeneratedResources.directory(
                project,
                GENERATED_RESOURCES_PATH
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
                    task.getMinecraftVersion().set(minecraftVersion);
                    task.getQuiltLoaderDependency().set(QUILT_LOADER_DEPENDENCY);
                    task.getJavaDependency().set(JAVA_DEPENDENCY);
                    task.getUMAPIDependency().set(UMAPI_DEPENDENCY);
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
                "Exports the Quilt 1.20.1 mod jar to the UMAPI exports directory."
        );
    }

    private static void configureRuntime(Project project, UMAPITargetDescriptor target) {
        UMAPILoomTargetSupport.configureRuntime(
                project,
                target,
                QUILT_1201_CLIENT_WORKING_DIRECTORY,
                QUILT_1201_SERVER_WORKING_DIRECTORY
        );
    }
}
