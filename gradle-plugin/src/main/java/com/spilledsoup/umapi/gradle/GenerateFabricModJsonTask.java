package com.spilledsoup.umapi.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GenerateFabricModJsonTask extends DefaultTask {
    @Input
    public abstract Property<String> getModId();

    @Input
    public abstract Property<String> getModVersion();

    @Input
    public abstract Property<String> getModName();

    @Input
    public abstract Property<String> getModDescription();

    @Input
    public abstract ListProperty<String> getModAuthors();

    @Input
    public abstract Property<String> getModEntrypoint();

    @Input
    public abstract Property<String> getMinecraftVersion();

    @Input
    public abstract Property<String> getFabricLoaderDependency();

    @Input
    public abstract Property<String> getJavaDependency();

    @Input
    public abstract Property<String> getUMAPIDependency();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void generate() {
        try {
            var outputDirectory = getOutputDirectory().get().getAsFile().toPath();
            Files.createDirectories(outputDirectory);
            Files.writeString(
                    outputDirectory.resolve("fabric.mod.json"),
                    render(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not generate fabric.mod.json.",
                    exception
            );
        }
    }

    private String render() {
        return """
                {
                  "schemaVersion": 1,
                  "id": %s,
                  "version": %s,
                  "name": %s,
                  "description": %s,
                  "authors": %s,
                  "environment": "*",
                  "entrypoints": {
                    "umapi": [
                      %s
                    ]
                  },
                  "depends": {
                    "fabricloader": %s,
                    "minecraft": %s,
                    "java": %s,
                    "umapi": %s
                  }
                }
                """.formatted(
                quote(getModId().get()),
                quote(getModVersion().get()),
                quote(getModName().get()),
                quote(getModDescription().get()),
                renderJsonArray(getModAuthors().get()),
                quote(getModEntrypoint().get()),
                quote(getFabricLoaderDependency().get()),
                quote(getMinecraftVersion().get()),
                quote(getJavaDependency().get()),
                quote(getUMAPIDependency().get())
        );
    }

    private static String renderJsonArray(List<String> values) {
        return values.stream()
                .map(GenerateFabricModJsonTask::quote)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String quote(String value) {
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "\"" + escaped + "\"";
    }
}
