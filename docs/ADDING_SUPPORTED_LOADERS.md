# Adding Supported Loaders and Targets

This document records the current best practice for adding loader/version support to UMAPI.

In UMAPI, a target means one loader plus one Minecraft version, such as `fabric("1.20.4")`. A loader means a whole loader family, such as Fabric, Forge, NeoForge, or Quilt.

## Guiding Rules

- Preserve the current known-good paths, especially Fabric 1.20.1.
- Add one small target at a time unless the loader family is already well proven.
- Keep SampleMod neutral. SampleMod should declare intent through `umapi { targets { ... } }`, not loader Gradle plugins, Minecraft dependencies, metadata files, or loader entrypoints.
- Put target facts in `UMAPITargetCatalog` first. Avoid scattering loader versions, dependency versions, version ranges, generated package names, or pack formats through the plugin.
- Share code only when it is practically identical. Do not force Forge, NeoForge, Fabric, or Quilt logic together just because they are conceptually similar.
- Prefer source-only shared platform code under `platforms/shared` when multiple concrete platform jars can safely compile the same source.
- Let Gradle's normal resource pipeline own generated metadata. Avoid deleting or copying files directly from `build/resources/main` unless there is no cleaner option.

## Existing Structure

The current project has these main pieces:

- `api`: the neutral UMAPI API used by SpilledSoup mods.
- `gradle-plugin`: the project plugin applied by consuming mods.
- `settings-plugin`: the settings plugin that adds plugin repositories and target-specific buildscript tooling early enough for Gradle to resolve loader plugins.
- `platforms/<loader>-<minecraftVersion>`: one concrete platform implementation per supported loader/version pair.
- `platforms/shared`: source-only shared implementation code used by concrete platform modules when compatibility is proven.

The current plugin-side target flow is:

1. SampleMod declares targets in the UMAPI DSL.
2. `UMAPITargetsExtension` validates the declared targets.
3. `UMAPISupportedTargets` finds the requested target in `UMAPITargetCatalog`.
4. The loader-specific helper configures the selected target:
   - `FabricTargets`
   - `NeoForgeTargets`
   - `ForgeTargets`
   - `QuiltTargets`
5. The selected helper applies loader tooling, dependencies, generated resources, generated entrypoints, export tasks, and run tasks.

## Adding a New Minecraft Version for an Existing Loader

This is the preferred first step when expanding support.

### 1. Add the Target to the Catalog

Add a new target entry to `UMAPITargetCatalog`.

Include all known target facts there:

- Minecraft version
- loader dependency version
- platform API dependency version, if applicable
- Minecraft version range for generated metadata
- loader version range for generated metadata
- generated entrypoint package
- resource pack format

The catalog should be the first place to check when a target behaves differently from another version.

### 2. Add the Platform Module

Create a module named:

```text
platforms/<loader>-<minecraftVersion>
```

Examples:

```text
platforms/fabric-1.20.4
platforms/neoforge-1.20.4
platforms/forge-1.20.4
platforms/quilt-1.20.4
```

Add it to UMAPI `settings.gradle.kts`.

Start by copying the nearest known-good platform for the same loader, then reduce duplication only after it builds and runs.

### 3. Reuse Shared Source Only When Proven Compatible

Current proven shared groups:

- `platforms/shared/common`: loader-neutral helpers such as SLF4J logging.
- `platforms/shared/fabriclike-1.20.x`: Fabric and Quilt 1.20.x player join and player wrapper logic.
- `platforms/shared/forge-1.20.x`: Forge 1.20.x player join and player wrapper logic.

Add shared source directories from the concrete platform build script:

```kotlin
extensions.configure<SourceSetContainer> {
    named("main") {
        java.srcDir("../shared/common/src/main/java")
    }
}
```

Only add a loader/version-specific shared folder when the imports, event hook, player wrapper, and method names are actually compatible.

If the code is almost the same but uses different packages or method names, keep it per-target for now.

### 4. Keep Entrypoints Thin

Each platform module should still own its loader entrypoint. The entrypoint should:

- initialise UMAPI with the concrete platform
- invoke consuming UMAPI mod entrypoints if the loader needs that bridge
- avoid putting gameplay logic directly in the loader entrypoint

The concrete platform class should usually be a thin wrapper around implementations of UMAPI services:

```java
public final class Fabric1204Platform extends AbstractPlatform {
    public Fabric1204Platform() {
        super(new FabricLikeEvents(), new Slf4jLogger());
    }
}
```

### 5. Add SampleMod Declaration

Add only the target declaration to SampleMod:

```kotlin
umapi {
    targets {
        fabric("1.20.4")
    }
}
```

Do not add loader plugins, Minecraft dependencies, loader dependencies, mappings, metadata files, or loader entrypoint classes to SampleMod.

## Adding a New Loader Family

Adding a new loader family is larger than adding a new Minecraft version for an existing loader.

### 1. Add the Loader ID

Add the loader to `UMAPILoader`.

Choose:

- stable id, such as `fabric`
- display name, such as `Fabric`
- run directory name part
- runtime priority

Runtime priority controls default runtime selection when SampleMod declares multiple targets and does not choose a default.

Current default preference is:

```text
Fabric, NeoForge, Forge, Quilt
```

### 2. Add Settings Plugin Support If Needed

Some loader Gradle tooling must be available before the project plugin is configured.

Use `settings-plugin` for:

- plugin repositories needed by consuming builds
- target-specific buildscript classpath tooling
- tooling that should not sit permanently on UMAPI's main Gradle plugin compile classpath

This matters because loader Gradle plugins can have their own Java or Gradle compatibility requirements. We saw this with Loom tooling: putting it directly on the UMAPI Gradle plugin classpath caused Java compatibility issues, so the settings plugin now injects target-specific tooling only for the active target.

### 3. Add Catalog Records

Add a catalog record type if the loader has unique target facts.

Examples:

- Fabric needs loader version, loader dependency range, and Fabric API version.
- Quilt needs loader version, loader dependency range, and QFAPI version.
- Forge and NeoForge need metadata ranges, resource pack format, and generated entrypoint package details.

Keep the data shape honest. If the loader differs from existing families, add a new record rather than squeezing it into an existing one.

### 4. Add a Loader Target Helper

Create a helper similar to:

```text
FabricTargets
NeoForgeTargets
ForgeTargets
QuiltTargets
```

The helper should own:

- applying the loader Gradle plugin
- adding repositories not covered by settings/plugin resolution
- adding Minecraft, mappings, loader, and UMAPI platform dependencies
- generated metadata wiring
- generated entrypoint wiring, if needed
- export task registration
- runtime task registration

Then route the loader in `UMAPISupportedTargets`.

### 5. Decide Whether Existing Shared Helpers Apply

Use existing support classes only when they truly match:

- `UMAPILoomTargetSupport` is for Loom-family targets such as Fabric and Quilt.
- `UMAPIForgeFamilyTargetSupport` is currently for Forge-family generated `mods.toml`, `pack.mcmeta`, and generated `@Mod` bridges.
- `UMAPIExportTasks` should be used by all target helpers.
- `UMAPIRuntimeTasks` should be used by all target helpers for task naming.
- `UMAPIGeneratedResources` should be used when generated metadata can flow through normal resources.

If a new loader behaves differently, create a focused helper instead of adding loader-specific branches everywhere.

## Generated Metadata Best Practice

Generated metadata should come from neutral UMAPI mod metadata plus catalog target facts.

SampleMod should not own:

- `fabric.mod.json`
- `quilt.mod.json`
- `mods.toml`
- `pack.mcmeta`
- loader-specific entrypoint declarations

Generated resources should usually live under target-specific generated folders, such as:

```text
build/generated/resources/umapi-fabric/1204
build/generated/resources/umapi-forge/1204
```

The generated folder should be wired into the main resources source set and `processResources` should depend on the generator task.

Avoid using shared output paths for multiple targets. Shared output paths can make full exports pick up stale metadata from a previous target.

Forge is the current exception to the normal resource wiring rule. During Forge dev runs, Forge can treat `build/resources/main` as its own mod file. If that folder contains `META-INF/mods.toml` but not the generated `@Mod` class, Forge reports that the mod file has mods that were not found.

For Forge targets, generate `mods.toml` and `pack.mcmeta` into a target-specific generated folder, then copy those generated resources into the Java classes output so they sit beside the generated `@Mod` class. Also remove stale generated Forge metadata from `build/resources/main` before `processResources`, because older runs may have left files there.

## Runtime Task Best Practice

Every target should expose:

```text
runUMAPI<Loader><MinecraftVersion>Client
runUMAPI<Loader><MinecraftVersion>Server
```

Examples:

```text
runUMAPIFabric1204Client
runUMAPIForge1204Client
runUMAPIQuilt1204Server
```

The neutral shortcuts are:

```text
runUMAPIClient
runUMAPIServer
```

With one target, the shortcut should run that target.

With multiple targets, the shortcut should use the configured runtime default, or automatically choose the latest Minecraft version and then the preferred loader order.

Runtime directories must be target-specific:

```text
runs/fabric1204Client
runs/quilt1204Client
runs/forge1204Client
runs/neoForge1204Client
```

Do not let two loaders share the same run directory. Fabric and Quilt in particular can look compatible while still leaving incompatible loader cache or runtime data behind.

## Export Best Practice

Every target should expose:

```text
exportUMAPI<Loader><MinecraftVersion>
```

The aggregate task is:

```text
exportUMAPI
```

Exports should land in:

```text
build/umapi/exports
```

Jar names should include:

- mod name
- mod version
- loader
- Minecraft version

This keeps drag-and-drop testing and Windows Explorer sorting simple.

Do not make normal `build` export every target. `build` should stay a verification command. Use target-specific export tasks while adding a new target, then use `exportUMAPI` after a group of targets is stable.

## Testing Order

For a new target, use this order:

1. Build UMAPI.
2. Build SampleMod.
3. Export only the new SampleMod target.
4. Run only the new SampleMod client.
5. If that works, run the server if the target has server support.
6. After several related targets are added, run a full `exportUMAPI`.

Preferred commands:

```powershell
cd C:\Users\mattw\Documents\Git\UMAPI
.\gradlew.bat build

cd C:\Users\mattw\Documents\Git\SampleMod
.\gradlew.bat build
.\gradlew.bat exportUMAPI<Loader><MinecraftVersion>
.\gradlew.bat runUMAPI<Loader><MinecraftVersion>Client
```

Use `clean build` when source-set wiring, generated-resource wiring, or deleted/renamed classes changed. Normal `build` is usually enough for small catalog additions after caches are warm.

## Known Pitfalls

### Plugin resolution happens early

Gradle needs plugin repositories before project plugins are applied. If SampleMod needs a repository only to resolve loader tooling, put that responsibility in the UMAPI settings plugin.

### Loader Gradle plugins can have Java compatibility requirements

Do not casually add loader Gradle plugin implementation dependencies to `gradle-plugin`. A plugin compiled for Java 21 can break a Java 17 UMAPI Gradle plugin compile path.

Prefer active-target tooling injection through the settings plugin when needed.

### Configuration cache is useful but exposes bad task wiring

Configuration cache should stay enabled because it makes repeated builds much faster, but it rejects tasks that capture project state at execution time.

Prefer typed task inputs and provider-backed paths.

Mark native loader run tasks as not compatible only when the loader tooling itself keeps unsupported state.

### Full exports run targets in separate nested Gradle builds

When multiple targets are declared, UMAPI currently invokes target-specific nested Gradle runs with `umapi.activeTarget`.

This keeps incompatible loader plugins from being applied together, but it means generated outputs must be isolated by target. Do not use shared generated metadata paths.

### Forge and NeoForge are related but not identical

They can currently share generated Forge-family metadata and generated `@Mod` bridge concepts.

They should not automatically share dependency setup, runtime setup, or Minecraft-bound event/player adapters unless the specific versions prove compatible.

### Quilt is Fabric-like, but still separate

Quilt can share some Fabric-like API code and Loom-style setup, but it uses Quilt Loader, Quilt Loom, and QFAPI. Keep its catalog facts separate.

### Runtime cache can hide local platform version changes

Loom may cache remapped UMAPI jars by mod id/version. During local development, runtime tasks may need cache invalidation so Minecraft sees the current UMAPI jar without requiring fake version bumps.

### Use real runtime testing

A Gradle build can succeed while the loader rejects metadata or fails during game startup. Always run the client for a new loader/version target before treating it as supported.

## When to Batch Targets

Add the first target for a loader family alone.

Once one target for a loader family works, adding nearby Minecraft versions can be batched cautiously when:

- the same loader Gradle plugin works
- the same Java version works
- generated metadata format is unchanged
- event/player code is identical or safely isolated
- dependency versions are known

Good batch candidates:

- adding several Fabric 1.20.x targets after Fabric 1.20.1 and 1.20.4 are stable
- adding several Forge versions only after Forge build, export, reobf, and runtime behavior are proven for that version range
- adding NeoForge versions only after confirming whether package names and event APIs match

Avoid batching across different loader families until each family has at least one working target.

## Definition of Done

A target is not really supported until:

- UMAPI builds
- SampleMod builds
- target-specific export produces a jar
- the target client starts
- SampleMod initialises
- the welcome message appears in chat
- the exported jar can be used in a normal mods folder together with any required UMAPI/platform jars
- README/docs mention any new assumptions or known quirks
