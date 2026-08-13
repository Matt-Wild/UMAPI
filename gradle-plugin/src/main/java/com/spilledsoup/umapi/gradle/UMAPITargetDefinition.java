package com.spilledsoup.umapi.gradle;

record UMAPITargetDefinition(UMAPILoader loader, String minecraftVersion) {

    UMAPITargetDefinition {
        if (loader == null) {
            throw new IllegalArgumentException("UMAPI target loader must not be null.");
        }

        UMAPIMinecraftVersion.validate(minecraftVersion);
    }

    String loaderId() {
        return loader.id();
    }

    String id() {
        return loaderId() + "-" + minecraftVersion;
    }

    String declaration() {
        return loaderId() + "(\"" + minecraftVersion + "\")";
    }

    String platformArtifactId() {
        return id();
    }

    String clientWorkingDirectory() {
        return workingDirectory("Client");
    }

    String serverWorkingDirectory() {
        return workingDirectory("Server");
    }

    UMAPITargetDescriptor descriptor() {
        return new UMAPITargetDescriptor(loaderId(), minecraftVersion);
    }

    UMAPIRuntimeTarget runtimeTarget() {
        return descriptor().runtimeTarget();
    }

    private String workingDirectory(String side) {
        return loader.runDirectoryPart()
                + UMAPIMinecraftVersion.compact(minecraftVersion)
                + side;
    }
}
