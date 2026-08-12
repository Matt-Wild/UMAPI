package com.spilledsoup.umapi.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

import java.net.URI;

public final class UMAPISettingsPlugin implements Plugin<Settings> {
    @Override
    public void apply(Settings settings) {
        var repositories = settings.getPluginManagement().getRepositories();

        repositories.mavenCentral();
        repositories.gradlePluginPortal();
        repositories.maven(repository -> {
            repository.setName("Fabric");
            repository.setUrl(URI.create("https://maven.fabricmc.net/"));
        });
        repositories.maven(repository -> {
            repository.setName("NeoForged");
            repository.setUrl(URI.create("https://maven.neoforged.net/releases/"));
        });
    }
}
