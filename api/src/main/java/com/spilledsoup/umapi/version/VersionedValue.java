package com.spilledsoup.umapi.version;

import com.spilledsoup.umapi.runtime.MinecraftVersion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class VersionedValue<T> {
    private final String label;
    private final Map<MinecraftVersion, T> values = new LinkedHashMap<>();
    private T fallbackValue;
    private boolean hasFallbackValue;

    public VersionedValue(String label) {
        this.label = validateLabel(label);
    }

    public VersionedValue<T> add(String minecraftVersion, T value) {
        return add(MinecraftVersion.of(minecraftVersion), value);
    }

    public VersionedValue<T> add(MinecraftVersion minecraftVersion, T value) {
        Objects.requireNonNull(minecraftVersion, "minecraftVersion");
        Objects.requireNonNull(value, "value");

        if (values.containsKey(minecraftVersion)) {
            throw new IllegalArgumentException(
                    label + " already exists for Minecraft " + minecraftVersion + "."
            );
        }

        values.put(minecraftVersion, value);
        return this;
    }

    public VersionedValue<T> fallback(T value) {
        Objects.requireNonNull(value, "value");

        if (hasFallbackValue) {
            throw new IllegalArgumentException(label + " fallback already exists.");
        }

        fallbackValue = value;
        hasFallbackValue = true;
        return this;
    }

    public Optional<T> fallback() {
        if (hasFallbackValue) {
            return Optional.of(fallbackValue);
        }

        return Optional.empty();
    }

    public List<Entry<T>> entries() {
        List<Entry<T>> entries = new ArrayList<>();
        values.forEach((version, value) -> entries.add(new Entry<>(version, value)));
        return Collections.unmodifiableList(entries);
    }

    public Optional<Selection<T>> select(String minecraftVersion) {
        return select(MinecraftVersion.of(minecraftVersion));
    }

    public Optional<Selection<T>> select(MinecraftVersion minecraftVersion) {
        Objects.requireNonNull(minecraftVersion, "minecraftVersion");

        T exactValue = values.get(minecraftVersion);
        if (exactValue != null) {
            return Optional.of(new Selection<>(minecraftVersion, minecraftVersion, exactValue));
        }

        Optional<MinecraftVersion> nearestLower = values.keySet()
                .stream()
                .filter(version -> version.compareTo(minecraftVersion) < 0)
                .max(Comparator.naturalOrder());

        if (nearestLower.isPresent()) {
            MinecraftVersion sourceVersion = nearestLower.get();
            return Optional.of(new Selection<>(
                    minecraftVersion,
                    sourceVersion,
                    values.get(sourceVersion)
            ));
        }

        if (hasFallbackValue) {
            return Optional.of(new Selection<>(
                    minecraftVersion,
                    minecraftVersion,
                    fallbackValue,
                    true
            ));
        }

        return values.keySet()
                .stream()
                .filter(version -> version.compareTo(minecraftVersion) > 0)
                .min(Comparator.naturalOrder())
                .map(sourceVersion -> new Selection<>(
                        minecraftVersion,
                        sourceVersion,
                        values.get(sourceVersion)
                ));
    }

    public boolean isEmpty() {
        return values.isEmpty() && !hasFallbackValue;
    }

    private static String validateLabel(String label) {
        Objects.requireNonNull(label, "label");

        String trimmed = label.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Versioned value label must not be blank.");
        }

        return trimmed;
    }

    public record Entry<T>(MinecraftVersion minecraftVersion, T value) {
    }

    public record Selection<T>(
            MinecraftVersion requestedVersion,
            MinecraftVersion sourceVersion,
            T value,
            boolean fallback
    ) {
        public Selection(MinecraftVersion requestedVersion, MinecraftVersion sourceVersion, T value) {
            this(requestedVersion, sourceVersion, value, false);
        }

        public boolean isFallback() {
            return fallback;
        }
    }
}
