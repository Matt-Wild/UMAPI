package com.spilledsoup.umapi.runtime;

import java.util.Arrays;
import java.util.Objects;

public final class MinecraftVersion implements Comparable<MinecraftVersion> {
    private final String value;
    private final int[] parts;

    private MinecraftVersion(String value, int[] parts) {
        this.value = value;
        this.parts = parts;
    }

    public static MinecraftVersion of(String value) {
        Objects.requireNonNull(value, "value");

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Minecraft version must not be blank.");
        }

        String[] rawParts = trimmed.split("\\.");
        int[] parsedParts = new int[rawParts.length];
        for (int index = 0; index < rawParts.length; index++) {
            parsedParts[index] = parsePart(rawParts[index], trimmed);
        }

        return new MinecraftVersion(trimmed, trimTrailingZeros(parsedParts));
    }

    public String value() {
        return value;
    }

    public boolean is(String version) {
        return compareTo(of(version)) == 0;
    }

    public boolean isAtLeast(String version) {
        return compareTo(of(version)) >= 0;
    }

    public boolean isBefore(String version) {
        return compareTo(of(version)) < 0;
    }

    @Override
    public int compareTo(MinecraftVersion other) {
        Objects.requireNonNull(other, "other");

        int length = Math.max(parts.length, other.parts.length);
        for (int index = 0; index < length; index++) {
            int left = index < parts.length ? parts[index] : 0;
            int right = index < other.parts.length ? other.parts[index] : 0;

            if (left != right) {
                return Integer.compare(left, right);
            }
        }

        return 0;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof MinecraftVersion version)) {
            return false;
        }

        return Arrays.equals(parts, version.parts);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(parts);
    }

    @Override
    public String toString() {
        return value;
    }

    private static int parsePart(String part, String version) {
        if (part.isEmpty()) {
            throw new IllegalArgumentException("Invalid Minecraft version: " + version);
        }

        for (int index = 0; index < part.length(); index++) {
            if (!Character.isDigit(part.charAt(index))) {
                throw new IllegalArgumentException("Invalid Minecraft version: " + version);
            }
        }

        return Integer.parseInt(part);
    }

    private static int[] trimTrailingZeros(int[] parts) {
        int length = parts.length;
        while (length > 1 && parts[length - 1] == 0) {
            length--;
        }

        return Arrays.copyOf(parts, length);
    }
}
