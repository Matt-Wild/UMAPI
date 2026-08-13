package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

final class UMAPIRuntimeTasks {
    enum Side {
        CLIENT("Client", "client"),
        SERVER("Server", "server");

        private final String taskSuffix;
        private final String descriptionName;

        Side(String taskSuffix, String descriptionName) {
            this.taskSuffix = taskSuffix;
            this.descriptionName = descriptionName;
        }
    }

    private UMAPIRuntimeTasks() {
    }

    static String clientTaskName(String loader, String minecraftVersion) {
        return taskName(loader, minecraftVersion, Side.CLIENT);
    }

    static String serverTaskName(String loader, String minecraftVersion) {
        return taskName(loader, minecraftVersion, Side.SERVER);
    }

    static String targetTaskName(String loader, String minecraftVersion) {
        return loaderTaskName(loader) + minecraftVersionTaskName(minecraftVersion);
    }

    static TaskProvider<Task> registerWrapper(
            Project project,
            String taskName,
            String nativeTaskName,
            String loader,
            String minecraftVersion,
            Side side
    ) {
        return project.getTasks().register(taskName, task -> {
            task.setGroup("umapi");
            task.setDescription(description(loader, minecraftVersion, side));
            task.dependsOn(nativeTaskName);
        });
    }

    static String description(String loader, String minecraftVersion, Side side) {
        return "Runs the "
                + loaderDisplayName(loader)
                + " "
                + minecraftVersion
                + " UMAPI "
                + side.descriptionName
                + " runtime.";
    }

    private static String taskName(String loader, String minecraftVersion, Side side) {
        return "runUMAPI" + targetTaskName(loader, minecraftVersion) + side.taskSuffix;
    }

    private static String loaderTaskName(String loader) {
        String knownTaskNamePart = UMAPILoader.taskNamePart(loader);

        return knownTaskNamePart != null ? knownTaskNamePart : toTaskNamePart(loader);
    }

    private static String loaderDisplayName(String loader) {
        return UMAPILoader.displayName(loader);
    }

    private static String minecraftVersionTaskName(String minecraftVersion) {
        return toTaskNamePart(minecraftVersion);
    }

    private static String toTaskNamePart(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        String sanitized = value.replaceAll("[^A-Za-z0-9]+", "");

        if (sanitized.isBlank()) {
            return "Unknown";
        }

        return Character.toUpperCase(sanitized.charAt(0)) + sanitized.substring(1);
    }
}
