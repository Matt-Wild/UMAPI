package com.spilledsoup.umapi.gradle;

final class UMAPIGradleNames {

    private UMAPIGradleNames() {
    }

    static String taskNamePart(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        String sanitized = value.replaceAll("[^A-Za-z0-9]+", "");

        if (sanitized.isBlank()) {
            return "Unknown";
        }

        return Character.toUpperCase(sanitized.charAt(0)) + sanitized.substring(1);
    }
}
