package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;

final class UMAPIContentResources {
    private static final String GENERATE_CONTENT_RESOURCES_TASK = "generateUMAPIContentResources";

    private UMAPIContentResources() {
    }

    static TaskProvider<GenerateUMAPIContentResourcesTask> register(
            Project project,
            UMAPIModExtension mod,
            String minecraftVersion,
            Provider<Directory> outputDirectory
    ) {
        var sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        var main = sourceSets.getByName("main");
        var compileJava = project.getTasks().named("compileJava", JavaCompile.class);

        return project.getTasks().register(
                GENERATE_CONTENT_RESOURCES_TASK,
                GenerateUMAPIContentResourcesTask.class,
                task -> {
                    task.dependsOn(compileJava);
                    task.getOutputDirectory().set(outputDirectory);
                    task.getSourceResourcesDirectory().set(project.getLayout()
                            .getProjectDirectory()
                            .dir("src/main/resources"));
                    task.getModClasspath().from(main.getOutput().getClassesDirs());
                    task.getModClasspath().from(main.getCompileClasspath());
                    task.getModId().set(project.provider(mod::getId));
                    task.getModEntrypoint().set(project.provider(mod::getEntrypoint));
                    task.getMinecraftVersion().set(minecraftVersion);
                }
        );
    }
}
