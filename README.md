# UMAPI

UMAPI is the Universal Modding API: a Minecraft modding abstraction layer for SpilledSoup mods.

Its purpose is to let a mod target multiple Minecraft versions and loaders through a shared API, while UMAPI owns the loader- and version-specific platform code needed to connect that API to the game.

## Project Goals

- Provide a stable common API for SpilledSoup mods.
- Hide Minecraft loader details behind platform implementations.
- Support multiple Minecraft versions and loaders one platform at a time.
- Automate as much consumer Gradle setup as possible through the UMAPI Gradle plugin.
- Preserve known-good mod behavior while the abstraction grows incrementally.

UMAPI is not intended to be a general-purpose public modding framework. It is specifically designed for the development of SpilledSoup mods, and its functionality should expand only when a SpilledSoup mod actually needs that capability.

## Planned Support Matrix

UMAPI's target list should stay focused on major modding versions rather than every Minecraft release. The current planned support matrix is:

| Minecraft version | Planned loaders |
| --- | --- |
| 1.12.2 | Forge |
| 1.16.5 | Forge |
| 1.18.2 | Fabric, Forge |
| 1.19.2 | Fabric, Forge |
| 1.20.1 | Fabric, NeoForge, Forge, Quilt |
| 1.20.4 | Fabric, NeoForge, Forge, Quilt |
| 1.20.6 | Fabric, NeoForge, Forge |
| 1.21.1 | Fabric, NeoForge |
| 1.21.3 | Fabric, NeoForge |
| 1.21.5 | Fabric, NeoForge |
| 1.21.8 | Fabric, NeoForge |
| 1.21.10 | Fabric, NeoForge |
| 1.21.11 | Fabric, NeoForge |
| 26.1.2 | Fabric, NeoForge |
| 26.2 | Fabric, NeoForge |

Older Forge targets exist to cover established legacy modpack versions. Fabric and NeoForge are the preferred forward path for modern Minecraft versions. Quilt should stay optional and only be implemented where it is useful for a SpilledSoup mod.

## Repository Layout

```text
api/
gradle-plugin/
settings-plugin/
platforms/
```

`api` contains the shared UMAPI surface that mods should depend on.

`gradle-plugin` contains the `com.spilledsoup.umapi` Gradle plugin. The plugin is responsible for gradually moving loader- and version-specific build setup out of consuming mods.

`settings-plugin` contains the lightweight `com.spilledsoup.umapi.settings` Gradle settings plugin. It runs before project plugins and adds the repositories needed to resolve UMAPI's loader tooling.

`platforms` contains loader/version-specific implementations. For example, `platforms/fabric-1.20.1` and `platforms/fabric-1.20.4` adapt the shared API to Fabric, `platforms/neoforge-1.20.1` and `platforms/neoforge-1.20.4` adapt it to NeoForge, `platforms/forge-1.20.1` and `platforms/forge-1.20.4` adapt it to Forge, and `platforms/quilt-1.20.1` and `platforms/quilt-1.20.4` adapt it to Quilt. `platforms/shared` contains source-only shared platform logic that concrete platform modules explicitly opt into when the same logic is proven compatible.

## Current State

The first known-good path is Fabric for Minecraft 1.20.1. SampleMod has successfully launched through this path and displayed its welcome message in chat. UMAPI also has tested Fabric and NeoForge 1.20.1 targets, initial Forge and Quilt 1.20.1 targets, and initial Fabric, NeoForge, Forge, and Quilt 1.20.4 targets.

The Gradle plugin currently:

- applies Java and configures Java 17 for consuming mod code
- applies Fabric Loom, NeoGradle, ForgeGradle, or Quilt Loom for the selected declared target
- adds Minecraft, mappings, loader, and UMAPI platform dependencies for the selected target
- adds the shared UMAPI API as a compile-only dependency for consuming mod code
- generates loader metadata from neutral UMAPI mod metadata
- exports the finished mod jar to `build/umapi/exports`
- exposes UMAPI runtime tasks for launching declared target clients or servers
- exposes a target DSL shape:

```kotlin
umapi {
    mod {
        id = "samplemod"
        name = "Sample Mod"
        description = "UMAPI proof-of-concept sample mod"
        authors.add("SpilledSoup")
        entrypoint = "com.spilledsoup.samplemod.SampleMod"
    }

    targets {
        fabric("1.20.1")
        fabric("1.20.4")
        neoforge("1.20.1")
        neoforge("1.20.4")
        forge("1.20.1")
        forge("1.20.4")
        quilt("1.20.1")
        quilt("1.20.4")
    }
}
```

The DSL lets consuming mods declare neutral mod metadata and their intended targets. When multiple targets are declared, UMAPI configures each loader/version path separately through target-specific build and runtime tasks.

Consuming mods can optionally choose the default runtime used by `runUMAPIClient` and `runUMAPIServer`:

```kotlin
umapi {
    runtime {
        defaultTarget = "fabric-1.20.1"
    }
}
```

Or:

```kotlin
umapi {
    runtime {
        defaultLoader = "fabric"
        defaultMinecraftVersion = "1.20.1"
    }
}
```

If no runtime default is declared, UMAPI chooses the latest declared Minecraft version and then prefers Fabric, NeoForge, Forge, and Quilt in that order.

## Shared Logic Notes

UMAPI now keeps supported loader/version facts in `UMAPITargetCatalog`. New targets should start there so dependency versions, loader version ranges, Minecraft version ranges, generated package names, and resource pack formats do not get scattered through the Gradle plugin.

Fabric and Quilt are currently treated as Loom-family targets. Their shared setup lives in `UMAPILoomTargetSupport`, including Minecraft dependency setup, official Mojang mappings, UMAPI platform dependency setup, loader dependency setup, runtime task registration, run directory handling, Loom mod-source registration, and the current run-task configuration-cache workaround.

Forge and NeoForge are currently treated as Forge-family targets for generated metadata and generated Java entrypoint bridges. Their shared generated-resource and generated-source wiring lives in `UMAPIForgeFamilyTargetSupport`. Their dependency and runtime setup remain separate because ForgeGradle and NeoGradle behave differently.

The current 1.20.x platform implementations share the neutral `AbstractPlatform` base class. Shared Minecraft-bound implementation code lives under `platforms/shared` and is grouped by proven compatibility rather than by ideal loader families. The current source-only shared groups are:

- `platforms/shared/common` for loader-neutral platform implementation helpers such as SLF4J logging
- `platforms/shared/fabriclike-1.20.x` for Fabric and Quilt 1.20.x player join and player wrapper logic
- `platforms/shared/forge-1.20.x` for Forge 1.20.x player join and player wrapper logic

NeoForge event and player adapters remain per target for now because NeoForge 1.20.1 and NeoForge 1.20.4 currently use meaningfully different API shapes.

Current known shared assumptions:

- Java 17 is enough for the implemented 1.20.1 and 1.20.4 targets.
- Fabric and Quilt 1.20.x targets use Loom-style Minecraft, mappings, remap, and run task setup.
- Fabric and Quilt 1.20.x targets share the same Fabric-like player join event hook and player wrapper.
- Forge 1.20.1 and Forge 1.20.4 share the same Forge player login event hook and player wrapper.
- Forge and NeoForge 1.20.x targets can share generated `mods.toml`, `pack.mcmeta`, and generated `@Mod` bridge concepts.
- `Component.literal(...)` is still valid for the current 1.20.x player-message adapters.

## Class Responsibilities

### Shared API

`UMAPI` is the static entry point used by mods. It stores the active platform implementation, exposes shared services such as `events()`, and guards against use before the platform has initialised UMAPI.

`UMAPIMod` is the neutral lifecycle interface implemented by consuming mods. Platform code invokes `initialise()` after UMAPI is ready.

`Platform` is the abstraction implemented by each loader/version platform. It exposes the services that the shared API can delegate to.

`AbstractPlatform` is the small shared base for platform implementations that simply hold an `Events` implementation and a `Logger` implementation.

`Slf4jLogger` is the shared platform logging adapter. It maps UMAPI logging calls to the SLF4J logging environment supplied by the active loader.

`FabricLikeEvents` is the shared Fabric/Quilt 1.20.x event adapter. It maps Fabric API/QFAPI player join callbacks to `Events.onPlayerJoin`.

`FabricLikePlayer` is the shared Fabric/Quilt 1.20.x player adapter. It adapts Minecraft's `ServerPlayer` to the shared `Player` interface for the current Fabric-like 1.20.x targets.

`Forge120xEvents` is the shared Forge 1.20.x event adapter. It maps Forge's player login event to `Events.onPlayerJoin`.

`Forge120xPlayer` is the shared Forge 1.20.x player adapter. It adapts Minecraft's `ServerPlayer` to the shared `Player` interface for the current Forge 1.20.x targets.

`Events` is the current shared event surface. It lets mods register callbacks without depending on Fabric event classes.

`Player` is the current shared player abstraction. It exposes only the player behavior UMAPI currently needs: reading the player name and sending a chat message.

`Logger` is the shared logging abstraction. It lets mods write basic log messages without depending on a loader-specific logging API.

### Gradle Plugins

`UMAPISettingsPlugin` is the lightweight settings plugin from `settings-plugin`. It runs from `settings.gradle.kts`, adds repositories needed to resolve UMAPI's loader tooling before the main project plugin is loaded, and supplies isolated target tooling when a loader cannot safely live on the main UMAPI plugin classpath.

`UMAPISettingsTarget` is the settings-plugin registry for target-specific buildscript tooling that must be injected only for an active target, such as Fabric Loom or Quilt Loom.

`UMAPIPlugin` is the main project plugin from `gradle-plugin`. It applies Java, configures Java 17 for consuming mod code, adds the shared UMAPI API as a compile-only dependency, creates the `umapi {}` DSL, and performs final validation.

`UMAPIExtension` is the root Gradle DSL object behind `umapi {}`. It owns the neutral `mod {}` metadata block, the `targets {}` block, and optional runtime defaults.

`UMAPIModExtension` stores neutral mod metadata such as id, name, description, authors, and UMAPI entrypoint. It validates that required metadata exists before generated platform resources are used.

`UMAPITargetsExtension` stores target declarations from `targets {}`. It tracks supported loader/version targets, delegates single-target setup to the relevant target helper, and orchestrates target-specific build and runtime tasks when multiple targets are declared.

`UMAPIRuntimeExtension` stores optional default runtime choices. If no default is configured, it chooses the latest declared Minecraft version and then prefers Fabric, NeoForge, Forge, and Quilt in that order.

`UMAPISupportedTargets` validates declared loader/version combinations against the target catalog and routes each active target to the helper that configures it.

`UMAPITargetCatalog` is the central catalog of supported target facts. It stores loader versions, platform API versions, Minecraft version ranges, loader version ranges, generated package names, and resource pack formats for each supported loader/version pair.

`UMAPITargetConfigurator` is the small function interface used by `UMAPISupportedTargets` to call the correct loader/version configuration helper.

`UMAPITargetDefinition` is the shared definition for one supported loader/version pair. It derives the target id, platform artifact id, runtime directories, descriptor, and runtime target record from the loader and Minecraft version.

`UMAPITargetDescriptor` is the small shared model used once a target is being configured. It derives the export task name and runtime task names from the loader and Minecraft version.

`UMAPIRuntimeTarget` is the internal record UMAPI uses to describe a runnable target, including its loader, Minecraft version, and client/server task names.

`UMAPILoader` is the small internal registry of known loader ids, display names, task-name parts, runtime directory prefixes, and runtime priority.

`UMAPIMinecraftVersion` centralizes Minecraft version validation, task-name formatting, compact directory suffixes, and version comparison.

`UMAPIGradleNames` centralizes small Gradle naming helpers used by task and target naming code.

`UMAPIRuntimeTasks` centralizes UMAPI runtime task naming, descriptions, and wrapper task registration.

`UMAPIExportName` centralizes the readable exported jar filename format used by target implementations.

`UMAPIExportTasks` centralizes exported jar task registration and the shared `exportUMAPI` aggregate task.

`UMAPIGeneratedResources` centralizes wiring generated resource directories into the main resource set and `processResources` task.

`UMAPILoomTargetSupport` centralizes common Fabric/Quilt Loom target wiring such as Minecraft dependency setup, official Mojang mappings, local remap-cache invalidation, UMAPI platform dependency setup, and runtime task registration.

`UMAPILoomRunDirectories` centralizes Fabric/Quilt runtime directory configuration so Loom run configs and command-line run tasks use target-specific folders under `runs/`.

`UMAPIForgeFamilyTargetSupport` centralizes generated Forge-family metadata and generated Java entrypoint bridge setup for Forge and NeoForge targets.

`FabricTargets` centralizes the current Fabric build wiring. It uses the shared target catalog for supported Fabric/Minecraft target facts, then delegates common Loom setup, connects generated Fabric resources to the main resource set, exports the finished target jar, and registers Fabric runtime task wrappers.

`NeoForgeTargets` centralizes the current NeoForge build wiring. It uses the shared target catalog for supported NeoForge/Minecraft target facts, applies NeoGradle, adds NeoForge and UMAPI platform dependencies, delegates generated Forge-family metadata and source setup, exports the finished target jar, and registers NeoForge runtime task wrappers.

`ForgeTargets` centralizes the current Forge build wiring. It uses the shared target catalog for supported Forge/Minecraft target facts, applies ForgeGradle and Renamer Gradle, adds Forge and UMAPI platform dependencies, delegates generated Forge-family metadata and source setup, exports the reobfuscated target jar, and registers Forge runtime task wrappers.

`QuiltTargets` centralizes the current Quilt build wiring. It uses the shared target catalog for supported Quilt/Minecraft target facts, delegates common Loom setup, adds QFAPI, connects generated Quilt resources to the main resource set, exports the remapped target jar, and registers Quilt runtime task wrappers.

`GenerateFabricModJsonTask` is the typed Gradle task that writes `fabric.mod.json` from neutral UMAPI mod metadata plus Fabric target details.

`GenerateQuiltModJsonTask` is the typed Gradle task that writes `quilt.mod.json` from neutral UMAPI mod metadata plus Quilt target details.

`GenerateForgeFamilyModsTomlTask` is the typed Gradle task that writes `META-INF/mods.toml` and `pack.mcmeta` from neutral UMAPI mod metadata plus Forge-family target details.

`GenerateForgeFamilyEntrypointTask` is the typed Gradle task that writes a small generated `@Mod` bridge for consuming mods, so SampleMod can keep using `UMAPIMod` instead of a Forge-family Java entrypoint.

### Fabric 1.20.1 Platform

`Fabric1201Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.20.1 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric1201Platform` is the Fabric implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like 1.20.x event/player adapters and shared SLF4J logger.

### Fabric 1.20.4 Platform

`Fabric1204Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.20.4 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric1204Platform` is the Fabric 1.20.4 implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like 1.20.x event/player adapters and shared SLF4J logger.

### NeoForge 1.20.1 Platform

`NeoForge1201Entrypoint` is the NeoForge loader entrypoint. It initialises UMAPI with the NeoForge 1.20.1 platform.

`NeoForge1201Platform` is the NeoForge implementation of `Platform`. It wires shared UMAPI services to NeoForge-backed event/player implementations and the shared SLF4J logger.

`NeoForgeEvents` adapts NeoForge event callbacks to the shared `Events` interface. It currently maps NeoForge's player login event to `Events.onPlayerJoin`.

`NeoForgePlayer` adapts Minecraft's `ServerPlayer` to the shared `Player` interface for the NeoForge 1.20.1 mapping layer.

### NeoForge 1.20.4 Platform

`NeoForge1204Entrypoint` is the NeoForge loader entrypoint. It initialises UMAPI with the NeoForge 1.20.4 platform.

`NeoForge1204Platform` is the NeoForge 1.20.4 implementation of `Platform`. It wires shared UMAPI services to NeoForge-backed event/player implementations and the shared SLF4J logger.

`NeoForgeEvents` adapts NeoForge event callbacks to the shared `Events` interface. It currently maps NeoForge's player login event to `Events.onPlayerJoin`.

`NeoForgePlayer` adapts Minecraft's `ServerPlayer` to the shared `Player` interface for the NeoForge 1.20.4 mapping layer.

### Forge 1.20.1 Platform

`Forge1201Entrypoint` is the Forge loader entrypoint. It initialises UMAPI with the Forge 1.20.1 platform.

`Forge1201Platform` is the Forge implementation of `Platform`. It wires shared UMAPI services to the shared Forge 1.20.x event/player adapters and shared SLF4J logger.

### Forge 1.20.4 Platform

`Forge1204Entrypoint` is the Forge loader entrypoint. It initialises UMAPI with the Forge 1.20.4 platform.

`Forge1204Platform` is the Forge 1.20.4 implementation of `Platform`. It wires shared UMAPI services to the shared Forge 1.20.x event/player adapters and shared SLF4J logger.

### Quilt 1.20.1 Platform

`Quilt1201Entrypoint` is the Quilt loader entrypoint. It initialises UMAPI with the Quilt 1.20.1 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Quilt1201Platform` is the Quilt implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like 1.20.x event/player adapters and shared SLF4J logger.

### Quilt 1.20.4 Platform

`Quilt1204Entrypoint` is the Quilt loader entrypoint. It initialises UMAPI with the Quilt 1.20.4 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Quilt1204Platform` is the Quilt 1.20.4 implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like 1.20.x event/player adapters and shared SLF4J logger.

## Development Approach

UMAPI should be developed conservatively:

- keep the Fabric 1.20.1 path working
- make one small change at a time
- build UMAPI and SampleMod after each meaningful step
- avoid broad refactors while platform ownership is still moving
- avoid hardcoded duplicate version strings where the build already has a single source of truth

For the repeatable process of adding loader/version support, see [Adding Supported Loaders and Targets](docs/ADDING_SUPPORTED_LOADERS.md).

## Building

From the repository root:

```powershell
.\gradlew.bat clean build
```

SampleMod should also be built after UMAPI changes, because it exercises the plugin and the current platform path as a consuming mod.

Consuming mods that use the UMAPI plugin receive explicit export tasks. `build` should stay a normal verification command; it should not export every declared loader/version target by default.

Use `exportUMAPI` when every declared target jar is needed:

```powershell
.\gradlew.bat exportUMAPI
```

Use a target-specific export task when only one jar is needed:

```powershell
.\gradlew.bat exportUMAPIFabric1201
.\gradlew.bat exportUMAPIFabric1204
.\gradlew.bat exportUMAPINeoForge1201
.\gradlew.bat exportUMAPINeoForge1204
.\gradlew.bat exportUMAPIForge1201
.\gradlew.bat exportUMAPIForge1204
.\gradlew.bat exportUMAPIQuilt1201
.\gradlew.bat exportUMAPIQuilt1204
```

Exported jars are copied to `build/umapi/exports` with readable names containing the mod name, mod version, loader, and Minecraft version.

Consuming mods also receive UMAPI runtime tasks:

```powershell
.\gradlew.bat runUMAPIFabric1201Client
.\gradlew.bat runUMAPIFabric1201Server
.\gradlew.bat runUMAPIFabric1204Client
.\gradlew.bat runUMAPIFabric1204Server
.\gradlew.bat runUMAPINeoForge1201Client
.\gradlew.bat runUMAPINeoForge1201Server
.\gradlew.bat runUMAPINeoForge1204Client
.\gradlew.bat runUMAPINeoForge1204Server
.\gradlew.bat runUMAPIForge1201Client
.\gradlew.bat runUMAPIForge1201Server
.\gradlew.bat runUMAPIForge1204Client
.\gradlew.bat runUMAPIForge1204Server
.\gradlew.bat runUMAPIQuilt1201Client
.\gradlew.bat runUMAPIQuilt1201Server
.\gradlew.bat runUMAPIQuilt1204Client
.\gradlew.bat runUMAPIQuilt1204Server
.\gradlew.bat runUMAPIClient
.\gradlew.bat runUMAPIServer
```

The loader-specific tasks delegate to the selected loader tooling's native run tasks. The neutral `runUMAPIClient` and `runUMAPIServer` tasks use the configured or automatically selected default runtime.

UMAPI keeps loader runtime data in target-specific directories under `runs/`, such as `runs/fabric1201Client`, `runs/fabric1204Client`, `runs/neoForge1201Client`, `runs/neoForge1204Client`, `runs/forge1201Client`, `runs/forge1204Client`, `runs/quilt1201Client`, and `runs/quilt1204Client`.
