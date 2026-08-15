package com.spilledsoup.umapi.gradle;

import java.util.List;
import java.util.stream.Stream;

final class UMAPITargetCatalog {
    static final String FABRIC_LOOM_PLUGIN = "net.fabricmc.fabric-loom";
    static final String FABRIC_LOOM_REMAP_PLUGIN = "net.fabricmc.fabric-loom-remap";
    static final String QUILT_LOOM_PLUGIN = "org.quiltmc.loom";
    static final int JAVA_17_LANGUAGE_VERSION = 17;
    static final int JAVA_21_LANGUAGE_VERSION = 21;
    static final int JAVA_25_LANGUAGE_VERSION = 25;
    static final String JAVA_17_DEPENDENCY = ">=17";
    static final String JAVA_21_DEPENDENCY = ">=21";
    static final String JAVA_25_DEPENDENCY = ">=25";
    static final String UMAPI_ANY_DEPENDENCY = "*";
    static final String UMAPI_FORGE_FAMILY_VERSION_RANGE = "[0,)";

    private static final List<FabricTarget> FABRIC_TARGETS = List.of(
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "1.18.2"),
                    "0.19.3",
                    ">=0.14.6",
                    "0.77.0+1.18.2",
                    JAVA_17_LANGUAGE_VERSION,
                    JAVA_17_DEPENDENCY
            ),
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "1.19.2"),
                    "0.19.3",
                    ">=0.14.6",
                    "0.77.0+1.19.2",
                    JAVA_17_LANGUAGE_VERSION,
                    JAVA_17_DEPENDENCY
            ),
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "1.20.1"),
                    "0.19.3",
                    ">=0.15.0",
                    "0.92.11+1.20.1",
                    JAVA_17_LANGUAGE_VERSION,
                    JAVA_17_DEPENDENCY
            ),
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "1.20.4"),
                    "0.19.3",
                    ">=0.15.0",
                    "0.97.3+1.20.4",
                    JAVA_17_LANGUAGE_VERSION,
                    JAVA_17_DEPENDENCY
            ),
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "1.20.6"),
                    "0.19.3",
                    ">=0.15.0",
                    "0.100.8+1.20.6",
                    JAVA_21_LANGUAGE_VERSION,
                    JAVA_21_DEPENDENCY
            ),
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "1.21.1"),
                    "0.19.3",
                    ">=0.15.0",
                    "0.116.15+1.21.1",
                    JAVA_21_LANGUAGE_VERSION,
                    JAVA_21_DEPENDENCY
            ),
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "1.21.3"),
                    "0.19.3",
                    ">=0.15.0",
                    "0.114.1+1.21.3",
                    JAVA_21_LANGUAGE_VERSION,
                    JAVA_21_DEPENDENCY
            ),
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "1.21.5"),
                    "0.19.3",
                    ">=0.15.0",
                    "0.128.2+1.21.5",
                    JAVA_21_LANGUAGE_VERSION,
                    JAVA_21_DEPENDENCY
            ),
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "1.21.8"),
                    "0.19.3",
                    ">=0.15.0",
                    "0.136.1+1.21.8",
                    JAVA_21_LANGUAGE_VERSION,
                    JAVA_21_DEPENDENCY
            ),
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "1.21.10"),
                    "0.19.3",
                    ">=0.15.0",
                    "0.138.4+1.21.10",
                    JAVA_21_LANGUAGE_VERSION,
                    JAVA_21_DEPENDENCY
            ),
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "1.21.11"),
                    "0.19.3",
                    ">=0.15.0",
                    "0.141.6+1.21.11",
                    JAVA_21_LANGUAGE_VERSION,
                    JAVA_21_DEPENDENCY
            ),
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "26.1.2"),
                    "0.19.3",
                    ">=0.19.3",
                    "0.155.2+26.1.2",
                    JAVA_25_LANGUAGE_VERSION,
                    JAVA_25_DEPENDENCY,
                    FABRIC_LOOM_PLUGIN,
                    "implementation",
                    "jar",
                    false
            ),
            new FabricTarget(
                    target(UMAPILoader.FABRIC, "26.2"),
                    "0.19.3",
                    ">=0.19.3",
                    "0.156.0+26.2",
                    JAVA_25_LANGUAGE_VERSION,
                    JAVA_25_DEPENDENCY,
                    FABRIC_LOOM_PLUGIN,
                    "implementation",
                    "jar",
                    false
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
                    "mods.toml",
                    "com.spilledsoup.umapi.generated.neoforge1201",
                    "net.minecraftforge",
                    "",
                    JAVA_17_LANGUAGE_VERSION,
                    NeoForgeBuildPlugin.NEOGRADLE_USERDEV
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
                    "mods.toml",
                    "com.spilledsoup.umapi.generated.neoforge1204",
                    "net.neoforged",
                    "net.neoforged.bus.api.IEventBus",
                    JAVA_17_LANGUAGE_VERSION,
                    NeoForgeBuildPlugin.NEOGRADLE_USERDEV
            ),
            new NeoForgeTarget(
                    target(UMAPILoader.NEOFORGE, "1.20.6"),
                    "net.neoforged:neoforge:20.6.139",
                    "[1.20.6,1.21)",
                    "neoforge",
                    "[20.6.139,20.7)",
                    "[1,)",
                    true,
                    32,
                    "neoforge.mods.toml",
                    "com.spilledsoup.umapi.generated.neoforge1206",
                    "net.neoforged",
                    "net.neoforged.bus.api.IEventBus",
                    JAVA_21_LANGUAGE_VERSION,
                    NeoForgeBuildPlugin.NEOGRADLE_USERDEV
            ),
            new NeoForgeTarget(
                    target(UMAPILoader.NEOFORGE, "1.21.1"),
                    "net.neoforged:neoforge:21.1.244",
                    "[1.21.1]",
                    "neoforge",
                    "[21.1.244,21.2)",
                    "[1,)",
                    true,
                    34,
                    "neoforge.mods.toml",
                    "com.spilledsoup.umapi.generated.neoforge1211",
                    "net.neoforged",
                    "net.neoforged.bus.api.IEventBus",
                    JAVA_21_LANGUAGE_VERSION,
                    NeoForgeBuildPlugin.MODDEV_GRADLE
            ),
            new NeoForgeTarget(
                    target(UMAPILoader.NEOFORGE, "1.21.3"),
                    "net.neoforged:neoforge:21.3.96",
                    "[1.21.3]",
                    "neoforge",
                    "[21.3.96,21.4)",
                    "[1,)",
                    true,
                    42,
                    "neoforge.mods.toml",
                    "com.spilledsoup.umapi.generated.neoforge1213",
                    "net.neoforged",
                    "net.neoforged.bus.api.IEventBus",
                    JAVA_21_LANGUAGE_VERSION,
                    NeoForgeBuildPlugin.MODDEV_GRADLE
            ),
            new NeoForgeTarget(
                    target(UMAPILoader.NEOFORGE, "1.21.5"),
                    "net.neoforged:neoforge:21.5.98",
                    "[1.21.5]",
                    "neoforge",
                    "[21.5.98,21.6)",
                    "[1,)",
                    true,
                    55,
                    "neoforge.mods.toml",
                    "com.spilledsoup.umapi.generated.neoforge1215",
                    "net.neoforged",
                    "net.neoforged.bus.api.IEventBus",
                    JAVA_21_LANGUAGE_VERSION,
                    NeoForgeBuildPlugin.MODDEV_GRADLE
            ),
            new NeoForgeTarget(
                    target(UMAPILoader.NEOFORGE, "1.21.8"),
                    "net.neoforged:neoforge:21.8.54",
                    "[1.21.8]",
                    "neoforge",
                    "[21.8.54,21.9)",
                    "[1,)",
                    true,
                    64,
                    "neoforge.mods.toml",
                    "com.spilledsoup.umapi.generated.neoforge1218",
                    "net.neoforged",
                    "net.neoforged.bus.api.IEventBus",
                    JAVA_21_LANGUAGE_VERSION,
                    NeoForgeBuildPlugin.MODDEV_GRADLE
            ),
            new NeoForgeTarget(
                    target(UMAPILoader.NEOFORGE, "1.21.10"),
                    "net.neoforged:neoforge:21.10.64",
                    "[1.21.10]",
                    "neoforge",
                    "[21.10.64,21.11)",
                    "[1,)",
                    true,
                    69,
                    "neoforge.mods.toml",
                    "com.spilledsoup.umapi.generated.neoforge12110",
                    "net.neoforged",
                    "net.neoforged.bus.api.IEventBus",
                    JAVA_21_LANGUAGE_VERSION,
                    NeoForgeBuildPlugin.MODDEV_GRADLE
            ),
            new NeoForgeTarget(
                    target(UMAPILoader.NEOFORGE, "1.21.11"),
                    "net.neoforged:neoforge:21.11.45",
                    "[1.21.11]",
                    "neoforge",
                    "[21.11.45,21.12)",
                    "[1,)",
                    true,
                    75,
                    "neoforge.mods.toml",
                    "com.spilledsoup.umapi.generated.neoforge12111",
                    "net.neoforged",
                    "net.neoforged.bus.api.IEventBus",
                    JAVA_21_LANGUAGE_VERSION,
                    NeoForgeBuildPlugin.MODDEV_GRADLE
            ),
            new NeoForgeTarget(
                    target(UMAPILoader.NEOFORGE, "26.1.2"),
                    "net.neoforged:neoforge:26.1.2.95",
                    "[26.1.2]",
                    "neoforge",
                    "[26.1.2.95,26.1.3)",
                    "[1,)",
                    true,
                    84,
                    "neoforge.mods.toml",
                    "com.spilledsoup.umapi.generated.neoforge2612",
                    "net.neoforged",
                    "net.neoforged.bus.api.IEventBus",
                    JAVA_25_LANGUAGE_VERSION,
                    NeoForgeBuildPlugin.MODDEV_GRADLE
            ),
            new NeoForgeTarget(
                    target(UMAPILoader.NEOFORGE, "26.2"),
                    "net.neoforged:neoforge:26.2.0.59",
                    "[26.2]",
                    "neoforge",
                    "[26.2.0.59,26.3)",
                    "[1,)",
                    true,
                    88,
                    "neoforge.mods.toml",
                    "com.spilledsoup.umapi.generated.neoforge262",
                    "net.neoforged",
                    "net.neoforged.bus.api.IEventBus",
                    JAVA_25_LANGUAGE_VERSION,
                    NeoForgeBuildPlugin.MODDEV_GRADLE
            )
    );
    private static final List<ForgeTarget> FORGE_TARGETS = List.of(
            new ForgeTarget(
                    target(UMAPILoader.FORGE, "1.16.5"),
                    "1.16.5-36.2.42",
                    "[1.16.5,1.17)",
                    "[36.2.42,)",
                    "[36,)",
                    6,
                    "com.spilledsoup.umapi.generated.forge1165",
                    JAVA_17_LANGUAGE_VERSION
            ),
            new ForgeTarget(
                    target(UMAPILoader.FORGE, "1.18.2"),
                    "1.18.2-40.3.12",
                    "[1.18.2,1.19)",
                    "[40.3.12,)",
                    "[40,)",
                    8,
                    "com.spilledsoup.umapi.generated.forge1182",
                    JAVA_17_LANGUAGE_VERSION
            ),
            new ForgeTarget(
                    target(UMAPILoader.FORGE, "1.19.2"),
                    "1.19.2-43.5.2",
                    "[1.19.2,1.20)",
                    "[43.5.2,)",
                    "[43,)",
                    9,
                    "com.spilledsoup.umapi.generated.forge1192",
                    JAVA_17_LANGUAGE_VERSION
            ),
            new ForgeTarget(
                    target(UMAPILoader.FORGE, "1.20.1"),
                    "1.20.1-47.4.10",
                    "[1.20.1,1.21)",
                    "[47.4.10,)",
                    "[47,)",
                    15,
                    "com.spilledsoup.umapi.generated.forge1201",
                    JAVA_17_LANGUAGE_VERSION
            ),
            new ForgeTarget(
                    target(UMAPILoader.FORGE, "1.20.4"),
                    "1.20.4-49.2.8",
                    "[1.20.4,1.20.5)",
                    "[49.2.8,)",
                    "[49,)",
                    22,
                    "com.spilledsoup.umapi.generated.forge1204",
                    JAVA_17_LANGUAGE_VERSION
            ),
            new ForgeTarget(
                    target(UMAPILoader.FORGE, "1.20.6"),
                    "1.20.6-50.2.10",
                    "[1.20.6,1.21)",
                    "[50.2.10,)",
                    "[50,)",
                    32,
                    "com.spilledsoup.umapi.generated.forge1206",
                    JAVA_21_LANGUAGE_VERSION
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
            String fabricApiVersion,
            int javaLanguageVersion,
            String javaDependency,
            String loomPluginId,
            String dependencyConfiguration,
            String exportJarTaskName,
            boolean useExplicitOfficialMojangMappings
    ) implements CatalogTarget {
        FabricTarget(
                UMAPITargetDefinition target,
                String fabricLoaderVersion,
                String fabricLoaderDependency,
                String fabricApiVersion,
                int javaLanguageVersion,
                String javaDependency
        ) {
            this(
                    target,
                    fabricLoaderVersion,
                    fabricLoaderDependency,
                    fabricApiVersion,
                    javaLanguageVersion,
                    javaDependency,
                    FABRIC_LOOM_REMAP_PLUGIN,
                    "modImplementation",
                    "remapJar",
                    true
            );
        }

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
            String metadataFileName,
            String generatedEntrypointPackage,
            String fmlPackageRoot,
            String modEventBusParameterType,
            int javaLanguageVersion,
            NeoForgeBuildPlugin buildPlugin
    ) implements ForgeFamilyTarget {
        @Override
        public String forgeVersionRange() {
            return neoForgeVersionRange;
        }

        String neoForgeVersion() {
            return neoForgeDependency.substring(neoForgeDependency.lastIndexOf(':') + 1);
        }
    }

    record ForgeTarget(
            UMAPITargetDefinition target,
            String forgeVersion,
            String minecraftVersionRange,
            String forgeVersionRange,
            String loaderVersionRange,
            int resourcePackFormat,
            String generatedEntrypointPackage,
            int javaLanguageVersion
    ) implements ForgeFamilyTarget {
        private static final String LOADER_DEPENDENCY_MOD_ID = "forge";
        private static final String FML_PACKAGE_ROOT = "net.minecraftforge";
        private static final String METADATA_FILE_NAME = "mods.toml";

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

        @Override
        public String metadataFileName() {
            return METADATA_FILE_NAME;
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

        default Integer resourcePackMinFormat() {
            if (resourcePackFormat() >= 69) {
                return resourcePackFormat();
            }

            return null;
        }

        default Integer resourcePackMaxFormat() {
            if (resourcePackFormat() >= 69) {
                return resourcePackFormat();
            }

            return null;
        }

        String metadataFileName();

        String generatedEntrypointPackage();

        String fmlPackageRoot();

        String modEventBusParameterType();

        int javaLanguageVersion();
    }

    enum NeoForgeBuildPlugin {
        NEOGRADLE_USERDEV,
        MODDEV_GRADLE
    }
}
