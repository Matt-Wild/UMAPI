package com.spilledsoup.umapi.gradle;

import java.util.List;
import java.util.stream.Stream;

final class UMAPITargetCatalog {
    static final String FABRIC_LOOM_PLUGIN = "net.fabricmc.fabric-loom-remap";
    static final String QUILT_LOOM_PLUGIN = "org.quiltmc.loom";
    static final String JAVA_17_DEPENDENCY = ">=17";
    static final String UMAPI_ANY_DEPENDENCY = "*";
    static final String UMAPI_FORGE_FAMILY_VERSION_RANGE = "[0,)";

    private static final List<FabricTarget> FABRIC_TARGETS = List.of(
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "1.20.1"),
                    "0.19.3",
                    ">=0.15.0",
                    "0.92.11+1.20.1"
            ),
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "1.20.4"),
                    "0.19.3",
                    ">=0.15.0",
                    "0.97.3+1.20.4"
            )
    );
    private static final List<NeoForgeTarget> NEOFORGE_TARGETS = List.of(
            new NeoForgeTarget(
                    target(UMAPILoader.NEOFORGE, "1.20.1"),
                    "net.neoforged:forge:1.20.1-47.1.106",
                    "[1.20.1,1.21)",
                    "forge",
                    "[47.1.106,)",
                    "[47,)",
                    false,
                    15,
                    "com.spilledsoup.umapi.generated.neoforge1201",
                    "net.minecraftforge",
                    ""
            ),
            new NeoForgeTarget(
                    target(UMAPILoader.NEOFORGE, "1.20.4"),
                    "net.neoforged:neoforge:20.4.251",
                    "[1.20.4,1.20.5)",
                    "neoforge",
                    "[20.4.251,)",
                    "[1,)",
                    true,
                    22,
                    "com.spilledsoup.umapi.generated.neoforge1204",
                    "net.neoforged",
                    "net.neoforged.bus.api.IEventBus"
            )
    );
    private static final List<ForgeTarget> FORGE_TARGETS = List.of(
            new ForgeTarget(
                    target(UMAPILoader.FORGE, "1.20.1"),
                    "1.20.1-47.4.10",
                    "[1.20.1,1.21)",
                    "[47.4.10,)",
                    "[47,)",
                    15,
                    "com.spilledsoup.umapi.generated.forge1201"
            ),
            new ForgeTarget(
                    target(UMAPILoader.FORGE, "1.20.4"),
                    "1.20.4-49.2.8",
                    "[1.20.4,1.20.5)",
                    "[49.2.8,)",
                    "[49,)",
                    22,
                    "com.spilledsoup.umapi.generated.forge1204"
            )
    );
    private static final List<QuiltTarget> QUILT_TARGETS = List.of(
            new QuiltTarget(
                    target(UMAPILoader.QUILT, "1.20.1"),
                    "0.29.2",
                    ">=0.26.0",
                    "7.7.0+0.92.2-1.20.1"
            ),
            new QuiltTarget(
                    target(UMAPILoader.QUILT, "1.20.4"),
                    "0.29.2",
                    ">=0.26.0",
                    "9.0.0-alpha.8+0.97.0-1.20.4"
            )
    );

    private UMAPITargetCatalog() {
    }

    static List<UMAPITargetDefinition> definitions() {
        return Stream.of(
                        FABRIC_TARGETS.stream().map(FabricTarget::target),
                        NEOFORGE_TARGETS.stream().map(NeoForgeTarget::target),
                        FORGE_TARGETS.stream().map(ForgeTarget::target),
                        QUILT_TARGETS.stream().map(QuiltTarget::target)
                )
                .flatMap(stream -> stream)
                .toList();
    }

    static FabricTarget fabric(String minecraftVersion) {
        return require(FABRIC_TARGETS, UMAPILoader.FABRIC, minecraftVersion);
    }

    static NeoForgeTarget neoForge(String minecraftVersion) {
        return require(NEOFORGE_TARGETS, UMAPILoader.NEOFORGE, minecraftVersion);
    }

    static ForgeTarget forge(String minecraftVersion) {
        return require(FORGE_TARGETS, UMAPILoader.FORGE, minecraftVersion);
    }

    static QuiltTarget quilt(String minecraftVersion) {
        return require(QUILT_TARGETS, UMAPILoader.QUILT, minecraftVersion);
    }

    private static UMAPITargetDefinition target(UMAPILoader loader, String minecraftVersion) {
        return new UMAPITargetDefinition(loader, minecraftVersion);
    }

    private static <T extends CatalogTarget> T require(
            List<T> targets,
            UMAPILoader loader,
            String minecraftVersion
    ) {
        return targets.stream()
                .filter(target -> target.target().minecraftVersion().equals(minecraftVersion))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "UMAPI currently only supports " + declaration(loader) + "."
                ));
    }

    private static String declaration(UMAPILoader loader) {
        var declarations = definitions()
                .stream()
                .filter(target -> target.loader() == loader)
                .map(UMAPITargetDefinition::declaration)
                .toList();

        if (declarations.isEmpty()) {
            return loader.id() + "(\"<minecraftVersion>\")";
        }

        return String.join(" or ", declarations);
    }

    interface CatalogTarget {
        UMAPITargetDefinition target();
    }

    record FabricTarget(
            UMAPITargetDefinition target,
            String fabricLoaderVersion,
            String fabricLoaderDependency,
            String fabricApiVersion
    ) implements CatalogTarget {
        String fabricLoaderDependencyNotation() {
            return "net.fabricmc:fabric-loader:" + fabricLoaderVersion;
        }
    }

    record NeoForgeTarget(
            UMAPITargetDefinition target,
            String neoForgeDependency,
            String minecraftVersionRange,
            String loaderDependencyModId,
            String neoForgeVersionRange,
            String loaderVersionRange,
            boolean useModernDependencyType,
            int resourcePackFormat,
            String generatedEntrypointPackage,
            String fmlPackageRoot,
            String modEventBusParameterType
    ) implements ForgeFamilyTarget {
        @Override
        public String forgeVersionRange() {
            return neoForgeVersionRange;
        }
    }

    record ForgeTarget(
            UMAPITargetDefinition target,
            String forgeVersion,
            String minecraftVersionRange,
            String forgeVersionRange,
            String loaderVersionRange,
            int resourcePackFormat,
            String generatedEntrypointPackage
    ) implements ForgeFamilyTarget {
        private static final String LOADER_DEPENDENCY_MOD_ID = "forge";
        private static final String FML_PACKAGE_ROOT = "net.minecraftforge";

        String forgeDependencyNotation() {
            return "net.minecraftforge:forge:" + forgeVersion;
        }

        @Override
        public String loaderDependencyModId() {
            return LOADER_DEPENDENCY_MOD_ID;
        }

        @Override
        public boolean useModernDependencyType() {
            return false;
        }

        @Override
        public String fmlPackageRoot() {
            return FML_PACKAGE_ROOT;
        }

        @Override
        public String modEventBusParameterType() {
            return "";
        }
    }

    record QuiltTarget(
            UMAPITargetDefinition target,
            String quiltLoaderVersion,
            String quiltLoaderDependency,
            String quiltedFabricApiVersion
    ) implements CatalogTarget {
        String quiltLoaderDependencyNotation() {
            return "org.quiltmc:quilt-loader:" + quiltLoaderVersion;
        }

        String quiltedFabricApiDependencyNotation() {
            return "org.quiltmc.quilted-fabric-api:quilted-fabric-api:" + quiltedFabricApiVersion;
        }
    }

    interface ForgeFamilyTarget extends CatalogTarget {
        String minecraftVersionRange();

        String loaderDependencyModId();

        String forgeVersionRange();

        String loaderVersionRange();

        boolean useModernDependencyType();

        int resourcePackFormat();

        String generatedEntrypointPackage();

        String fmlPackageRoot();

        String modEventBusParameterType();
    }
}
