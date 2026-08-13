package com.spilledsoup.umapi.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

import java.net.URI;

public final class UMAPISettingsPlugin implements Plugin<Settings> {
    private static final String ACTIVE_TARGET_PROPERTY = "umapi.activeTarget";

    @Override
    public void apply(Settings settings) {
        var repositories = settings.getPluginManagement().getRepositories();

        repositories.mavenCentral();
        repositories.gradlePluginPortal();
        repositories.maven(repository -> {
            repository.setName("Quilt");
            repository.setUrl(URI.create("https://maven.quiltmc.org/repository/release/"));
        });
        repositories.maven(repository -> {
            repository.setName("Fabric");
            repository.setUrl(URI.create("https://maven.fabricmc.net/"));
        });
        repositories.maven(repository -> {
            repository.setName("NeoForged");
            repository.setUrl(URI.create("https://maven.neoforged.net/releases/"));
        });
        repositories.maven(repository -> {
            repository.setName("MinecraftForge");
            repository.setUrl(URI.create("https://maven.minecraftforge.net/"));
        });

        configureActiveTargetBuildscript(settings);
    }

    private static void configureActiveTargetBuildscript(Settings settings) {
        String activeTarget = settings.getGradle()
                .getStartParameter()
                .getProjectProperties()
                .get(ACTIVE_TARGET_PROPERTY);

        if (activeTarget == null) {
            return;
        }

        String toolingDependency = UMAPISettingsTarget.toolingDependencyFor(activeTarget);

        if (toolingDependency == null) {
            return;
        }

        settings.getGradle().beforeProject(project -> {
            configureTargetBuildscriptRepositories(project.getBuildscript().getRepositories());
            project.getBuildscript()
                    .getDependencies()
                    .add("classpath", toolingDependency);
        });
    }

    private static void configureTargetBuildscriptRepositories(
            org.gradle.api.artifacts.dsl.RepositoryHandler repositories
    ) {
        repositories.maven(repository -> {
            repository.setName("Quilt");
            repository.setUrl(URI.create("https://maven.quiltmc.org/repository/release/"));
        });
        repositories.maven(repository -> {
            repository.setName("Fabric");
            repository.setUrl(URI.create("https://maven.fabricmc.net/"));
        });
        repositories.mavenCentral();
        repositories.gradlePluginPortal();
    }
}
