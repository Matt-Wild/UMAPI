package com.spilledsoup.umapi.gradle;

final class UMAPIMinecraftVersion {

    private UMAPIMinecraftVersion() {
    }

    static void validate(String minecraftVersion) {
        if (minecraftVersion == null || minecraftVersion.isBlank()) {
            throw new IllegalArgumentException(
                    "UMAPI target Minecraft version must not be blank."
            );
        }
    }

    static String compact(String minecraftVersion) {
        validate(minecraftVersion);
        return minecraftVersion.replaceAll("[^A-Za-z0-9]+", "");
    }

    static String taskNamePart(String minecraftVersion) {
        validate(minecraftVersion);
        return UMAPIGradleNames.taskNamePart(minecraftVersion);
    }

    static int compare(String left, String right) {
        String[] leftParts = left.split("\\.");
        String[] rightParts = right.split("\\.");
        int partCount = Math.max(leftParts.length, rightParts.length);

        for (int index = 0; index < partCount; index++) {
            int leftPart = versionPart(leftParts, index);
            int rightPart = versionPart(rightParts, index);

            if (leftPart != rightPart) {
                return Integer.compare(leftPart, rightPart);
            }
        }

        return left.compareTo(right);
    }

    private static int versionPart(String[] parts, int index) {
        if (index >= parts.length) {
            return 0;
        }

        try {
            return Integer.parseInt(parts[index]);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
