package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.language.jvm.tasks.ProcessResources;

import java.util.List;

final class UMAPIGeneratedResources {
    private static final String CLEAN_STALE_MAIN_RESOURCES_TASK = "cleanUMAPIStaleMainResources";
    private static final String CLEAN_STALE_CLASS_RESOURCES_TASK = "cleanUMAPIStaleClassResources";
    private static final List<String> LOADER_METADATA_PATHS = List.of(
            "fabric.mod.json",
            "quilt.mod.json",
            "pack.mcmeta",
            "mcmod.info",
            "META-INF/mods.toml",
            "META-INF/neoforge.mods.toml"
    );

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
        wireMainResources(project, directory, List.of(generatorTask));
    }

    static void wireMainResources(
            Project project,
            Provider<Directory> directory,
            List<TaskProvider<? extends Task>> generatorTasks
    ) {
        project.getExtensions().configure(
                SourceSetContainer.class,
                sourceSets -> sourceSets.named("main", main ->
                        main.getResources().srcDir(directory)
                )
        );

        var cleanStaleMainResources = registerLoaderMetadataCleanup(
                project,
                CLEAN_STALE_MAIN_RESOURCES_TASK,
                mainResourcesDirectory(project)
        );

        project.getTasks()
                .withType(ProcessResources.class)
                .configureEach(task -> {
                    task.dependsOn(cleanStaleMainResources);
                    generatorTasks.forEach(task::dependsOn);
                });
    }

    static void cleanStaleCompiledLoaderMetadataBeforeJar(Project project) {
        var compileJava = project.getTasks().named("compileJava", JavaCompile.class);
        var cleanStaleClassResources = registerLoaderMetadataCleanup(
                project,
                CLEAN_STALE_CLASS_RESOURCES_TASK,
                compileJava.flatMap(JavaCompile::getDestinationDirectory)
        );

        cleanStaleClassResources.configure(task -> task.dependsOn(compileJava));

        project.getTasks()
                .named("jar", Jar.class)
                .configure(task -> task.dependsOn(cleanStaleClassResources));
    }

    static TaskProvider<Delete> registerLoaderMetadataCleanup(
            Project project,
            String taskName,
            Provider<Directory> directory
    ) {
        return project.getTasks().register(
                taskName,
                Delete.class,
                task -> LOADER_METADATA_PATHS.forEach(path ->
                        task.delete(directory.map(root -> root.file(path)))
                )
        );
    }

    private static Provider<Directory> mainResourcesDirectory(Project project) {
        return project.getLayout()
                .getBuildDirectory()
                .dir("resources/main");
    }
}
