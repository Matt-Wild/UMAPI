package com.spilledsoup.umapi.content;

import com.spilledsoup.umapi.version.VersionedValue;

import java.util.Objects;

public final class ItemContent {
    private final String namespace;
    private final String id;
    private final String name;
    private final VersionedValue<String> texture = new VersionedValue<>("Texture path");

    ItemContent(String namespace, String id, String name) {
        this.namespace = Objects.requireNonNull(namespace, "namespace");
        this.id = Objects.requireNonNull(id, "id");
        this.name = validateName(name);
    }

    public String namespace() {
        return namespace;
    }

    public String id() {
        return id;
    }

    public String qualifiedId() {
        return namespace + ":" + id;
    }

    public String name() {
        return name;
    }

    public VersionedValue<String> texture() {
        return texture;
    }

    public ItemContent texture(String path) {
        texture.fallback(validateTexturePath(path));
        return this;
    }

    public ItemContent texture(String minecraftVersion, String path) {
        texture.add(minecraftVersion, validateTexturePath(path));
        return this;
    }

    private static String validateName(String name) {
        Objects.requireNonNull(name, "name");

        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Item name must not be blank.");
        }

        return trimmed;
    }

    private static String validateTexturePath(String path) {
        Objects.requireNonNull(path, "path");

        String trimmed = path.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Texture path must not be blank.");
        }

        return trimmed;
    }
}
