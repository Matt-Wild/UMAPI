package com.spilledsoup.umapi.gradle;

import org.gradle.api.Action;
import org.gradle.api.Project;

public class UMAPIExtension {
    private final UMAPITargetsExtension targets;
    private final UMAPIModExtension mod = new UMAPIModExtension();
    private final UMAPIRuntimeExtension runtime = new UMAPIRuntimeExtension();

    public UMAPIExtension(Project project, String umapiVersion) {
        targets = new UMAPITargetsExtension(project, umapiVersion, mod);
    }

    public void targets(Action<? super UMAPITargetsExtension> action) {
        action.execute(targets);
        targets.configureDeclaredTargets();
    }

    public void mod(Action<? super UMAPIModExtension> action) {
        action.execute(mod);
    }

    public void runtime(Action<? super UMAPIRuntimeExtension> action) {
        action.execute(runtime);
    }

    public UMAPITargetsExtension getTargets() {
        return targets;
    }

    public UMAPIModExtension getMod() {
        return mod;
    }

    public UMAPIRuntimeExtension getRuntime() {
        return runtime;
    }
}
