package com.spilledsoup.umapi.gradle;

enum UMAPISettingsTarget {
    FABRIC("fabric-", "net.fabricmc:fabric-loom:1.17.19"),
    QUILT("quilt-", "org.quiltmc:loom:1.14.3");

    private final String targetPrefix;
    private final String toolingDependency;

    UMAPISettingsTarget(String targetPrefix, String toolingDependency) {
        this.targetPrefix = targetPrefix;
        this.toolingDependency = toolingDependency;
    }

    static String toolingDependencyFor(String targetId) {
        if (targetId == null || targetId.isBlank()) {
            return null;
        }

        for (UMAPISettingsTarget target : values()) {
            if (targetId.startsWith(target.targetPrefix)) {
                return target.toolingDependency;
            }
        }

        return null;
    }
}
