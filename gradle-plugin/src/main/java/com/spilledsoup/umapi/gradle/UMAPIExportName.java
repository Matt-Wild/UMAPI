package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;

final class UMAPIExportName {

    private UMAPIExportName() {
    }

    static String jarFileName(
            Project project,
            UMAPIModExtension mod,
            String loader,
            String minecraftVersion
    ) {
        return sanitizePart(mod.getName())
                + "-v"
                + sanitizePart(project.getVersion().toString())
                + "-"
                + sanitizePart(loader)
                + "-mc"
                + sanitizePart(minecraftVersion)
                + ".jar";
    }

    private static String sanitizePart(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        String sanitized = value.trim()
                .replaceAll("[^A-Za-z0-9._-]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");

        if (sanitized.isBlank()) {
            return "unknown";
        }

        return sanitized;
    }
}
