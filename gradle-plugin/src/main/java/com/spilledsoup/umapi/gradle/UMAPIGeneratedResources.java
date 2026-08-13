package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.jvm.tasks.ProcessResources;

final class UMAPIGeneratedResources {

    private UMAPIGeneratedResources() {
    }

    static Provider<Directory> directory(Project project, String path) {
        return project.getLayout()
                .getBuildDirectory()
                .dir(path);
    }

    static void wireMainResources(
            Project project,
            Provider<Directory> directory,
            TaskProvider<? extends Task> generatorTask
    ) {
        project.getExtensions().configure(
                SourceSetContainer.class,
                sourceSets -> sourceSets.named("main", main ->
                        main.getResources().srcDir(directory)
                )
        );

        project.getTasks()
                .withType(ProcessResources.class)
                .configureEach(task -> task.dependsOn(generatorTask));
    }
}
