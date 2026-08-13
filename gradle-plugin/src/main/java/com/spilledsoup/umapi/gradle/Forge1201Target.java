package com.spilledsoup.umapi.gradle;

import net.minecraftforge.gradle.MinecraftExtensionForProject;
import net.minecraftforge.gradle.MavenizerInstance;
import net.minecraftforge.gradle.SlimeLauncherOptions;
import net.minecraftforge.gradle.shadow.net.minecraftforge.gradleutils.shared.ToolsExtension;
import net.minecraftforge.renamer.gradle.RenamerExtension;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainService;

import java.net.URI;
import java.util.List;

final class Forge1201Target {
    private static final String MINECRAFT_VERSION = "1.20.1";
    private static final String PLATFORM_ARTIFACT_ID = "forge-1.20.1";
    private static final String FORGE_VERSION = "1.20.1-47.4.10";
    private static final String FORGE_GRADLE_PLUGIN = "net.minecraftforge.gradle";
    private static final String FORGE_RENAMER_PLUGIN = "net.minecraftforge.renamer";
    private static final String SLIME_LAUNCHER_TOOL = "slimelauncher";
    private static final UMAPILoader LOADER = UMAPILoader.FORGE;
    private static final String LOADER_ID = LOADER.id();
    private static final UMAPITargetDescriptor TARGET =
            new UMAPITargetDescriptor(LOADER_ID, MINECRAFT_VERSION);
    private static final String MINECRAFT_VERSION_RANGE = "[1.20.1,1.21)";
    private static final String FORGE_VERSION_RANGE = "[47.4.10,)";
    private static final String LOADER_VERSION_RANGE = "[47,)";
    private static final String UMAPI_VERSION_RANGE = "[0,)";
    private static final int RESOURCE_PACK_FORMAT = 15;
    private static final String GENERATED_RESOURCES_PATH = "generated/resources/umapi-forge";
    private static final String GENERATED_SOURCES_PATH = "generated/sources/umapi-forge/java";
    private static final String GENERATE_RESOURCES_TASK = "generateUMAPIForgeResources";
    private static final String GENERATE_ENTRYPOINT_TASK = "generateUMAPIForgeEntrypoint";
    private static final String GENERATED_ENTRYPOINT_PACKAGE = "com.spilledsoup.umapi.generated.forge1201";
    private static final String GENERATED_ENTRYPOINT_CLASS = "UMAPIForgeEntrypoint";
    private static final String FORGE_CLIENT_RUN = "client";
    private static final String FORGE_SERVER_RUN = "server";
    private static final String NATIVE_FORGE_CLIENT_TASK = "runClient";
    private static final String NATIVE_FORGE_SERVER_TASK = "runServer";
    private static final String FORGE_1201_CLIENT_WORKING_DIRECTORY = "forge1201Client";
    private static final String FORGE_1201_SERVER_WORKING_DIRECTORY = "forge1201Server";
    private static final String REOBF_JAR_TASK = "reobfJar";

    private Forge1201Target() {
    }

    static boolean supports(String minecraftVersion) {
        return MINECRAFT_VERSION.equals(minecraftVersion);
    }

    static String declaration() {
        return "forge(\"" + MINECRAFT_VERSION + "\")";
    }

    static UMAPIRuntimeTarget configure(
            Project project,
            String umapiVersion,
            UMAPIModExtension mod,
            String minecraftVersion
    ) {
        var target = descriptor(minecraftVersion);

        configureDependencies(project, umapiVersion, target);
        configureGeneratedResources(project, mod);
        configureGeneratedEntrypoint(project, mod);
        configureRuntime(project, mod, target);
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

    private static void configureDependencies(
            Project project,
            String umapiVersion,
            UMAPITargetDescriptor target
    ) {
        project.getPluginManager().apply(FORGE_GRADLE_PLUGIN);
        project.getPluginManager().apply(FORGE_RENAMER_PLUGIN);
        configureSlimeLauncher(project);

        project.getRepositories().maven(repository -> {
            repository.setName("MinecraftForge");
            repository.setUrl(URI.create("https://maven.minecraftforge.net/"));
        });
        project.getRepositories().maven(repository -> {
            repository.setName("MinecraftLibraries");
            repository.setUrl(URI.create("https://libraries.minecraft.net/"));
        });

        var minecraft = project.getExtensions().getByType(MinecraftExtensionForProject.class);
        minecraft.mappings("official", target.minecraftVersion());
        var forge = minecraft.dependency("net.minecraftforge:forge:" + FORGE_VERSION);
        minecraft.mavenizer(project.getRepositories());

        project.getDependencies().addProvider("implementation", forge.asProvider());
        configureRenamer(project, forge);

        project.getDependencies().add(
                "implementation",
                "com.spilledsoup.umapi:" + PLATFORM_ARTIFACT_ID + ":" + umapiVersion
        );
    }

    private static void configureSlimeLauncher(Project project) {
        var toolchains = project.getExtensions().getByType(JavaToolchainService.class);
        var java17 = toolchains.launcherFor(spec -> spec.getLanguageVersion()
                .set(JavaLanguageVersion.of(17)));

        project.getExtensions()
                .getByType(ToolsExtension.class)
                .configure(SLIME_LAUNCHER_TOOL, tool -> tool.getJavaLauncher().set(java17));
    }

    private static void configureRenamer(Project project, MavenizerInstance forge) {
        var renamer = project.getExtensions().getByType(RenamerExtension.class);
        var jar = project.getTasks().named("jar", Jar.class);

        renamer.classes(
                REOBF_JAR_TASK,
                jar,
                task -> {
                    task.getJavaLauncher().set(project.getExtensions()
                            .getByType(JavaToolchainService.class)
                            .launcherFor(spec -> spec.getLanguageVersion()
                                    .set(JavaLanguageVersion.of(17))));
                    task.mappings(forge.getToSrg());
                }
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

    private static void configureRuntime(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetDescriptor target
    ) {
        project.getExtensions().configure(
                SourceSetContainer.class,
                sourceSets -> {
                    var main = sourceSets.getByName("main");
                    var minecraft = project.getExtensions().getByType(MinecraftExtensionForProject.class);

                    configureForgeRun(
                            project,
                            minecraft.getRuns().maybeCreate(FORGE_CLIENT_RUN),
                            main,
                            mod.getId(),
                            FORGE_1201_CLIENT_WORKING_DIRECTORY
                    );
                    configureForgeRun(
                            project,
                            minecraft.getRuns().maybeCreate(FORGE_SERVER_RUN),
                            main,
                            mod.getId(),
                            FORGE_1201_SERVER_WORKING_DIRECTORY
                    );
                }
        );

        UMAPIRuntimeTasks.registerWrapper(
                project,
                target.clientTaskName(),
                NATIVE_FORGE_CLIENT_TASK,
                target.loader(),
                target.minecraftVersion(),
                UMAPIRuntimeTasks.Side.CLIENT
        );

        UMAPIRuntimeTasks.registerWrapper(
                project,
                target.serverTaskName(),
                NATIVE_FORGE_SERVER_TASK,
                target.loader(),
                target.minecraftVersion(),
                UMAPIRuntimeTasks.Side.SERVER
        );
    }

    private static void configureForgeRun(
            Project project,
            SlimeLauncherOptions run,
            SourceSet main,
            String modId,
            String runName
    ) {
        run.getWorkingDir().set(
                project.getLayout()
                        .getProjectDirectory()
                        .dir("runs/" + runName)
        );
        run.getMods().maybeCreate(modId).source(main);
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
                project.getTasks().named(REOBF_JAR_TASK),
                "Exports the Forge 1.20.1 mod jar to the UMAPI exports directory."
        );
    }
}
