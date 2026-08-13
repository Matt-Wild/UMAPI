package com.spilledsoup.umapi.gradle;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.tasks.JavaExec;

import java.lang.reflect.InvocationTargetException;

final class UMAPILoomRunDirectories {
    private static final String CLIENT_RUN_CONFIG = "client";
    private static final String SERVER_RUN_CONFIG = "server";
    private static final String RUN_CLIENT_TASK = "runClient";
    private static final String RUN_SERVER_TASK = "runServer";
    private static final String RUN_DIRECTORY_PREFIX = "runs/";

    private UMAPILoomRunDirectories() {
    }

    static void configure(
            Project project,
            String clientDirectoryName,
            String serverDirectoryName
    ) {
        configureRun(project, CLIENT_RUN_CONFIG, RUN_CLIENT_TASK, clientDirectoryName);
        configureRun(project, SERVER_RUN_CONFIG, RUN_SERVER_TASK, serverDirectoryName);
    }

    private static void configureRun(
            Project project,
            String runConfigName,
            String taskName,
            String directoryName
    ) {
        String relativeRunDirectory = RUN_DIRECTORY_PREFIX + directoryName;

        configureLoomRunConfig(project, runConfigName, relativeRunDirectory);
        configureJavaExecTask(project, taskName, directoryName);
    }

    private static void configureLoomRunConfig(
            Project project,
            String runConfigName,
            String relativeRunDirectory
    ) {
        Object loom = project.getExtensions().getByName("loom");
        Object runs = invoke(loom, "getRuns");

        if (!(runs instanceof NamedDomainObjectContainer<?>)) {
            throw new IllegalStateException("Could not configure Loom run directories.");
        }

        @SuppressWarnings("unchecked")
        var runConfigs = (NamedDomainObjectContainer<Object>) runs;

        runConfigs.named(runConfigName)
                .configure(runConfig -> invoke(
                        runConfig,
                        "setRunDir",
                        String.class,
                        relativeRunDirectory
                ));
    }

    private static void configureJavaExecTask(
            Project project,
            String taskName,
            String directoryName
    ) {
        var runDirectory = project.getLayout()
                .getProjectDirectory()
                .dir(RUN_DIRECTORY_PREFIX + directoryName)
                .getAsFile();

        project.getTasks()
                .withType(JavaExec.class)
                .matching(task -> task.getName().equals(taskName))
                .configureEach(task -> {
                    task.setWorkingDir(runDirectory);
                    task.doFirst(ignored -> {
                        task.setWorkingDir(runDirectory);
                        project.mkdir(runDirectory);
                    });
                });
    }

    private static Object invoke(Object target, String methodName) {
        try {
            return target.getClass()
                    .getMethod(methodName)
                    .invoke(target);
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw new IllegalStateException(
                    "Could not configure Loom run directories.",
                    exception
            );
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(
                    "Could not configure Loom run directories.",
                    exception.getCause()
            );
        }
    }

    private static void invoke(
            Object target,
            String methodName,
            Class<?> parameterType,
            Object value
    ) {
        try {
            target.getClass()
                    .getMethod(methodName, parameterType)
                    .invoke(target, value);
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw new IllegalStateException(
                    "Could not configure Loom run directories.",
                    exception
            );
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(
                    "Could not configure Loom run directories.",
                    exception.getCause()
            );
        }
    }
}
