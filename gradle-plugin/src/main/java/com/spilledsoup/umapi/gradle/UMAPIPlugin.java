package com.spilledsoup.umapi.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

import java.io.IOException;
import java.util.Properties;

public final class UMAPIPlugin implements Plugin<Project> {
    private static final String RUN_CLIENT_TASK = "runUMAPIClient";
    private static final String RUN_SERVER_TASK = "runUMAPIServer";

    @Override
    public void apply(Project project) {
        project.getLogger().lifecycle(
                "UMAPI Gradle plugin applied to " + project.getName()
        );

        project.getPluginManager().apply("java");

        project.getRepositories().mavenCentral();

        String umapiVersion = getUMAPIVersion();

        UMAPIExtension extension = project.getExtensions()
                .create("umapi", UMAPIExtension.class, project, umapiVersion);

        var runClient = registerRuntimeShortcut(
                project,
                RUN_CLIENT_TASK,
                "Runs the default UMAPI client runtime."
        );

        var runServer = registerRuntimeShortcut(
                project,
                RUN_SERVER_TASK,
                "Runs the default UMAPI server runtime."
        );

        project.getPluginManager().withPlugin("java", ignored -> {
            project.getExtensions().configure(
                    JavaPluginExtension.class,
                    java -> java.getToolchain()
                            .getLanguageVersion()
                            .set(JavaLanguageVersion.of(17))
            );
        });

        String apiDependency = "com.spilledsoup.umapi:api:" + umapiVersion;

        project.getDependencies().add("compileOnly", apiDependency);
        project.getDependencies().add("testImplementation", apiDependency);

        configureTesting(project);

        project.afterEvaluate(ignored -> {
            var targets = extension.getTargets().getRuntimeTargets();

            if (targets.isEmpty()) {
                throw new IllegalStateException(
                        "UMAPI requires at least one target, such as "
                                + UMAPISupportedTargets.declarations()
                                + "."
                );
            }

            extension.getMod().validate();

            configureRuntimeShortcuts(
                    extension.getRuntime(),
                    extension.getTargets().getShortcutRuntimeTargets(),
                    runClient,
                    runServer
            );
        });
    }

    private static TaskProvider<Task> registerRuntimeShortcut(
            Project project,
            String taskName,
            String description
    ) {
        return project.getTasks().register(taskName, task -> {
            task.setGroup("umapi");
            task.setDescription(description);
        });
    }

    private static void configureRuntimeShortcuts(
            UMAPIRuntimeExtension runtime,
            java.util.List<UMAPIRuntimeTarget> targets,
            TaskProvider<Task> runClient,
            TaskProvider<Task> runServer
    ) {
        var defaultRuntime = runtime.selectDefault(targets);

        runClient.configure(task -> {
            task.setDescription(
                    "Runs the default UMAPI client runtime: "
                            + defaultRuntime.id()
                            + "."
            );
            task.dependsOn(defaultRuntime.clientTaskName());
        });

        runServer.configure(task -> {
            task.setDescription(
                    "Runs the default UMAPI server runtime: "
                            + defaultRuntime.id()
                            + "."
            );
            task.dependsOn(defaultRuntime.serverTaskName());
        });
    }

    private static void configureTesting(Project project) {
        var dependencies = project.getDependencies();

        dependencies.add(
                "testImplementation",
                dependencies.platform("org.junit:junit-bom:6.0.0")
        );

        dependencies.add(
                "testImplementation",
                "org.junit.jupiter:junit-jupiter"
        );

        dependencies.add(
                "testRuntimeOnly",
                "org.junit.platform:junit-platform-launcher"
        );

        project.getTasks()
                .withType(Test.class)
                .configureEach(Test::useJUnitPlatform);
    }

    private static String getUMAPIVersion() {
        try (var stream = UMAPIPlugin.class
                .getClassLoader()
                .getResourceAsStream("umapi.properties")) {

            if (stream == null) {
                throw new IllegalStateException("Could not determine UMAPI version.");
            }

            var properties = new Properties();
            properties.load(stream);

            String version = properties.getProperty("version");
            if (version == null || version.isBlank()) {
                throw new IllegalStateException("Could not determine UMAPI version.");
            }

            return version;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not determine UMAPI version.",
                    exception
            );
        }
    }
}
