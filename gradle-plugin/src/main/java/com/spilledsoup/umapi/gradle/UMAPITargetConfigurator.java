package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;

@FunctionalInterface
interface UMAPITargetConfigurator {
    UMAPIRuntimeTarget configure(
            Project project,
            String umapiVersion,
            UMAPIModExtension mod,
            String minecraftVersion
    );
}
