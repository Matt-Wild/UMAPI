package com.spilledsoup.umapi.gradle;

import net.minecraftforge.gradle.MavenizerInstance;
import net.minecraftforge.gradle.MinecraftExtensionForProject;
import net.minecraftforge.gradle.SlimeLauncherOptions;
import net.minecraftforge.gradle.shadow.net.minecraftforge.gradleutils.shared.ToolsExtension;
import net.minecraftforge.renamer.gradle.RenamerExtension;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainService;

import java.net.URI;

final class ForgeTargets {
    private static final String FORGE_GRADLE_PLUGIN = "net.minecraftforge.gradle";
    private static final String FORGE_RENAMER_PLUGIN = "net.minecraftforge.renamer";
    private static final String SLIME_LAUNCHER_TOOL = "slimelauncher";
    private static final UMAPILoader LOADER = UMAPILoader.FORGE;
    private static final String GENERATED_RESOURCES_BASE_PATH = "generated/resources/umapi-forge";
    private static final String GENERATED_SOURCES_BASE_PATH = "generated/sources/umapi-forge";
    private static final String GENERATE_RESOURCES_TASK = "generateUMAPIForgeResources";
    private static final String CLEAN_STALE_MAIN_RESOURCES_TASK = "cleanUMAPIForgeStaleMainResources";
    private static final String CLEAN_STALE_CLASS_RESOURCES_TASK = "cleanUMAPIForgeStaleClassResources";
    private static final String COPY_RESOURCES_TO_MAIN_RESOURCES_TASK = "copyUMAPIForgeResourcesToMainResources";
    private static final String GENERATE_ENTRYPOINT_TASK = "generateUMAPIForgeEntrypoint";
    private static final String GENERATED_ENTRYPOINT_CLASS = "UMAPIForgeEntrypoint";
    private static final String FORGE_CLIENT_RUN = "client";
    private static final String FORGE_SERVER_RUN = "server";
    private static final String NATIVE_FORGE_CLIENT_TASK = "runClient";
    private static final String NATIVE_FORGE_SERVER_TASK = "runServer";
    private static final String REOBF_JAR_TASK = "reobfJar";
    private static final String BOOTSTRAP_LAUNCHER_MAIN_OPEN = "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED";

    private ForgeTargets() {
    }

    static UMAPIRuntimeTarget configure(
            Project project,
            String umapiVersion,
            UMAPIModExtension mod,
            String minecraftVersion
    ) {
        var definition = UMAPITargetCatalog.forge(minecraftVersion);
        var target = definition.target();
        var descriptor = target.descriptor();

        configureDependencies(project, umapiVersion, target, definition, descriptor);
        configureGeneratedResources(project, mod, target, definition);
        configureGeneratedEntrypoint(project, mod, target, definition);
        configureRuntime(project, mod, target, descriptor);
        configureExport(project, mod, descriptor);

        return descriptor.runtimeTarget();
    }

    private static void configureDependencies(
            Project project,
            String umapiVersion,
            UMAPITargetDefinition target,
            UMAPITargetCatalog.ForgeTarget definition,
            UMAPITargetDescriptor descriptor
    ) {
        project.getPluginManager().apply(FORGE_GRADLE_PLUGIN);
        project.getPluginManager().apply(FORGE_RENAMER_PLUGIN);
        configureJava(project, definition.javaLanguageVersion());
        configureSlimeLauncher(project, definition.javaLanguageVersion());

        project.getRepositories().maven(repository -> {
            repository.setName("MinecraftForge");
            repository.setUrl(URI.create("https://maven.minecraftforge.net/"));
        });
        project.getRepositories().maven(repository -> {
            repository.setName("MinecraftLibraries");
            repository.setUrl(URI.create("https://libraries.minecraft.net/"));
        });

        var minecraft = project.getExtensions().getByType(MinecraftExtensionForProject.class);
        minecraft.mappings("official", descriptor.minecraftVersion());
        var forge = minecraft.dependency(definition.forgeDependencyNotation());
        minecraft.mavenizer(project.getRepositories());

        project.getDependencies().addProvider("implementation", forge.asProvider());
        configureRenamer(project, forge, definition.javaLanguageVersion());

        project.getDependencies().add(
                "implementation",
                "com.spilledsoup.umapi:" + target.platformArtifactId() + ":" + umapiVersion
        );
    }

    private static void configureSlimeLauncher(Project project, int javaLanguageVersion) {
        var toolchains = project.getExtensions().getByType(JavaToolchainService.class);
        var javaLauncher = toolchains.launcherFor(spec -> spec.getLanguageVersion()
                .set(JavaLanguageVersion.of(javaLanguageVersion)));

        project.getExtensions()
                .getByType(ToolsExtension.class)
                .configure(SLIME_LAUNCHER_TOOL, tool -> tool.getJavaLauncher().set(javaLauncher));
    }

    private static void configureRenamer(
            Project project,
            MavenizerInstance forge,
            int javaLanguageVersion
    ) {
        var renamer = project.getExtensions().getByType(RenamerExtension.class);
        var jar = project.getTasks().named("jar", Jar.class);

        renamer.classes(
                REOBF_JAR_TASK,
                jar,
                task -> {
                    task.getJavaLauncher().set(project.getExtensions()
                            .getByType(JavaToolchainService.class)
                            .launcherFor(spec -> spec.getLanguageVersion()
                                    .set(JavaLanguageVersion.of(javaLanguageVersion))));
                    task.mappings(forge.getToSrg());
                }
        );
    }

    private static void configureGeneratedResources(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetDefinition target,
            UMAPITargetCatalog.ForgeTarget definition
    ) {
        var generatedResourcesDirectory = UMAPIGeneratedResources.directory(
                project,
                generatedResourcesPath(target)
        );

        var generateResources = UMAPIForgeFamilyTargetSupport.registerGeneratedResources(
                project,
                mod,
                definition,
                generatedResourcesDirectory,
                GENERATE_RESOURCES_TASK
        );

        var generateContentResources = UMAPIContentResources.register(
                project,
                mod,
                target.minecraftVersion(),
                generatedResourcesDirectory
        );

        wireGeneratedResourcesToOutputs(
                project,
                generatedResourcesDirectory,
                java.util.List.of(generateResources, generateContentResources)
        );
    }

    private static void wireGeneratedResourcesToOutputs(
            Project project,
            org.gradle.api.provider.Provider<org.gradle.api.file.Directory> generatedResourcesDirectory,
            java.util.List<org.gradle.api.tasks.TaskProvider<?>> generateResources
    ) {
        var compileJava = project.getTasks().named("compileJava", JavaCompile.class);
        var processResources = project.getTasks().named("processResources");
        var mainResourcesDirectory = project.getLayout()
                .getBuildDirectory()
                .dir("resources/main");

        var cleanStaleMainResources = UMAPIGeneratedResources.registerLoaderMetadataCleanup(
                project,
                CLEAN_STALE_MAIN_RESOURCES_TASK,
                mainResourcesDirectory
        );

        processResources.configure(task -> task.dependsOn(cleanStaleMainResources));

        var copyResourcesToMainResources = project.getTasks().register(
                COPY_RESOURCES_TO_MAIN_RESOURCES_TASK,
                Copy.class,
                task -> {
                    generateResources.forEach(task::dependsOn);
                    task.dependsOn(processResources);
                    task.from(generatedResourcesDirectory);
                    task.into(mainResourcesDirectory);
                }
        );

        var cleanStaleClassResources = UMAPIGeneratedResources.registerLoaderMetadataCleanup(
                project,
                CLEAN_STALE_CLASS_RESOURCES_TASK,
                compileJava.flatMap(JavaCompile::getDestinationDirectory)
        );
        cleanStaleClassResources.configure(task -> task.dependsOn(compileJava));

        project.getTasks()
                .named("classes")
                .configure(task -> {
                    task.dependsOn(copyResourcesToMainResources);
                    task.dependsOn(cleanStaleClassResources);
                });
    }

    private static void configureGeneratedEntrypoint(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetDefinition target,
            UMAPITargetCatalog.ForgeTarget definition
    ) {
        var generatedSourcesDirectory = project.getLayout()
                .getBuildDirectory()
                .dir(generatedSourcesPath(target));

        UMAPIForgeFamilyTargetSupport.configureGeneratedEntrypoint(
                project,
                mod,
                definition,
                generatedSourcesDirectory,
                GENERATE_ENTRYPOINT_TASK,
                GENERATED_ENTRYPOINT_CLASS
        );
    }

    private static void configureRuntime(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetDefinition definition,
            UMAPITargetDescriptor descriptor
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
                            definition.clientWorkingDirectory()
                    );
                    configureForgeRun(
                            project,
                            minecraft.getRuns().maybeCreate(FORGE_SERVER_RUN),
                            main,
                            mod.getId(),
                            definition.serverWorkingDirectory()
                    );
                }
        );

        UMAPIRuntimeTasks.registerWrapper(
                project,
                descriptor.clientTaskName(),
                NATIVE_FORGE_CLIENT_TASK,
                descriptor.loader(),
                descriptor.minecraftVersion(),
                UMAPIRuntimeTasks.Side.CLIENT
        );

        UMAPIRuntimeTasks.registerWrapper(
                project,
                descriptor.serverTaskName(),
                NATIVE_FORGE_SERVER_TASK,
                descriptor.loader(),
                descriptor.minecraftVersion(),
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
        run.jvmArgs(BOOTSTRAP_LAUNCHER_MAIN_OPEN);
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
                "Exports the Forge " + target.minecraftVersion() + " mod jar to the UMAPI exports directory."
        );
    }

    private static String generatedResourcesPath(UMAPITargetDefinition target) {
        return UMAPIForgeFamilyTargetSupport.generatedPath(GENERATED_RESOURCES_BASE_PATH, target);
    }

    private static String generatedSourcesPath(UMAPITargetDefinition target) {
        return UMAPIForgeFamilyTargetSupport.generatedJavaPath(GENERATED_SOURCES_BASE_PATH, target);
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
