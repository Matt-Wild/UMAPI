package com.spilledsoup.umapi.gradle;

record UMAPIRuntimeTarget(
        String id,
        String loader,
        String minecraftVersion,
        String exportTaskName,
        String clientTaskName,
        String serverTaskName
) {
}
