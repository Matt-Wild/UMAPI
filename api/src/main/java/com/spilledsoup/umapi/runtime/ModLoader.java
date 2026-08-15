package com.spilledsoup.umapi.runtime;

public enum ModLoader {
    FABRIC("fabric", "Fabric"),
    QUILT("quilt", "Quilt"),
    FORGE("forge", "Forge"),
    NEOFORGE("neoforge", "NeoForge");

    private final String id;
    private final String displayName;

    ModLoader(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isFabricLike() {
        return this == FABRIC || this == QUILT;
    }

    public boolean isForgeFamily() {
        return this == FORGE || this == NEOFORGE;
    }
}
