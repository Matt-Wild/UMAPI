package com.spilledsoup.umapi.gradle;

final class UMAPIExportName {

    private UMAPIExportName() {
    }

    static String jarFileName(
            String modName,
            String modVersion,
            String loader,
            String minecraftVersion
    ) {
        return sanitizePart(modName)
                + "-v"
                + sanitizePart(modVersion)
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
