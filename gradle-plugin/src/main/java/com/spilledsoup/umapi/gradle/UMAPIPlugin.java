package com.spilledsoup.umapi.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

import java.io.IOException;
import java.util.Properties;

public final class UMAPIPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getLogger().lifecycle(
                "UMAPI Gradle plugin applied to " + project.getName()
        );

        project.getRepositories().mavenCentral();

        project.getPluginManager().apply("net.fabricmc.fabric-loom-remap");

        String umapiVersion = getUMAPIVersion();

        UMAPIExtension extension = project.getExtensions()
                .create("umapi", UMAPIExtension.class, project, umapiVersion);

        project.getPluginManager().withPlugin("java", ignored -> {
            project.getExtensions().configure(
                    JavaPluginExtension.class,
                    java -> java.getToolchain()
                            .getLanguageVersion()
                            .set(JavaLanguageVersion.of(17))
            );
        });

        project.getDependencies().add(
                "implementation",
                "com.spilledsoup.umapi:api:" + umapiVersion
        );

        configureTesting(project);

        project.afterEvaluate(ignored -> {
            var fabricTargets = extension.getTargets().getFabricTargets();

            if (fabricTargets.isEmpty()) {
                throw new IllegalStateException(
                        "UMAPI currently requires a Fabric target: "
                                + Fabric1201Target.declaration()
                                + "."
                );
            }

            extension.getMod().validate();
        });
    }

    private static void configureTesting(Project project) {
        var dependencies = project.getDependencies();

        dependencies.add(
                "testImplementation",
                dependencies.platform("org.junit:junit-bom:6.0.0")
        );

        dependencies.add(
                "testImplementation",
                "org.junit.jupiter:junit-jupiter"
        );

        dependencies.add(
                "testRuntimeOnly",
                "org.junit.platform:junit-platform-launcher"
        );

        project.getTasks()
                .withType(Test.class)
                .configureEach(Test::useJUnitPlatform);
    }

    private static String getUMAPIVersion() {
        try (var stream = UMAPIPlugin.class
                .getClassLoader()
                .getResourceAsStream("umapi.properties")) {

            if (stream == null) {
                throw new IllegalStateException("Could not determine UMAPI version.");
            }

            var properties = new Properties();
            properties.load(stream);

            String version = properties.getProperty("version");
            if (version == null || version.isBlank()) {
                throw new IllegalStateException("Could not determine UMAPI version.");
            }

            return version;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not determine UMAPI version.",
                    exception
            );
        }
    }
}
