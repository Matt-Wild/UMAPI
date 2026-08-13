package com.spilledsoup.umapi.gradle;

import org.gradle.api.Project;

import java.util.List;
import java.util.stream.Collectors;

final class UMAPISupportedTargets {
    private static final List<SupportedTarget> TARGETS = UMAPITargetCatalog.definitions()
            .stream()
            .map(UMAPISupportedTargets::target)
            .toList();

    private UMAPISupportedTargets() {
    }

    static UMAPITargetDefinition require(
            UMAPILoader loader,
            String minecraftVersion
    ) {
        return requireSupported(loader.id(), minecraftVersion).definition();
    }

    static UMAPIRuntimeTarget configure(
            Project project,
            String umapiVersion,
            UMAPIModExtension mod,
            UMAPIRuntimeTarget runtimeTarget
    ) {
        var target = requireSupported(
                runtimeTarget.loader(),
                runtimeTarget.minecraftVersion()
        );

        return target.configurator().configure(
                project,
                umapiVersion,
                mod,
                runtimeTarget.minecraftVersion()
        );
    }

    private static SupportedTarget requireSupported(
            String loaderId,
            String minecraftVersion
    ) {
        return TARGETS.stream()
                .filter(target -> target.definition().loaderId().equals(loaderId))
                .filter(target -> target.definition().minecraftVersion().equals(minecraftVersion))
                .findFirst()
                .orElseThrow(() -> {
                    var loader = UMAPILoader.find(loaderId).orElse(null);

                    if (loader == null) {
                        return new IllegalStateException(
                                "UMAPI currently only supports " + declarations() + "."
                        );
                    }

                    return new IllegalStateException(
                            "UMAPI currently only supports " + declaration(loader) + "."
                    );
                });
    }

    static String declaration(UMAPILoader loader) {
        var declarations = targets(loader)
                .stream()
                .map(target -> target.definition().declaration())
                .toList();

        if (declarations.isEmpty()) {
            return loader.id() + "(\"<minecraftVersion>\")";
        }

        return String.join(" or ", declarations);
    }

    static String declarations() {
        return TARGETS.stream()
                .map(target -> target.definition().declaration())
                .collect(Collectors.joining(", "));
    }

    private static List<SupportedTarget> targets(UMAPILoader loader) {
        return TARGETS.stream()
                .filter(target -> target.definition().loader() == loader)
                .toList();
    }

    private static SupportedTarget target(UMAPITargetDefinition definition) {
        return new SupportedTarget(
                definition,
                configurator(definition.loader())
        );
    }

    private static UMAPITargetConfigurator configurator(UMAPILoader loader) {
        return switch (loader) {
            case FABRIC -> FabricTargets::configure;
            case NEOFORGE -> NeoForgeTargets::configure;
            case FORGE -> ForgeTargets::configure;
            case QUILT -> QuiltTargets::configure;
        };
    }

    private record SupportedTarget(
            UMAPITargetDefinition definition,
            UMAPITargetConfigurator configurator
    ) {
    }
}
