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

`platforms` contains loader/version-specific implementations. For example, `platforms/fabric-1.20.1` adapts the shared API to Fabric on Minecraft 1.20.1.

## Current State

The current known-good path is Fabric for Minecraft 1.20.1. SampleMod has successfully launched through this path and displayed its welcome message in chat.

The Gradle plugin currently:

- configures Java 17 when the Java plugin is present
- applies Fabric Loom for the Fabric 1.20.1 target
- adds Minecraft, mappings, Fabric Loader, and the Fabric 1.20.1 UMAPI platform dependency
- adds the shared UMAPI API dependency
- generates Fabric metadata from neutral UMAPI mod metadata
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
    }
}
```

The DSL lets consuming mods declare neutral mod metadata and their intended target matrix. UMAPI owns the Fabric-specific build wiring for the currently supported target.

## Class Responsibilities

### Shared API

`UMAPI` is the static entry point used by mods. It stores the active platform implementation, exposes shared services such as `events()`, and guards against use before the platform has initialised UMAPI.

`UMAPIMod` is the neutral lifecycle interface implemented by consuming mods. Platform code invokes `initialise()` after UMAPI is ready.

`Platform` is the abstraction implemented by each loader/version platform. It exposes the services that the shared API can delegate to.

`Events` is the current shared event surface. It lets mods register callbacks without depending on Fabric event classes.

`Player` is the current shared player abstraction. It exposes only the player behavior UMAPI currently needs: reading the player name and sending a chat message.

### Gradle Plugins

`UMAPISettingsPlugin` is the lightweight settings plugin from `settings-plugin`. It runs from `settings.gradle.kts` and adds repositories needed to resolve UMAPI's loader tooling before the main project plugin is loaded.

`UMAPIPlugin` is the main project plugin from `gradle-plugin`. It applies Fabric Loom, configures Java 17 for consuming mod code, adds the shared UMAPI API dependency, creates the `umapi {}` DSL, and performs final validation.

`UMAPIExtension` is the root Gradle DSL object behind `umapi {}`. It owns the neutral `mod {}` metadata block and the `targets {}` block.

`UMAPIModExtension` stores neutral mod metadata such as id, name, description, authors, and UMAPI entrypoint. It validates that required metadata exists before generated platform resources are used.

`UMAPITargetsExtension` stores target declarations from `targets {}`. For now it accepts exactly one supported target, `fabric("1.20.1")`, and delegates that target's setup to the Fabric target helper.

`Fabric1201Target` centralizes the current Fabric 1.20.1 build wiring. It owns the Fabric/Minecraft constants for this target, adds Minecraft, mappings, Fabric Loader, and UMAPI platform dependencies, and connects generated Fabric resources to the main resource set.

`GenerateFabricModJsonTask` is the typed Gradle task that writes `fabric.mod.json` from neutral UMAPI mod metadata plus Fabric 1.20.1 target details.

### Fabric 1.20.1 Platform

`Fabric1201Entrypoint` is the Fabric loader entrypoint. It initialises UMAPI with the Fabric 1.20.1 platform and then invokes all consuming mod entrypoints registered under UMAPI's `umapi` entrypoint key.

`Fabric1201Platform` is the Fabric implementation of `Platform`. It wires shared UMAPI services to Fabric-backed implementations.

`FabricEvents` adapts Fabric event callbacks to the shared `Events` interface. It currently maps Fabric's player join event to `Events.onPlayerJoin`.

`FabricPlayer` adapts Minecraft's `ServerPlayer` to the shared `Player` interface. It translates UMAPI player operations into Minecraft/Fabric calls.

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
