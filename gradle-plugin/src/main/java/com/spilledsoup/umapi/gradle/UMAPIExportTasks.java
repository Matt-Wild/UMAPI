package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskProvider;

final class UMAPIExportTasks {
    private static final String EXPORT_DIRECTORY = "umapi/exports";
    private static final String EXPORT_TASK = "exportUMAPI";

    private UMAPIExportTasks() {
    }

    static TaskProvider<Copy> registerJarExport(
            Project project,
            UMAPIModExtension mod,
            UMAPITargetDescriptor target,
            TaskProvider<?> jarTask,
            String description
    ) {
        var exportTarget = project.getTasks().register(
                target.exportTaskName(),
                Copy.class,
                task -> {
                    task.setGroup("umapi");
                    task.setDescription(description);
                    task.dependsOn(jarTask);
                    task.from(jarTask);
                    task.include("*.jar");
                    task.into(project.getLayout().getBuildDirectory().dir(EXPORT_DIRECTORY));
                    task.rename(ignored -> UMAPIExportName.jarFileName(
                            project,
                            mod,
                            target.loader(),
                            target.minecraftVersion()
                    ));
                }
        );

        var exportUMAPI = getOrCreateExportTask(project);
        exportUMAPI.configure(task -> task.dependsOn(exportTarget));

        project.getTasks()
                .named("assemble")
                .configure(task -> task.dependsOn(exportUMAPI));

        return exportTarget;
    }

    static TaskProvider<Task> getOrCreateExportTask(Project project) {
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
}
