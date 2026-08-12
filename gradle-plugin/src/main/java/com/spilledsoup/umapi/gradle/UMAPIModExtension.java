package com.spilledsoup.umapi.gradle;

import java.util.ArrayList;
import java.util.List;

public class UMAPIModExtension {
    private String id;
    private String name;
    private String description;
    private String entrypoint;
    private final List<String> authors = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEntrypoint() { return entrypoint; }
    public void setEntrypoint(String entrypoint) { this.entrypoint = entrypoint; }

    public List<String> getAuthors() { return authors; }

    void validate() {
        require(id, "umapi.mod.id");
        require(name, "umapi.mod.name");
        require(description, "umapi.mod.description");
        require(entrypoint, "umapi.mod.entrypoint");

        if (authors.isEmpty()) {
            throw new IllegalStateException("UMAPI requires at least one umapi.mod author.");
        }
    }

    private static void require(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("UMAPI requires " + name + ".");
        }
    }
}
