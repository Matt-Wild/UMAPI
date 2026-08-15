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

Older Forge targets exist to cover established legacy modpack versions, but targets that force Java 8-only shared mod code are intentionally out of scope. Fabric and NeoForge are the preferred forward path for modern Minecraft versions. Quilt should stay optional and only be implemented where it is useful for a SpilledSoup mod.

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

`platforms` contains loader/version-specific implementations. For example, Fabric platforms cover 1.18.2 through 26.2, NeoForge platforms cover 1.20.1 through 26.2, Forge platforms currently cover 1.16.5 through 1.20.6, and Quilt platforms currently cover 1.20.1 and 1.20.4. `platforms/shared` contains source-only shared platform logic that concrete platform modules explicitly opt into when the same logic is proven compatible.

## Current State

The first known-good path is Fabric for Minecraft 1.20.1. SampleMod has successfully launched through this path and displayed its welcome message in chat. UMAPI also has tested Fabric and NeoForge 1.20.1 targets, initial Forge and Quilt 1.20.1 targets, Fabric, NeoForge, Forge, and Quilt 1.20.4 targets, Fabric, NeoForge, and Forge 1.20.6 targets, and Fabric and NeoForge 1.21.1 targets. Forge 1.16.5, Fabric and Forge 1.18.2 and 1.19.2, plus Fabric and NeoForge 1.21.3, 1.21.5, 1.21.8, 1.21.10, 1.21.11, 26.1.2, and 26.2 have initial target implementations ready for testing.

The Gradle plugin currently:

- applies Java and configures the target-specific Java toolchain for consuming mod code
- applies Fabric Loom, NeoGradle/ModDevGradle, ForgeGradle, or Quilt Loom for the selected declared target
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
        fabric("1.18.2")
        fabric("1.19.2")
        fabric("1.20.1")
        fabric("1.20.4")
        fabric("1.20.6")
        fabric("1.21.1")
        fabric("1.21.3")
        fabric("1.21.5")
        fabric("1.21.8")
        fabric("1.21.10")
        fabric("1.21.11")
        fabric("26.1.2")
        fabric("26.2")
        neoforge("1.20.1")
        neoforge("1.20.4")
        neoforge("1.20.6")
        neoforge("1.21.1")
        neoforge("1.21.3")
        neoforge("1.21.5")
        neoforge("1.21.8")
        neoforge("1.21.10")
        neoforge("1.21.11")
        neoforge("26.1.2")
        neoforge("26.2")
        forge("1.16.5")
        forge("1.18.2")
        forge("1.19.2")
        forge("1.20.1")
        forge("1.20.4")
        forge("1.20.6")
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

Fabric and Quilt are currently treated as Loom-family targets. Their shared setup lives in `UMAPILoomTargetSupport`, including Minecraft dependency setup, mapping setup where required, UMAPI platform dependency setup, loader dependency setup, runtime task registration, run directory handling, Loom mod-source registration, and the current run-task configuration-cache workaround.

Forge and NeoForge are currently treated as Forge-family targets for generated metadata and generated Java entrypoint bridges. Their shared generated-resource and generated-source wiring lives in `UMAPIForgeFamilyTargetSupport`. Their dependency and runtime setup remain separate because ForgeGradle and NeoGradle behave differently.

ForgeGradle userdev run discovery expects generated Forge metadata in the standard `build/resources/main` output. Forge 1.16.5 is especially strict here: if `META-INF/mods.toml` only exists in the compiled classes output, UMAPI can load while the consuming mod is skipped.

The current 1.20.x platform implementations share the neutral `AbstractPlatform` base class. Shared Minecraft-bound implementation code lives under `platforms/shared` and is grouped by proven compatibility rather than by ideal loader families. The current source-only shared groups are:

- `platforms/shared/common` for loader-neutral platform implementation helpers such as SLF4J logging
- `platforms/shared/fabriclike-1.19.2-plus` for currently compatible Fabric 1.19.2+, Quilt 1.20.x, and Minecraft 26.x Fabric-like player join and player wrapper logic
- `platforms/shared/forge-1.19.2-plus` for Forge 1.19.2+ player join and player wrapper logic

NeoForge event and player adapters remain per target for now because NeoForge 1.20.1 and NeoForge 1.20.4 currently use meaningfully different API shapes.

Current known shared assumptions:

- The shared UMAPI API artifact uses Java 17 as its current baseline.
- Forge 1.12.2 is intentionally not supported because its Java 8 runtime requirement would force the shared mod code floor too low for the rest of UMAPI.
- Forge 1.16.5 uses the Java 17 toolchain path used by the current ForgeGradle setup.
- Java 17 is enough for the implemented 1.18.2, 1.19.2, 1.20.1, and 1.20.4 targets.
- Minecraft 1.20.5+ targets, including the current 1.20.6 and 1.21.x targets, require Java 21.
- Minecraft 26.1.2 and 26.2 require Java 25.
- Fabric and Quilt targets use Loom-style Minecraft and run task setup.
- Fabric 26.x uses the non-remapping `net.fabricmc.fabric-loom` plugin path, regular `implementation` dependencies, no explicit mappings dependency, and `jar` for export.
- Fabric 1.18.2 keeps its event/player adapters per target because its chat path still uses the older `TextComponent` API.
- Forge 1.16.5 and 1.18.2 keep their event/player adapters per target because their player/chat and login-event APIs differ from Forge 1.19.2+.
- Fabric 1.19.2, Fabric 1.20.x, Fabric 1.21.x, Fabric 26.x, and Quilt 1.20.x targets share the same Fabric-like player join event hook and player wrapper.
- Forge 1.19.2 and Forge 1.20.x targets share the same Forge player login event hook and player wrapper.
- Forge and NeoForge 1.20.x+ targets can share generated `mods.toml`, `pack.mcmeta`, and generated `@Mod` bridge concepts.
- NeoForge 1.21.x and 26.x targets use ModDevGradle with recompilation disabled, matching the current official NeoForge MDK direction and avoiding brittle generated-source recompilation.
- NeoForge 1.20.6+ uses `META-INF/neoforge.mods.toml` instead of `META-INF/mods.toml`.
- Minecraft 1.16.5 and 1.18.2 use the older `TextComponent` player-message path.
- `Component.literal(...)` is still valid for the current 1.19.2, 1.20.x, 1.21.x, and 26.x player-message adapters.

## Class Responsibilities

### Shared API

`UMAPI` is the static entry point used by mods. It stores the active platform implementation, exposes shared services such as `events()`, `logger()`, and `environment()`, and guards against use before the platform has initialised UMAPI.

`UMAPIMod` is the neutral lifecycle interface implemented by consuming mods. Platform code invokes `initialise()` after UMAPI is ready.

`RuntimeEnvironment`, `MinecraftVersion`, and `ModLoader` expose the active loader and Minecraft version to consuming mods. Mods can use them for simple compatibility branches while keeping feature-specific decisions in the mod.

`VersionedValue` stores values that can vary by Minecraft version and selects the best available value for the active runtime. It supports an optional fallback value for defaults, then exact or nearest-version values for targeted overrides. Textures use this first, but the same primitive can later support versioned models, worldgen values, recipes, or other content data.

`ContentRegistry` is the neutral content declaration graph used by consuming mods. It currently supports item definitions only, and will expand one content type at a time.

`ItemContent` is the neutral definition for a simple item. It stores the item id, display name, and texture paths with optional version-specific overrides without exposing Minecraft item classes or loader registration APIs to consuming mods.

`Platform` is the abstraction implemented by each loader/version platform. It exposes the active runtime environment and the services that the shared API can delegate to.

`AbstractPlatform` is the small shared base for platform implementations that hold the runtime environment, an `Events` implementation, and a `Logger` implementation.

`Slf4jLogger` is the shared platform logging adapter. It maps UMAPI logging calls to the SLF4J logging environment supplied by the active loader.

`FabricLikeEvents` is the shared Fabric-like 1.19.2+ event adapter. It maps Fabric API/QFAPI player join callbacks to `Events.onPlayerJoin`.

`FabricLikePlayer` is the shared Fabric-like 1.19.2+ player adapter. It adapts Minecraft's `ServerPlayer` to the shared `Player` interface for compatible Fabric and Quilt targets.

`ForgeEvents` is the shared Forge 1.19.2+ event adapter. It maps Forge's player login event to `Events.onPlayerJoin`.

`ForgePlayer` is the shared Forge 1.19.2+ player adapter. It adapts Minecraft's `ServerPlayer` to the shared `Player` interface for compatible Forge targets.

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

`NeoForgeTargets` centralizes the current NeoForge build wiring. It uses the shared target catalog for supported NeoForge/Minecraft target facts, applies the target's NeoForge build backend, adds UMAPI platform dependencies, delegates generated Forge-family metadata and source setup, exports the finished target jar, and registers NeoForge runtime task wrappers. Current 1.20.x targets use NeoGradle; NeoForge 1.21.x targets use ModDevGradle.

`ForgeTargets` centralizes the current Forge build wiring. It uses the shared target catalog for supported Forge/Minecraft target facts, applies ForgeGradle and Renamer Gradle, adds Forge and UMAPI platform dependencies, delegates generated Forge-family metadata and source setup, exports the reobfuscated target jar, and registers Forge runtime task wrappers.

`QuiltTargets` centralizes the current Quilt build wiring. It uses the shared target catalog for supported Quilt/Minecraft target facts, delegates common Loom setup, adds QFAPI, connects generated Quilt resources to the main resource set, exports the remapped target jar, and registers Quilt runtime task wrappers.

`GenerateFabricModJsonTask` is the typed Gradle task that writes `fabric.mod.json` from neutral UMAPI mod metadata plus Fabric target details.

`GenerateQuiltModJsonTask` is the typed Gradle task that writes `quilt.mod.json` from neutral UMAPI mod metadata plus Quilt target details.

`GenerateForgeFamilyModsTomlTask` is the typed Gradle task that writes Forge-family metadata and `pack.mcmeta` from neutral UMAPI mod metadata plus Forge-family target details.

`GenerateForgeFamilyEntrypointTask` is the typed Gradle task that writes a small generated `@Mod` bridge for consuming mods, so SampleMod can keep using `UMAPIMod` instead of a Forge-family Java entrypoint.

### Fabric 1.18.2 Platform

`Fabric1182Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.18.2 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric1182Platform` is the Fabric 1.18.2 implementation of `Platform`. It wires shared UMAPI services to the target-local Fabric 1.18.2 event/player adapters and shared SLF4J logger.

### Fabric 1.19.2 Platform

`Fabric1192Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.19.2 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric1192Platform` is the Fabric 1.19.2 implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like event/player adapters and shared SLF4J logger.

### Fabric 1.20.1 Platform

`Fabric1201Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.20.1 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric1201Platform` is the Fabric implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like 1.20.x event/player adapters and shared SLF4J logger.

### Fabric 1.20.4 Platform

`Fabric1204Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.20.4 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric1204Platform` is the Fabric 1.20.4 implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like 1.20.x event/player adapters and shared SLF4J logger.

### Fabric 1.20.6 Platform

`Fabric1206Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.20.6 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric1206Platform` is the Fabric 1.20.6 implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like 1.20.x event/player adapters and shared SLF4J logger.

### Fabric 1.21.1 Platform

`Fabric1211Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.21.1 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric1211Platform` is the Fabric 1.21.1 implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like event/player adapters and shared SLF4J logger.

### Fabric 1.21.3 Platform

`Fabric1213Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.21.3 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric1213Platform` is the Fabric 1.21.3 implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like event/player adapters and shared SLF4J logger.

### Fabric 1.21.5 Platform

`Fabric1215Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.21.5 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric1215Platform` is the Fabric 1.21.5 implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like event/player adapters and shared SLF4J logger.

### Fabric 1.21.8 Platform

`Fabric1218Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.21.8 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric1218Platform` is the Fabric 1.21.8 implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like event/player adapters and shared SLF4J logger.

### Fabric 1.21.10 Platform

`Fabric12110Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.21.10 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric12110Platform` is the Fabric 1.21.10 implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like event/player adapters and shared SLF4J logger.

### Fabric 1.21.11 Platform

`Fabric12111Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.21.11 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric12111Platform` is the Fabric 1.21.11 implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like event/player adapters and shared SLF4J logger.

### Fabric 26.1.2 Platform

`Fabric2612Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 26.1.2 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric2612Platform` is the Fabric 26.1.2 implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like event/player adapters and shared SLF4J logger.

### Fabric 26.2 Platform

`Fabric262Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 26.2 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric262Platform` is the Fabric 26.2 implementation of `Platform`. It wires shared UMAPI services to the shared Fabric-like event/player adapters and shared SLF4J logger.

### NeoForge 1.20.1 Platform

`NeoForge1201Entrypoint` is the NeoForge loader entrypoint. It initialises UMAPI with the NeoForge 1.20.1 platform.

`NeoForge1201Platform` is the NeoForge implementation of `Platform`. It wires shared UMAPI services to NeoForge-backed event/player implementations and the shared SLF4J logger. This target intentionally keeps its own event/player adapters because NeoForge 1.20.1 still uses older Forge-style packages and remapped Minecraft method names.

### NeoForge 1.20.4 - 1.21.8 Platforms

`NeoForge1204Entrypoint` is the NeoForge loader entrypoint. It initialises UMAPI with the NeoForge 1.20.4 platform.

`NeoForge1206Entrypoint` is the NeoForge loader entrypoint. It initialises UMAPI with the NeoForge 1.20.6 platform.

`NeoForge1211Entrypoint` is the NeoForge loader entrypoint. It initialises UMAPI with the NeoForge 1.21.1 platform.

`NeoForge1213Entrypoint` is the NeoForge loader entrypoint. It initialises UMAPI with the NeoForge 1.21.3 platform.

`NeoForge1215Entrypoint` is the NeoForge loader entrypoint. It initialises UMAPI with the NeoForge 1.21.5 platform.

`NeoForge1218Entrypoint` is the NeoForge loader entrypoint. It initialises UMAPI with the NeoForge 1.21.8 platform.

The matching `NeoForge1204Platform`, `NeoForge1206Platform`, `NeoForge1211Platform`, `NeoForge1213Platform`, `NeoForge1215Platform`, and `NeoForge1218Platform` classes wire shared UMAPI services to the shared NeoForge 1.20.4+ event adapter, the shared `GameProfile.getName()` player adapter, and the shared SLF4J logger.

`platforms/shared/neoforge-1.20.4-plus` adapts NeoForge event callbacks to the shared `Events` interface. It currently maps NeoForge's player login event to `Events.onPlayerJoin`.

`platforms/shared/neoforge-player-gameprofile-getname` adapts Minecraft's `ServerPlayer` to the shared `Player` interface for NeoForge targets whose `GameProfile` exposes `getName()`.

### NeoForge 1.21.10+ and 26.x Platforms

`NeoForge12110Entrypoint` is the NeoForge loader entrypoint. It initialises UMAPI with the NeoForge 1.21.10 platform.

`NeoForge12111Entrypoint` is the NeoForge loader entrypoint. It initialises UMAPI with the NeoForge 1.21.11 platform.

`NeoForge2612Entrypoint` is the NeoForge loader entrypoint. It initialises UMAPI with the NeoForge 26.1.2 platform.

`NeoForge262Entrypoint` is the NeoForge loader entrypoint. It initialises UMAPI with the NeoForge 26.2 platform.

The matching `NeoForge12110Platform`, `NeoForge12111Platform`, `NeoForge2612Platform`, and `NeoForge262Platform` classes wire shared UMAPI services to the shared NeoForge 1.20.4+ event adapter, the shared `GameProfile.name()` player adapter, and the shared SLF4J logger.

`platforms/shared/neoforge-player-gameprofile-name` adapts Minecraft's `ServerPlayer` to the shared `Player` interface for NeoForge targets whose `GameProfile` exposes `name()`.

### Forge 1.18.2 Platform

`Forge1182Entrypoint` is the Forge loader entrypoint. It initialises UMAPI with the Forge 1.18.2 platform.

`Forge1182Platform` is the Forge 1.18.2 implementation of `Platform`. It wires shared UMAPI services to the target-local Forge 1.18.2 event/player adapters and shared SLF4J logger.

### Forge 1.19.2 Platform

`Forge1192Entrypoint` is the Forge loader entrypoint. It initialises UMAPI with the Forge 1.19.2 platform.

`Forge1192Platform` is the Forge 1.19.2 implementation of `Platform`. It wires shared UMAPI services to the shared Forge 1.19.2+ event/player adapters and shared SLF4J logger.

### Forge 1.20.1 Platform

`Forge1201Entrypoint` is the Forge loader entrypoint. It initialises UMAPI with the Forge 1.20.1 platform.

`Forge1201Platform` is the Forge implementation of `Platform`. It wires shared UMAPI services to the shared Forge 1.19.2+ event/player adapters and shared SLF4J logger.

### Forge 1.20.4 Platform

`Forge1204Entrypoint` is the Forge loader entrypoint. It initialises UMAPI with the Forge 1.20.4 platform.

`Forge1204Platform` is the Forge 1.20.4 implementation of `Platform`. It wires shared UMAPI services to the shared Forge 1.19.2+ event/player adapters and shared SLF4J logger.

### Forge 1.20.6 Platform

`Forge1206Entrypoint` is the Forge loader entrypoint. It initialises UMAPI with the Forge 1.20.6 platform.

`Forge1206Platform` is the Forge 1.20.6 implementation of `Platform`. It wires shared UMAPI services to the shared Forge 1.19.2+ event/player adapters and shared SLF4J logger.

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
.\gradlew.bat exportUMAPIFabric1182
.\gradlew.bat exportUMAPIFabric1192
.\gradlew.bat exportUMAPIFabric1201
.\gradlew.bat exportUMAPIFabric1204
.\gradlew.bat exportUMAPIFabric1206
.\gradlew.bat exportUMAPIFabric1211
.\gradlew.bat exportUMAPIFabric1213
.\gradlew.bat exportUMAPIFabric1215
.\gradlew.bat exportUMAPIFabric1218
.\gradlew.bat exportUMAPIFabric12110
.\gradlew.bat exportUMAPIFabric12111
.\gradlew.bat exportUMAPIFabric2612
.\gradlew.bat exportUMAPIFabric262
.\gradlew.bat exportUMAPINeoForge1201
.\gradlew.bat exportUMAPINeoForge1204
.\gradlew.bat exportUMAPINeoForge1206
.\gradlew.bat exportUMAPINeoForge1211
.\gradlew.bat exportUMAPINeoForge1213
.\gradlew.bat exportUMAPINeoForge1215
.\gradlew.bat exportUMAPINeoForge1218
.\gradlew.bat exportUMAPINeoForge12110
.\gradlew.bat exportUMAPINeoForge12111
.\gradlew.bat exportUMAPINeoForge2612
.\gradlew.bat exportUMAPINeoForge262
.\gradlew.bat exportUMAPIForge1165
.\gradlew.bat exportUMAPIForge1182
.\gradlew.bat exportUMAPIForge1192
.\gradlew.bat exportUMAPIForge1201
.\gradlew.bat exportUMAPIForge1204
.\gradlew.bat exportUMAPIForge1206
.\gradlew.bat exportUMAPIQuilt1201
.\gradlew.bat exportUMAPIQuilt1204
```

Exported jars are copied to `build/umapi/exports` with readable names containing the mod name, mod version, loader, and Minecraft version.

Consuming mods also receive UMAPI runtime tasks:

```powershell
.\gradlew.bat runUMAPIFabric1182Client
.\gradlew.bat runUMAPIFabric1182Server
.\gradlew.bat runUMAPIFabric1192Client
.\gradlew.bat runUMAPIFabric1192Server
.\gradlew.bat runUMAPIFabric1201Client
.\gradlew.bat runUMAPIFabric1201Server
.\gradlew.bat runUMAPIFabric1204Client
.\gradlew.bat runUMAPIFabric1204Server
.\gradlew.bat runUMAPIFabric1206Client
.\gradlew.bat runUMAPIFabric1206Server
.\gradlew.bat runUMAPIFabric1211Client
.\gradlew.bat runUMAPIFabric1211Server
.\gradlew.bat runUMAPIFabric1213Client
.\gradlew.bat runUMAPIFabric1213Server
.\gradlew.bat runUMAPIFabric1215Client
.\gradlew.bat runUMAPIFabric1215Server
.\gradlew.bat runUMAPIFabric1218Client
.\gradlew.bat runUMAPIFabric1218Server
.\gradlew.bat runUMAPIFabric12110Client
.\gradlew.bat runUMAPIFabric12110Server
.\gradlew.bat runUMAPIFabric12111Client
.\gradlew.bat runUMAPIFabric12111Server
.\gradlew.bat runUMAPIFabric2612Client
.\gradlew.bat runUMAPIFabric2612Server
.\gradlew.bat runUMAPIFabric262Client
.\gradlew.bat runUMAPIFabric262Server
.\gradlew.bat runUMAPINeoForge1201Client
.\gradlew.bat runUMAPINeoForge1201Server
.\gradlew.bat runUMAPINeoForge1204Client
.\gradlew.bat runUMAPINeoForge1204Server
.\gradlew.bat runUMAPINeoForge1206Client
.\gradlew.bat runUMAPINeoForge1206Server
.\gradlew.bat runUMAPINeoForge1211Client
.\gradlew.bat runUMAPINeoForge1211Server
.\gradlew.bat runUMAPINeoForge1213Client
.\gradlew.bat runUMAPINeoForge1213Server
.\gradlew.bat runUMAPINeoForge1215Client
.\gradlew.bat runUMAPINeoForge1215Server
.\gradlew.bat runUMAPINeoForge1218Client
.\gradlew.bat runUMAPINeoForge1218Server
.\gradlew.bat runUMAPINeoForge12110Client
.\gradlew.bat runUMAPINeoForge12110Server
.\gradlew.bat runUMAPINeoForge12111Client
.\gradlew.bat runUMAPINeoForge12111Server
.\gradlew.bat runUMAPINeoForge2612Client
.\gradlew.bat runUMAPINeoForge2612Server
.\gradlew.bat runUMAPINeoForge262Client
.\gradlew.bat runUMAPINeoForge262Server
.\gradlew.bat runUMAPIForge1165Client
.\gradlew.bat runUMAPIForge1165Server
.\gradlew.bat runUMAPIForge1182Client
.\gradlew.bat runUMAPIForge1182Server
.\gradlew.bat runUMAPIForge1192Client
.\gradlew.bat runUMAPIForge1192Server
.\gradlew.bat runUMAPIForge1201Client
.\gradlew.bat runUMAPIForge1201Server
.\gradlew.bat runUMAPIForge1204Client
.\gradlew.bat runUMAPIForge1204Server
.\gradlew.bat runUMAPIForge1206Client
.\gradlew.bat runUMAPIForge1206Server
.\gradlew.bat runUMAPIQuilt1201Client
.\gradlew.bat runUMAPIQuilt1201Server
.\gradlew.bat runUMAPIQuilt1204Client
.\gradlew.bat runUMAPIQuilt1204Server
.\gradlew.bat runUMAPIClient
.\gradlew.bat runUMAPIServer
```

The loader-specific tasks delegate to the selected loader tooling's native run tasks. The neutral `runUMAPIClient` and `runUMAPIServer` tasks use the configured or automatically selected default runtime.

UMAPI keeps loader runtime data in target-specific directories under `runs/`, such as `runs/fabric1182Client`, `runs/fabric1192Client`, `runs/fabric1201Client`, `runs/fabric1204Client`, `runs/fabric1206Client`, `runs/fabric1211Client`, `runs/fabric1213Client`, `runs/fabric1215Client`, `runs/fabric1218Client`, `runs/fabric12110Client`, `runs/fabric12111Client`, `runs/fabric2612Client`, `runs/fabric262Client`, `runs/neoForge1201Client`, `runs/neoForge1204Client`, `runs/neoForge1206Client`, `runs/neoForge1211Client`, `runs/neoForge1213Client`, `runs/neoForge1215Client`, `runs/neoForge1218Client`, `runs/neoForge12110Client`, `runs/neoForge12111Client`, `runs/neoForge2612Client`, `runs/neoForge262Client`, `runs/forge1165Client`, `runs/forge1182Client`, `runs/forge1192Client`, `runs/forge1201Client`, `runs/forge1204Client`, `runs/forge1206Client`, `runs/quilt1201Client`, and `runs/quilt1204Client`.
