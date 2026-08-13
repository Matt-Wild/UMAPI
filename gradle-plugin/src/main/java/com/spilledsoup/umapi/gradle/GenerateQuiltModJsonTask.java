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

public abstract class GenerateQuiltModJsonTask extends DefaultTask {
    @Input
    public abstract Property<String> getModGroup();

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
    public abstract Property<String> getQuiltLoaderDependency();

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
                    outputDirectory.resolve("quilt.mod.json"),
                    render(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not generate quilt.mod.json.",
                    exception
            );
        }
    }

    private String render() {
        return """
                {
                  "schema_version": 1,
                  "quilt_loader": {
                    "group": %s,
                    "id": %s,
                    "version": %s,
                    "metadata": {
                      "name": %s,
                      "description": %s,
                      "contributors": %s
                    },
                    "intermediate_mappings": "net.fabricmc:intermediary",
                    "entrypoints": {
                      "umapi": %s
                    },
                    "depends": [
                      {
                        "id": "quilt_loader",
                        "versions": %s
                      },
                      {
                        "id": "minecraft",
                        "versions": %s
                      },
                      {
                        "id": "java",
                        "versions": %s
                      },
                      {
                        "id": "umapi",
                        "versions": %s
                      }
                    ]
                  }
                }
                """.formatted(
                quote(getModGroup().get()),
                quote(getModId().get()),
                quote(getModVersion().get()),
                quote(getModName().get()),
                quote(getModDescription().get()),
                renderContributors(getModAuthors().get()),
                quote(getModEntrypoint().get()),
                quote(getQuiltLoaderDependency().get()),
                quote(getMinecraftVersion().get()),
                quote(getJavaDependency().get()),
                quote(getUMAPIDependency().get())
        );
    }

    private static String renderContributors(List<String> authors) {
        return authors.stream()
                .map(author -> quote(author) + ": \"Developer\"")
                .collect(Collectors.joining(", ", "{", "}"));
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
