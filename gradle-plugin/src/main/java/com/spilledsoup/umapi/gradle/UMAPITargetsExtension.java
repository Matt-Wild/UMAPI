package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UMAPITargetsExtension {
    private final Project project;
    private final String umapiVersion;
    private final UMAPIModExtension mod;
    private final List<FabricTarget> fabricTargets = new ArrayList<>();

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
        Fabric1201Target.configure(project, umapiVersion, mod, minecraftVersion);
    }

    public List<FabricTarget> getFabricTargets() {
        return Collections.unmodifiableList(fabricTargets);
    }

    public record FabricTarget(String minecraftVersion) {
    }
}
