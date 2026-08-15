package com.spilledsoup.umapi.content;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public final class ContentRegistry {
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern ID_PATTERN = Pattern.compile("[a-z0-9_./-]+");

    private final String namespace;
    private final Map<String, ItemContent> items = new LinkedHashMap<>();

    private ContentRegistry(String namespace) {
        this.namespace = validateNamespace(namespace);
    }

    public static ContentRegistry forMod(String namespace) {
        return new ContentRegistry(namespace);
    }

    public String namespace() {
        return namespace;
    }

    public ItemContent item(String id, String name) {
        validateId(id);
        Objects.requireNonNull(name, "name");

        if (items.containsKey(id)) {
            throw new IllegalArgumentException("Item content already exists: " + id);
        }

        ItemContent item = new ItemContent(namespace, id, name);
        items.put(id, item);
        return item;
    }

    public Optional<ItemContent> item(String id) {
        validateId(id);
        return Optional.ofNullable(items.get(id));
    }

    public Collection<ItemContent> items() {
        return Collections.unmodifiableCollection(items.values());
    }

    public int itemCount() {
        return items.size();
    }

    private static String validateNamespace(String namespace) {
        Objects.requireNonNull(namespace, "namespace");

        String trimmed = namespace.trim();
        if (!NAMESPACE_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid content namespace: " + namespace);
        }

        return trimmed;
    }

    private static void validateId(String id) {
        Objects.requireNonNull(id, "id");

        if (!ID_PATTERN.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid content id: " + id);
        }
    }
}
