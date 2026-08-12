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

`platforms` contains loader/version-specific implementations. For example, `platforms/fabric-1.20.1` adapts the shared API to Fabric on Minecraft 1.20.1, and `platforms/neoforge-1.20.1` adapts the shared API to NeoForge on Minecraft 1.20.1.

## Current State

The first known-good path is Fabric for Minecraft 1.20.1. SampleMod has successfully launched through this path and displayed its welcome message in chat. UMAPI also has an initial NeoForge 1.20.1 target that can compile the UMAPI platform and SampleMod against NeoForge.

The Gradle plugin currently:

- applies Java and configures Java 17 for consuming mod code
- applies Fabric Loom for the Fabric 1.20.1 target, or NeoGradle for the NeoForge 1.20.1 target
- adds Minecraft, mappings, loader, and UMAPI platform dependencies for the selected target
- adds the shared UMAPI API dependency
- generates loader metadata from neutral UMAPI mod metadata
- exports the finished mod jar to `build/umapi/exports`
- exposes UMAPI runtime tasks for launching the current target client or server
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
        // or, during the current NeoForge pass:
        // neoforge("1.20.1")
    }
}
```

The DSL lets consuming mods declare neutral mod metadata and their intended target. UMAPI currently accepts exactly one target at a time while NeoForge support is being introduced.

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

If no runtime default is declared, UMAPI chooses the latest declared Minecraft version and then prefers Fabric, NeoForge, and Forge in that order.

## Class Responsibilities

### Shared API

`UMAPI` is the static entry point used by mods. It stores the active platform implementation, exposes shared services such as `events()`, and guards against use before the platform has initialised UMAPI.

`UMAPIMod` is the neutral lifecycle interface implemented by consuming mods. Platform code invokes `initialise()` after UMAPI is ready.

`Platform` is the abstraction implemented by each loader/version platform. It exposes the services that the shared API can delegate to.

`Events` is the current shared event surface. It lets mods register callbacks without depending on Fabric event classes.

`Player` is the current shared player abstraction. It exposes only the player behavior UMAPI currently needs: reading the player name and sending a chat message.

`Logger` is the shared logging abstraction. It lets mods write basic log messages without depending on a loader-specific logging API.

### Gradle Plugins

`UMAPISettingsPlugin` is the lightweight settings plugin from `settings-plugin`. It runs from `settings.gradle.kts` and adds repositories needed to resolve UMAPI's loader tooling before the main project plugin is loaded.

`UMAPIPlugin` is the main project plugin from `gradle-plugin`. It applies Java, configures Java 17 for consuming mod code, adds the shared UMAPI API dependency, creates the `umapi {}` DSL, and performs final validation.

`UMAPIExtension` is the root Gradle DSL object behind `umapi {}`. It owns the neutral `mod {}` metadata block, the `targets {}` block, and optional runtime defaults.

`UMAPIModExtension` stores neutral mod metadata such as id, name, description, authors, and UMAPI entrypoint. It validates that required metadata exists before generated platform resources are used.

`UMAPITargetsExtension` stores target declarations from `targets {}`. For now it accepts exactly one supported target, either `fabric("1.20.1")` or `neoforge("1.20.1")`, and delegates that target's setup to the relevant target helper.

`UMAPIRuntimeExtension` stores optional default runtime choices. If no default is configured, it chooses the latest declared Minecraft version and then prefers Fabric, NeoForge, and Forge in that order.

`UMAPIRuntimeTarget` is the internal record UMAPI uses to describe a runnable target, including its loader, Minecraft version, and client/server task names.

`Fabric1201Target` centralizes the current Fabric 1.20.1 build wiring. It owns the Fabric/Minecraft constants for this target, adds Minecraft, mappings, Fabric Loader, and UMAPI platform dependencies, connects generated Fabric resources to the main resource set, exports the finished target jar, and registers Fabric 1.20.1 runtime task wrappers.

`NeoForge1201Target` centralizes the current NeoForge 1.20.1 build wiring. It owns the NeoForge/Minecraft constants for this target, applies NeoGradle, adds NeoForge and UMAPI platform dependencies, connects generated NeoForge resources and source to the main source set, exports the finished target jar, and registers NeoForge 1.20.1 runtime task wrappers.

`GenerateFabricModJsonTask` is the typed Gradle task that writes `fabric.mod.json` from neutral UMAPI mod metadata plus Fabric 1.20.1 target details.

`GenerateNeoForgeModsTomlTask` is the typed Gradle task that writes `META-INF/mods.toml` from neutral UMAPI mod metadata plus NeoForge 1.20.1 target details.

`GenerateNeoForgeEntrypointTask` is the typed Gradle task that writes a small generated `@Mod` bridge for consuming mods, so SampleMod can keep using `UMAPIMod` instead of a NeoForge-specific Java entrypoint.

### Fabric 1.20.1 Platform

`Fabric1201Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.20.1 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric1201Platform` is the Fabric implementation of `Platform`. It wires shared UMAPI services to Fabric-backed implementations.

`FabricEvents` adapts Fabric event callbacks to the shared `Events` interface. It currently maps Fabric's player join event to `Events.onPlayerJoin`.

`FabricPlayer` adapts Minecraft's `ServerPlayer` to the shared `Player` interface. It translates UMAPI player operations into Minecraft/Fabric calls.

`FabricLogger` adapts UMAPI logging calls to Fabric's SLF4J-backed logging environment.

### NeoForge 1.20.1 Platform

`NeoForge1201Entrypoint` is the NeoForge loader entrypoint. It initialises UMAPI with the NeoForge 1.20.1 platform.

`NeoForge1201Platform` is the NeoForge implementation of `Platform`. It wires shared UMAPI services to NeoForge-backed implementations.

`NeoForgeEvents` adapts NeoForge event callbacks to the shared `Events` interface. It currently maps NeoForge's player login event to `Events.onPlayerJoin`.

`NeoForgePlayer` adapts Minecraft's `ServerPlayer` to the shared `Player` interface for the NeoForge 1.20.1 mapping layer.

`NeoForgeLogger` adapts UMAPI logging calls to NeoForge's SLF4J-backed logging environment.

## Development Approach

UMAPI should be developed conservatively:

- keep the Fabric 1.20.1 path working
- make one small change at a time
- build UMAPI and SampleMod after each meaningful step
- avoid broad refactors while platform ownership is still moving
- avoid hardcoded duplicate version strings where the build already has a single source of truth

## Building

From the repository root:

```powershell
.\gradlew.bat clean build
```

SampleMod should also be built after UMAPI changes, because it exercises the plugin and the current platform path as a consuming mod.

Consuming mods that use the UMAPI plugin receive an `exportUMAPI` task. For the current Fabric 1.20.1 target, the exported jar is copied to `build/umapi/exports` with a readable name containing the mod name, mod version, loader, and Minecraft version.

Consuming mods also receive UMAPI runtime tasks:

```powershell
.\gradlew.bat runUMAPIFabric1201Client
.\gradlew.bat runUMAPIFabric1201Server
.\gradlew.bat runUMAPINeoForge1201Client
.\gradlew.bat runUMAPINeoForge1201Server
.\gradlew.bat runUMAPIClient
.\gradlew.bat runUMAPIServer
```

The loader-specific tasks delegate to the selected loader tooling's native run tasks. The neutral `runUMAPIClient` and `runUMAPIServer` tasks use the configured or automatically selected default runtime.
