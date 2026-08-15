package com.spilledsoup.umapi.gradle;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

final class UMAPILoomTargetSupport {
    private static final String REMAPPED_UMAPI_CACHE_PATH =
            ".gradle/loom-cache/remapped_mods/remapped/com/spilledsoup/umapi";
    private static final String RUN_CLIENT_TASK = "runClient";
    private static final String RUN_SERVER_TASK = "runServer";

    private UMAPILoomTargetSupport() {
    }

    static void configureMinecraft(
            Project project,
            String loomPluginId,
            String minecraftVersion,
            String loaderDisplayName,
            Set<String> runtimeTasks
    ) {
        configureMinecraft(
                project,
                loomPluginId,
                minecraftVersion,
                loaderDisplayName,
                runtimeTasks,
                true
        );
    }

    static void configureMinecraft(
            Project project,
            String loomPluginId,
            String minecraftVersion,
            String loaderDisplayName,
            Set<String> runtimeTasks,
            boolean useExplicitOfficialMojangMappings
    ) {
        project.getPluginManager().apply(loomPluginId);

        project.getDependencies().add(
                "minecraft",
                "com.mojang:minecraft:" + minecraftVersion
        );

        invalidateLocalRuntimeRemapCache(project, loaderDisplayName, runtimeTasks);

        if (useExplicitOfficialMojangMappings) {
            project.getDependencies().add(
                    "mappings",
                    officialMojangMappings(project, loaderDisplayName)
            );
        }
    }

    static void addPlatformDependency(
            Project project,
            String platformArtifactId,
            String umapiVersion
    ) {
        addPlatformDependency(
                project,
                platformArtifactId,
                umapiVersion,
                "modImplementation"
        );
    }

    static void addPlatformDependency(
            Project project,
            String platformArtifactId,
            String umapiVersion,
            String dependencyConfiguration
    ) {
        project.getDependencies().add(
                dependencyConfiguration,
                "com.spilledsoup.umapi:" + platformArtifactId + ":" + umapiVersion
        );
    }

    static void configureLoomModDependencies(
            Project project,
            String umapiVersion,
            UMAPITargetDefinition target,
            String loomPluginId,
            UMAPILoader loader,
            String loaderDependencyNotation
    ) {
        configureLoomModDependencies(
                project,
                umapiVersion,
                target,
                loomPluginId,
                loader,
                loaderDependencyNotation,
                "modImplementation",
                true
        );
    }

    static void configureLoomModDependencies(
            Project project,
            String umapiVersion,
            UMAPITargetDefinition target,
            String loomPluginId,
            UMAPILoader loader,
            String loaderDependencyNotation,
            String dependencyConfiguration,
            boolean useExplicitOfficialMojangMappings
    ) {
        configureMinecraft(
                project,
                loomPluginId,
                target.minecraftVersion(),
                loader.displayName(),
                runtimeTasks(target.descriptor()),
                useExplicitOfficialMojangMappings
        );

        addPlatformDependency(
                project,
                target.platformArtifactId(),
                umapiVersion,
                dependencyConfiguration
        );

        project.getDependencies().add(
                dependencyConfiguration,
                loaderDependencyNotation
        );
    }

    static void configureRuntime(
            Project project,
            UMAPITargetDescriptor target,
            String clientDirectoryName,
            String serverDirectoryName,
            UMAPIModExtension mod
    ) {
        project.afterEvaluate(ignored -> configureMainModSourceSet(project, mod.getId()));
        disableConfigurationCacheForNativeRuntime(project);

        UMAPILoomRunDirectories.configure(
                project,
                clientDirectoryName,
                serverDirectoryName
        );

        UMAPIRuntimeTasks.registerWrapper(
                project,
                target.clientTaskName(),
                RUN_CLIENT_TASK,
                target.loader(),
                target.minecraftVersion(),
                UMAPIRuntimeTasks.Side.CLIENT
        );

        UMAPIRuntimeTasks.registerWrapper(
                project,
                target.serverTaskName(),
                RUN_SERVER_TASK,
                target.loader(),
                target.minecraftVersion(),
                UMAPIRuntimeTasks.Side.SERVER
        );
    }

    static Set<String> runtimeTasks(UMAPITargetDescriptor target) {
        return Set.of(
                RUN_CLIENT_TASK,
                RUN_SERVER_TASK,
                target.clientTaskName(),
                target.serverTaskName(),
                "runUMAPIClient",
                "runUMAPIServer"
        );
    }

    private static void disableConfigurationCacheForNativeRuntime(Project project) {
        project.getTasks()
                .matching(task ->
                        RUN_CLIENT_TASK.equals(task.getName())
                                || RUN_SERVER_TASK.equals(task.getName())
                )
                .configureEach(task -> task.notCompatibleWithConfigurationCache(
                        "Loom game run tasks currently keep project state that Gradle cannot serialize."
                ));
    }

    private static void configureMainModSourceSet(Project project, String modId) {
        Object loom = project.getExtensions().getByName("loom");
        Object mods = invoke(loom, "getMods", "Could not configure Loom mods block.");

        if (!(mods instanceof NamedDomainObjectContainer<?>)) {
            throw new IllegalStateException("Could not configure Loom mods block.");
        }

        @SuppressWarnings("unchecked")
        var modSettings = ((NamedDomainObjectContainer<Object>) mods).maybeCreate(modId);

        invoke(
                modSettings,
                "sourceSet",
                "Could not configure Loom mods block.",
                String.class,
                String.class,
                "main",
                project.getPath()
        );
    }

    private static Object officialMojangMappings(Project project, String loaderDisplayName) {
        Object loom = project.getExtensions().getByName("loom");

        try {
            return loom.getClass()
                    .getMethod("officialMojangMappings")
                    .invoke(loom);
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw new IllegalStateException(
                    "Could not configure "
                            + loaderDisplayName
                            + " official Mojang mappings.",
                    exception
            );
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(
                    "Could not configure "
                            + loaderDisplayName
                            + " official Mojang mappings.",
                    exception.getCause()
            );
        }
    }

    private static Object invoke(
            Object target,
            String methodName,
            String errorMessage
    ) {
        try {
            return target.getClass()
                    .getMethod(methodName)
                    .invoke(target);
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw new IllegalStateException(errorMessage, exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(errorMessage, exception.getCause());
        }
    }

    private static void invoke(
            Object target,
            String methodName,
            String errorMessage,
            Class<?> firstParameterType,
            Class<?> secondParameterType,
            Object firstValue,
            Object secondValue
    ) {
        try {
            target.getClass()
                    .getMethod(methodName, firstParameterType, secondParameterType)
                    .invoke(target, firstValue, secondValue);
        } catch (IllegalAccessException | NoSuchMethodException exception) {
            throw new IllegalStateException(errorMessage, exception);
        } catch (InvocationTargetException exception) {
            throw new IllegalStateException(errorMessage, exception.getCause());
        }
    }

    private static void invalidateLocalRuntimeRemapCache(
            Project project,
            String loaderDisplayName,
            Set<String> runtimeTasks
    ) {
        if (!isLocalUMAPIBuild(project) || !isRuntimeTaskRequested(project, runtimeTasks)) {
            return;
        }

        var cacheDirectory = project.getLayout()
                .getProjectDirectory()
                .dir(REMAPPED_UMAPI_CACHE_PATH)
                .getAsFile();

        if (cacheDirectory.exists() && project.delete(cacheDirectory)) {
            project.getLogger().lifecycle(
                    "UMAPI local development runtime detected; invalidated "
                            + loaderDisplayName
                            + " Loom's remapped UMAPI cache."
            );
        }
    }

    private static boolean isLocalUMAPIBuild(Project project) {
        return project.getGradle()
                .getIncludedBuilds()
                .stream()
                .anyMatch(includedBuild ->
                        "UMAPI".equalsIgnoreCase(includedBuild.getName())
                                || "UMAPI".equalsIgnoreCase(includedBuild.getProjectDir().getName())
                );
    }

    private static boolean isRuntimeTaskRequested(
            Project project,
            Set<String> runtimeTasks
    ) {
        return project.getGradle()
                .getStartParameter()
                .getTaskNames()
                .stream()
                .map(UMAPILoomTargetSupport::simpleTaskName)
                .anyMatch(runtimeTasks::contains);
    }

    private static String simpleTaskName(String taskName) {
        int separator = taskName.lastIndexOf(':');

        if (separator == -1) {
            return taskName;
        }

        return taskName.substring(separator + 1);
    }
}
