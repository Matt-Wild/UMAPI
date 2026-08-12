package com.spilledsoup.umapi.gradle;

import org.gradle.api.Action;
import org.gradle.api.Project;

public class UMAPIExtension {
    private final UMAPITargetsExtension targets;
    private final UMAPIModExtension mod = new UMAPIModExtension();

    public UMAPIExtension(Project project, String umapiVersion) {
        targets = new UMAPITargetsExtension(project, umapiVersion, mod);
    }

    public void targets(Action<? super UMAPITargetsExtension> action) {
        action.execute(targets);
    }

    public void mod(Action<? super UMAPIModExtension> action) {
        action.execute(mod);
    }

    public UMAPITargetsExtension getTargets() {
        return targets;
    }

    public UMAPIModExtension getMod() {
        return mod;
    }
}
