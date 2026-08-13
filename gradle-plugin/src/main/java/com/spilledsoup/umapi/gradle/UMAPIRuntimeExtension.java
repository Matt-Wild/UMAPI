package com.spilledsoup.umapi.gradle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UMAPIRuntimeExtension {
    private String defaultTarget;
    private String defaultLoader;
    private String defaultMinecraftVersion;

    public String getDefaultTarget() {
        return defaultTarget;
    }

    public void setDefaultTarget(String defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    public String getDefaultLoader() {
        return defaultLoader;
    }

    public void setDefaultLoader(String defaultLoader) {
        this.defaultLoader = defaultLoader;
    }

    public String getDefaultMinecraftVersion() {
        return defaultMinecraftVersion;
    }

    public void setDefaultMinecraftVersion(String defaultMinecraftVersion) {
        this.defaultMinecraftVersion = defaultMinecraftVersion;
    }

    UMAPIRuntimeTarget selectDefault(List<UMAPIRuntimeTarget> targets) {
        if (targets.isEmpty()) {
            throw new IllegalStateException(
                    "UMAPI cannot choose a runtime because no targets were declared."
            );
        }

        if (hasText(defaultTarget)) {
            return targets.stream()
                    .filter(target -> target.id().equals(defaultTarget))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "UMAPI runtime default target '"
                                    + defaultTarget
                                    + "' does not match any declared target."
                    ));
        }

        var candidates = new ArrayList<>(targets);

        if (hasText(defaultLoader)) {
            String selectedLoader = normalize(defaultLoader);
            candidates.removeIf(target -> !normalize(target.loader()).equals(selectedLoader));

            if (candidates.isEmpty()) {
                throw new IllegalStateException(
                        "UMAPI runtime default loader '"
                                + defaultLoader
                                + "' does not match any declared target."
                );
            }
        }

        if (hasText(defaultMinecraftVersion)) {
            candidates.removeIf(target -> !target.minecraftVersion().equals(defaultMinecraftVersion));

            if (candidates.isEmpty()) {
                throw new IllegalStateException(
                        "UMAPI runtime default Minecraft version '"
                                + defaultMinecraftVersion
                                + "' does not match any declared target."
                );
            }
        }

        String latestVersion = candidates.stream()
                .map(UMAPIRuntimeTarget::minecraftVersion)
                .max(UMAPIMinecraftVersion::compare)
                .orElseThrow();

        return candidates.stream()
                .filter(target -> target.minecraftVersion().equals(latestVersion))
                .min((left, right) -> Integer.compare(
                        loaderPreference(left.loader()),
                        loaderPreference(right.loader())
                ))
                .orElseThrow();
    }

    private static int loaderPreference(String loader) {
        return UMAPILoader.runtimePriority(loader);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}
