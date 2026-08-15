package com.spilledsoup.umapi.runtime;

import java.util.Objects;

public final class RuntimeEnvironment {
    private final ModLoader loader;
    private final MinecraftVersion minecraftVersion;

    private RuntimeEnvironment(ModLoader loader, MinecraftVersion minecraftVersion) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.minecraftVersion = Objects.requireNonNull(minecraftVersion, "minecraftVersion");
    }

    public static RuntimeEnvironment of(ModLoader loader, String minecraftVersion) {
        return of(loader, MinecraftVersion.of(minecraftVersion));
    }

    public static RuntimeEnvironment of(ModLoader loader, MinecraftVersion minecraftVersion) {
        return new RuntimeEnvironment(loader, minecraftVersion);
    }

    public ModLoader loader() {
        return loader;
    }

    public MinecraftVersion minecraftVersion() {
        return minecraftVersion;
    }

    public boolean isLoader(ModLoader loader) {
        return this.loader == loader;
    }

    public boolean isMinecraft(String version) {
        return minecraftVersion.is(version);
    }

    public boolean isMinecraftAtLeast(String version) {
        return minecraftVersion.isAtLeast(version);
    }

    public boolean isMinecraftBefore(String version) {
        return minecraftVersion.isBefore(version);
    }

    @Override
    public String toString() {
        return loader.displayName() + " " + minecraftVersion;
    }
}
