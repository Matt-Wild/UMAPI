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

    public void forge(String minecraftVersion) {
        if (!Forge1201Target.supports(minecraftVersion)) {
            throw new IllegalStateException(
                    "UMAPI currently only supports "
                            + Forge1201Target.declaration()
                            + "."
            );
        }

        if (!forgeTargets.isEmpty()) {
            throw new IllegalStateException(
                    "UMAPI currently supports exactly one Forge target."
            );
        }

        forgeTargets.add(new ForgeTarget(minecraftVersion));
        runtimeTargets.add(Forge1201Target.runtimeTarget(minecraftVersion));
    }

    public void quilt(String minecraftVersion) {
        if (!Quilt1201Target.supports(minecraftVersion)) {
            throw new IllegalStateException(
                    "UMAPI currently only supports "
                            + Quilt1201Target.declaration()
                            + "."
            );
        }

        if (!quiltTargets.isEmpty()) {
            throw new IllegalStateException(
                    "UMAPI currently supports exactly one Quilt target."
            );
        }

        quiltTargets.add(new QuiltTarget(minecraftVersion));
        runtimeTargets.add(Quilt1201Target.runtimeTarget(minecraftVersion));
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
            case "forge" -> Forge1201Target.configure(
                    project,
                    umapiVersion,
                    mod,
                    target.minecraftVersion()
            );
            case "quilt" -> Quilt1201Target.configure(
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
        var exportUMAPI = UMAPIExportTasks.getOrCreateExportTask(project);

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
