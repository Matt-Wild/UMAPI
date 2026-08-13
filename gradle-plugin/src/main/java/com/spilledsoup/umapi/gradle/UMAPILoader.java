package com.spilledsoup.umapi.gradle;

import java.util.Locale;
import java.util.Optional;

enum UMAPILoader {
    FABRIC("fabric", "Fabric", 0),
    NEOFORGE("neoforge", "NeoForge", 1),
    FORGE("forge", "Forge", 2),
    QUILT("quilt", "Quilt", 3);

    private final String id;
    private final String displayName;
    private final int runtimePriority;

    UMAPILoader(String id, String displayName, int runtimePriority) {
        this.id = id;
        this.displayName = displayName;
        this.runtimePriority = runtimePriority;
    }

    String id() {
        return id;
    }

    String displayName() {
        return displayName;
    }

    String taskNamePart() {
        return displayName;
    }

    int runtimePriority() {
        return runtimePriority;
    }

    static String displayName(String loaderId) {
        return find(loaderId)
                .map(UMAPILoader::displayName)
                .orElse(loaderId);
    }

    static String taskNamePart(String loaderId) {
        return find(loaderId)
                .map(UMAPILoader::taskNamePart)
                .orElse(null);
    }

    static int runtimePriority(String loaderId) {
        return find(loaderId)
                .map(UMAPILoader::runtimePriority)
                .orElse(Integer.MAX_VALUE);
    }

    private static Optional<UMAPILoader> find(String loaderId) {
        if (loaderId == null || loaderId.isBlank()) {
            return Optional.empty();
        }

        String normalizedLoaderId = normalize(loaderId);

        for (UMAPILoader loader : values()) {
            if (loader.id.equals(normalizedLoaderId)) {
                return Optional.of(loader);
            }
        }

        return Optional.empty();
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT);
    }
}
