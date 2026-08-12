package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.TaskProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UMAPITargetsExtension {
    private static final String ACTIVE_TARGET_PROPERTY = "umapi.activeTarget";
    private static final String EXPORT_TASK = "exportUMAPI";

    private final Project project;
    private final String umapiVersion;
    private final UMAPIModExtension mod;
    private final List<FabricTarget> fabricTargets = new ArrayList<>();
    private final List<NeoForgeTarget> neoForgeTargets = new ArrayList<>();
    private final List<UMAPIRuntimeTarget> runtimeTargets = new ArrayList<>();
    private boolean configured;
    private UMAPIRuntimeTarget configuredTarget;

    public UMAPITargetsExtension(
            Project project,
            String umapiVersion,
            UMAPIModExtension mod
    ) {
        this.project = project;
        this.umapiVersion = umapiVersion;
        this.mod = mod;
    }

    public void fabric(String minecraftVersion) {
        if (!Fabric1201Target.supports(minecraftVersion)) {
            throw new IllegalStateException(
                    "UMAPI currently only supports "
                            + Fabric1201Target.declaration()
                            + "."
            );
        }

        if (!fabricTargets.isEmpty()) {
            throw new IllegalStateException(
                    "UMAPI currently supports exactly one Fabric target."
            );
        }

        fabricTargets.add(new FabricTarget(minecraftVersion));
        runtimeTargets.add(Fabric1201Target.runtimeTarget(minecraftVersion));
    }

    public void neoforge(String minecraftVersion) {
        if (!NeoForge1201Target.supports(minecraftVersion)) {
            throw new IllegalStateException(
                    "UMAPI currently only supports "
                            + NeoForge1201Target.declaration()
                            + "."
            );
        }

        if (!neoForgeTargets.isEmpty()) {
            throw new IllegalStateException(
                    "UMAPI currently supports exactly one NeoForge target."
            );
        }

        neoForgeTargets.add(new NeoForgeTarget(minecraftVersion));
        runtimeTargets.add(NeoForge1201Target.runtimeTarget(minecraftVersion));
    }

    void configureDeclaredTargets() {
        if (configured) {
            throw new IllegalStateException("UMAPI targets can only be configured once.");
        }

        configured = true;

        if (runtimeTargets.isEmpty()) {
            return;
        }

        if (runtimeTargets.size() == 1) {
            configureDirectTarget(runtimeTargets.get(0));
            return;
        }

        String activeTargetId = activeTargetId();

        if (activeTargetId != null) {
            configureDirectTarget(findTarget(activeTargetId));
            return;
        }

        configureMultiTargetTasks();
    }

    public List<FabricTarget> getFabricTargets() {
        return Collections.unmodifiableList(fabricTargets);
    }

    public List<NeoForgeTarget> getNeoForgeTargets() {
        return Collections.unmodifiableList(neoForgeTargets);
    }

    List<UMAPIRuntimeTarget> getRuntimeTargets() {
        return Collections.unmodifiableList(runtimeTargets);
    }

    List<UMAPIRuntimeTarget> getShortcutRuntimeTargets() {
        if (configuredTarget != null) {
            return List.of(configuredTarget);
        }

        return getRuntimeTargets();
    }

    public record FabricTarget(String minecraftVersion) {
    }

    public record NeoForgeTarget(String minecraftVersion) {
    }

    private void configureDirectTarget(UMAPIRuntimeTarget target) {
        configuredTarget = target;

        switch (target.loader()) {
            case "fabric" -> Fabric1201Target.configure(
                    project,
                    umapiVersion,
                    mod,
                    target.minecraftVersion()
            );
            case "neoforge" -> NeoForge1201Target.configure(
                    project,
                    umapiVersion,
                    mod,
                    target.minecraftVersion()
            );
            default -> throw new IllegalStateException(
                    "UMAPI does not know how to configure target '"
                            + target.id()
                            + "'."
            );
        }
    }

    private UMAPIRuntimeTarget findTarget(String targetId) {
        return runtimeTargets.stream()
                .filter(target -> target.id().equals(targetId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "UMAPI target '"
                                + targetId
                                + "' does not match any declared target."
                ));
    }

    private void configureMultiTargetTasks() {
        var exportUMAPI = getOrCreateExportTask();

        for (UMAPIRuntimeTarget target : runtimeTargets) {
            var exportTarget = registerTargetBuildTask(
                    target.exportTaskName(),
                    target,
                    "Exports the " + displayName(target) + " UMAPI target jar."
            );

            exportUMAPI.configure(task -> task.dependsOn(exportTarget));

            registerTargetBuildTask(
                    target.clientTaskName(),
                    target,
                    "Runs the " + displayName(target) + " UMAPI client runtime."
            );

            registerTargetBuildTask(
                    target.serverTaskName(),
                    target,
                    "Runs the " + displayName(target) + " UMAPI server runtime."
            );
        }

        project.getTasks()
                .named("assemble")
                .configure(task -> task.dependsOn(exportUMAPI));
    }

    private TaskProvider<Exec> registerTargetBuildTask(
            String taskName,
            UMAPIRuntimeTarget target,
            String description
    ) {
        return project.getTasks().register(taskName, Exec.class, task -> {
            task.setGroup("umapi");
            task.setDescription(description);
            task.setWorkingDir(project.getProjectDir());
            task.commandLine(
                    gradleWrapperPath(),
                    "-P" + ACTIVE_TARGET_PROPERTY + "=" + target.id(),
                    taskName
            );
        });
    }

    private TaskProvider<Task> getOrCreateExportTask() {
        if (project.getTasks().findByName(EXPORT_TASK) != null) {
            return project.getTasks().named(EXPORT_TASK);
        }

        return project.getTasks().register(EXPORT_TASK, task -> {
            task.setGroup("umapi");
            task.setDescription(
                    "Exports all configured UMAPI target jars to the UMAPI exports directory."
            );
        });
    }

    private String activeTargetId() {
        Object activeTarget = project.findProperty(ACTIVE_TARGET_PROPERTY);

        if (activeTarget == null || activeTarget.toString().isBlank()) {
            return null;
        }

        return activeTarget.toString();
    }

    private static String displayName(UMAPIRuntimeTarget target) {
        return target.loader() + " " + target.minecraftVersion();
    }

    private String gradleWrapperPath() {
        String wrapperName = System.getProperty("os.name")
                .toLowerCase()
                .contains("win")
                ? "gradlew.bat"
                : "gradlew";

        return project.file(wrapperName).getAbsolutePath();
    }
}
