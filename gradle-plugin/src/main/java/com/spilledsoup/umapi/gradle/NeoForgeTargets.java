package com.spilledsoup.umapi.gradle;

import net.neoforged.gradle.dsl.common.runs.run.RunManager;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

import java.net.URI;

final class NeoForgeTargets {
    private static final UMAPILoader LOADER = UMAPILoader.NEOFORGE;
    private static final String NEOFORGE_GRADLE_PLUGIN = "net.neoforged.gradle.userdev";
    private static final String NEOFORGE_MODDEV_GRADLE_PLUGIN = "net.neoforged.moddev";
    private static final String GENERATED_RESOURCES_BASE_PATH = "generated/resources/umapi-neoforge";
    private static final String GENERATED_SOURCES_BASE_PATH = "generated/sources/umapi-neoforge";
    private static final String GENERATE_RESOURCES_TASK = "generateUMAPINeoForgeResources";
    private static final String GENERATE_ENTRYPOINT_TASK = "generateUMAPINeoForgeEntrypoint";
    private static final String GENERATED_ENTRYPOINT_CLASS = "UMAPINeoForgeEntrypoint";

    private NeoForgeTargets() {
    }

    static UMAPIRuntimeTarget configure(
            Project project,
            String umapiVersion,
            UMAPIModExtension mod,
            String minecraftVersion
    ) {
        var definition = UMAPITargetCatalog.neoForge(minecraftVersion);
        var target = definition.target();
        var descriptor = target.descriptor();

        configureDependencies(project, umapiVersion, target, definition);
        configureGeneratedResources(project, mod, target, definition);
        configureGeneratedEntrypoint(project, mod, target, definition);
        configureRuntime(project, mod, target, definition, descriptor);
        configureExport(project, mod, descriptor);

        return descriptor.runtimeTarget();
    }

    private static void configureDependencies(
            Project project,
            String umapiVersion,
            UMAPITargetDefinition target,
            UMAPITargetCatalog.NeoForgeTarget definition
    ) {
        if (definition.buildPlugin() == UMAPITargetCatalog.NeoForgeBuildPlugin.MODDEV_GRADLE) {
            disableModDevGradleIdeIntegration(project);
            project.getPluginManager().apply(NEOFORGE_MODDEV_GRADLE_PLUGIN);
            configureModDevGradle(project, definition);
        } else {
            project.getPluginManager().apply(NEOFORGE_GRADLE_PLUGIN);
            project.getDependencies().add(
                    "implementation",
                    definition.neoForgeDependency()
            );
        }

        configureJava(project, definition.javaLanguageVersion());

        project.getRepositories().maven(repository -> {
            repository.setName("NeoForged");
            repository.setUrl(URI.create("https://maven.neoforged.net/releases/"));
        });

        project.getDependencies().add(
                "implementation",
                "com.spilledsoup.umapi:" + target.platformArtifactId() + ":" + umapiVersion
        );
    }

    private static void configureGeneratedResources(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetDefinition target,
            UMAPITargetCatalog.NeoForgeTarget definition
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

        UMAPIGeneratedResources.wireMainResources(
                project,
                generatedResourcesDirectory,
                java.util.List.of(generateResources, generateContentResources)
        );

        UMAPIGeneratedResources.cleanStaleCompiledLoaderMetadataBeforeJar(project);
    }

    private static void configureGeneratedEntrypoint(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetDefinition target,
            UMAPITargetCatalog.NeoForgeTarget definition
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
            UMAPITargetDefinition target,
            UMAPITargetCatalog.NeoForgeTarget definition,
            UMAPITargetDescriptor descriptor
    ) {
        if (definition.buildPlugin() == UMAPITargetCatalog.NeoForgeBuildPlugin.MODDEV_GRADLE) {
            configureModDevGradleRuntime(project, mod, target);
        } else {
            configureNeoGradleRuntime(project, target);
        }

        UMAPIRuntimeTasks.registerWrapper(
                project,
                descriptor.clientTaskName(),
                nativeRunTaskName(target.clientWorkingDirectory()),
                descriptor.loader(),
                descriptor.minecraftVersion(),
                UMAPIRuntimeTasks.Side.CLIENT
        );

        UMAPIRuntimeTasks.registerWrapper(
                project,
                descriptor.serverTaskName(),
                nativeRunTaskName(target.serverWorkingDirectory()),
                descriptor.loader(),
                descriptor.minecraftVersion(),
                UMAPIRuntimeTasks.Side.SERVER
        );
    }

    private static void configureNeoGradleRuntime(
            Project project,
            UMAPITargetDefinition target
    ) {
        project.getExtensions().configure(
                SourceSetContainer.class,
                sourceSets -> {
                    var main = sourceSets.getByName("main");
                    var runs = project.getExtensions().getByType(RunManager.class);

                    var client = runs.maybeCreate(target.clientWorkingDirectory());
                    client.runType("client");
                    client.modSource(main);

                    var server = runs.maybeCreate(target.serverWorkingDirectory());
                    server.runType("server");
                    server.modSource(main);
                }
        );
    }

    private static void configureModDevGradleRuntime(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetDefinition target
    ) {
        project.getExtensions().configure(
                SourceSetContainer.class,
                sourceSets -> {
                    var main = sourceSets.getByName("main");

                    project.getExtensions().configure(
                            NeoForgeExtension.class,
                            neoForge -> {
                                neoForge.getMods()
                                        .maybeCreate(mod.getId())
                                        .sourceSet(main);

                                var client = neoForge.getRuns()
                                        .maybeCreate(target.clientWorkingDirectory());
                                client.client();
                                client.getSourceSet().set(main);
                                client.getGameDirectory().set(project.getLayout()
                                        .getProjectDirectory()
                                        .dir("runs/" + target.clientWorkingDirectory()));

                                var server = neoForge.getRuns()
                                        .maybeCreate(target.serverWorkingDirectory());
                                server.server();
                                server.getSourceSet().set(main);
                                server.getGameDirectory().set(project.getLayout()
                                        .getProjectDirectory()
                                        .dir("runs/" + target.serverWorkingDirectory()));
                            }
                    );
                }
        );
    }

    private static void configureModDevGradle(
            Project project,
            UMAPITargetCatalog.NeoForgeTarget definition
    ) {
        project.getExtensions().configure(
                NeoForgeExtension.class,
                neoForge -> neoForge.enable(settings -> {
                    settings.setVersion(definition.neoForgeVersion());
                    settings.setDisableRecompilation(true);
                })
        );
    }

    private static void disableModDevGradleIdeIntegration(Project project) {
        if (project.getExtensions().findByName("mdgInternalIdeIntegration") != null) {
            return;
        }

        try {
            ClassLoader classLoader = NeoForgeExtension.class.getClassLoader();
            Class<?> brandingClass = classLoader.loadClass(
                    "net.neoforged.moddevgradle.internal.Branding"
            );
            Object branding = brandingClass.getField("MDG").get(null);
            Class<?> noIdeIntegrationClass = classLoader.loadClass(
                    "net.neoforged.moddevgradle.internal.NoIdeIntegration"
            );
            var constructor = noIdeIntegrationClass.getDeclaredConstructor(
                    Project.class,
                    brandingClass
            );
            constructor.setAccessible(true);

            project.getExtensions().add(
                    "mdgInternalIdeIntegration",
                    constructor.newInstance(project, branding)
            );
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                    "Could not disable ModDevGradle IDE integration.",
                    exception
            );
        }
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
                "Exports the NeoForge " + target.minecraftVersion() + " mod jar to the UMAPI exports directory."
        );
    }

    private static String nativeRunTaskName(String runName) {
        return "run" + UMAPIGradleNames.taskNamePart(runName);
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
