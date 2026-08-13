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

public abstract class GenerateForgeFamilyModsTomlTask extends DefaultTask {
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
    public abstract Property<String> getMinecraftVersionRange();

    @Input
    public abstract Property<String> getForgeVersionRange();

    @Input
    public abstract Property<String> getLoaderDependencyModId();

    @Input
    public abstract Property<String> getLoaderVersionRange();

    @Input
    public abstract Property<Boolean> getUseModernDependencyType();

    @Input
    public abstract Property<String> getUMAPIVersionRange();

    @Input
    public abstract Property<Integer> getPackFormat();

    @Input
    public abstract Property<String> getPackDescription();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void generate() {
        try {
            var outputDirectory = getOutputDirectory().get()
                    .getAsFile()
                    .toPath();
            var metaInfDirectory = outputDirectory.resolve("META-INF");

            Files.createDirectories(metaInfDirectory);
            Files.writeString(
                    metaInfDirectory.resolve("mods.toml"),
                    renderModsToml(),
                    StandardCharsets.UTF_8
            );
            Files.writeString(
                    outputDirectory.resolve("pack.mcmeta"),
                    renderPackMcmeta(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not generate Forge-family mods.toml.",
                    exception
            );
        }
    }

    private String renderModsToml() {
        String modId = getModId().get();

        return """
                modLoader="javafml"
                loaderVersion=%s
                license="All Rights Reserved"

                [[mods]]
                modId=%s
                version=%s
                displayName=%s
                authors=%s
                description=%s

                [[dependencies.%s]]
                modId=%s
                %s
                versionRange=%s
                ordering="NONE"
                side="BOTH"

                [[dependencies.%s]]
                modId="minecraft"
                %s
                versionRange=%s
                ordering="NONE"
                side="BOTH"

                [[dependencies.%s]]
                modId="umapi"
                %s
                versionRange=%s
                ordering="AFTER"
                side="BOTH"
                """.formatted(
                quote(getLoaderVersionRange().get()),
                quote(modId),
                quote(getModVersion().get()),
                quote(getModName().get()),
                quote(renderAuthors(getModAuthors().get())),
                quote(getModDescription().get()),
                modId,
                quote(getLoaderDependencyModId().get()),
                dependencyRequirement(),
                quote(getForgeVersionRange().get()),
                modId,
                dependencyRequirement(),
                quote(getMinecraftVersionRange().get()),
                modId,
                dependencyRequirement(),
                quote(getUMAPIVersionRange().get())
        );
    }

    private String renderPackMcmeta() {
        return """
                {
                  "pack": {
                    "pack_format": %d,
                    "description": %s
                  }
                }
                """.formatted(
                getPackFormat().get(),
                quote(getPackDescription().get())
        );
    }

    private static String renderAuthors(List<String> authors) {
        return authors.stream()
                .collect(Collectors.joining(", "));
    }

    private String dependencyRequirement() {
        if (getUseModernDependencyType().get()) {
            return "type=\"required\"";
        }

        return "mandatory=true";
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
