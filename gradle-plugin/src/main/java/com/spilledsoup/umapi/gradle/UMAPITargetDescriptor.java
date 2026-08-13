package com.spilledsoup.umapi.gradle;

record UMAPITargetDescriptor(String loader, String minecraftVersion) {

    UMAPITargetDescriptor {
        if (loader == null || loader.isBlank()) {
            throw new IllegalArgumentException("UMAPI target loader must not be blank.");
        }

        if (minecraftVersion == null || minecraftVersion.isBlank()) {
            throw new IllegalArgumentException("UMAPI target Minecraft version must not be blank.");
        }
    }

    String id() {
        return loader + "-" + minecraftVersion;
    }

    String exportTaskName() {
        return "exportUMAPI" + UMAPIRuntimeTasks.targetTaskName(loader, minecraftVersion);
    }

    String clientTaskName() {
        return UMAPIRuntimeTasks.clientTaskName(loader, minecraftVersion);
    }

    String serverTaskName() {
        return UMAPIRuntimeTasks.serverTaskName(loader, minecraftVersion);
    }

    UMAPIRuntimeTarget runtimeTarget() {
        return new UMAPIRuntimeTarget(
                id(),
                loader,
                minecraftVersion,
                exportTaskName(),
                clientTaskName(),
                serverTaskName()
        );
    }
}
