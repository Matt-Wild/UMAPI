package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.TaskProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UMAPITargetsExtension {
    private static final String ACTIVE_TARGET_PROPERTY = "umapi.activeTarget";

    private final Project project;
    private final String umapiVersion;
    private final UMAPIModExtension mod;
    private final List<FabricTarget> fabricTargets = new ArrayList<>();
    private final List<NeoForgeTarget> neoForgeTargets = new ArrayList<>();
    private final List<ForgeTarget> forgeTargets = new ArrayList<>();
    private final List<QuiltTarget> quiltTargets = new ArrayList<>();
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
        var target = requireTarget(UMAPILoader.FABRIC, minecraftVersion);

        fabricTargets.add(new FabricTarget(minecraftVersion));
        runtimeTargets.add(target.runtimeTarget());
    }

    public void neoforge(String minecraftVersion) {
        var target = requireTarget(UMAPILoader.NEOFORGE, minecraftVersion);

        neoForgeTargets.add(new NeoForgeTarget(minecraftVersion));
        runtimeTargets.add(target.runtimeTarget());
    }

    public void forge(String minecraftVersion) {
        var target = requireTarget(UMAPILoader.FORGE, minecraftVersion);

        forgeTargets.add(new ForgeTarget(minecraftVersion));
        runtimeTargets.add(target.runtimeTarget());
    }

    public void quilt(String minecraftVersion) {
        var target = requireTarget(UMAPILoader.QUILT, minecraftVersion);

        quiltTargets.add(new QuiltTarget(minecraftVersion));
        runtimeTargets.add(target.runtimeTarget());
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

    public List<ForgeTarget> getForgeTargets() {
        return Collections.unmodifiableList(forgeTargets);
    }

    public List<QuiltTarget> getQuiltTargets() {
        return Collections.unmodifiableList(quiltTargets);
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

    public record ForgeTarget(String minecraftVersion) {
    }

    public record QuiltTarget(String minecraftVersion) {
    }

    private UMAPITargetDefinition requireTarget(
            UMAPILoader loader,
            String minecraftVersion
    ) {
        var target = UMAPISupportedTargets.require(loader, minecraftVersion);

        if (runtimeTargets.stream().anyMatch(existing -> existing.id().equals(target.id()))) {
            throw new IllegalStateException(
                    "UMAPI target '"
                            + target.id()
                            + "' has already been declared."
            );
        }

        return target;
    }

    private void configureDirectTarget(UMAPIRuntimeTarget target) {
        configuredTarget = UMAPISupportedTargets.configure(
                project,
                umapiVersion,
                mod,
                target
        );
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
        var exportUMAPI = UMAPIExportTasks.getOrCreateExportTask(project);
        TaskProvider<Exec> previousExportTarget = null;

        for (UMAPIRuntimeTarget target : runtimeTargets) {
            var exportTarget = registerTargetBuildTask(
                    target.exportTaskName(),
                    target,
                    "Exports the " + displayName(target) + " UMAPI target jar."
            );

            if (previousExportTarget != null) {
                TaskProvider<Exec> previous = previousExportTarget;
                exportTarget.configure(task -> task.mustRunAfter(previous));
            }

            previousExportTarget = exportTarget;
            exportUMAPI.configure(task -> task.dependsOn(exportTarget));

            registerTargetBuildTask(
                    target.clientTaskName(),
                    target,
                    UMAPIRuntimeTasks.description(
                            target.loader(),
                            target.minecraftVersion(),
                            UMAPIRuntimeTasks.Side.CLIENT
                    )
            );

            registerTargetBuildTask(
                    target.serverTaskName(),
                    target,
                    UMAPIRuntimeTasks.description(
                            target.loader(),
                            target.minecraftVersion(),
                            UMAPIRuntimeTasks.Side.SERVER
                    )
            );
        }
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
                    "--project-prop=" + ACTIVE_TARGET_PROPERTY + "=" + target.id(),
                    taskName
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
