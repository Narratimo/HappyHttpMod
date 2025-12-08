# docs/gradle-structure-plan.md

This document describes the planned Gradle and module structure for building multiple Minecraft versions and multiple loaders from one repository.

It is a planning document owned by the project owner. Technical contributors can propose changes through PRs, but decisions must be recorded here (or in a linked issue) so the structure stays consistent.

## Goals

- Build multiple Minecraft versions from one repo
- Build multiple loaders from one repo (Forge, NeoForge, Fabric)
- Produce one jar per loader + Minecraft version
- Allow building all jars in one command
- Keep shared logic in `common` and isolate version-specific glue

## Current observed structure (high level)

The repository currently contains:
- `common/`
- `forge/`
- `fabric/`
- `neoforge/`
- root `build.gradle` and `settings.gradle`

There are already signs of an intended multi-loader layout. The next step is to make the build predictable and scalable for multi-version.

## Planned structure

### Principle: one module = one jar

Each supported combination is its own Gradle module.

Illustrative layout

```text
common/

forge/
  mc_1_20_2/
  mc_1_21_1/

neoforge/
  mc_1_21_1/

fabric/
  mc_1_21_1/
```

Optional later (if helpful)
- loader-common modules, for example `forge/common/`, to share glue across Forge versions

## Dependency rules

- Version modules depend on `common`
- `common` must not depend on Minecraft or loader APIs (avoid leaking platform code)
- Version modules may include loader and Minecraft APIs and do the integration work

## Jar naming

Jar names must include loader and Minecraft version.

Recommended format

- `happyhttp-<mod_version>-<loader>-mc<minecraft_version>.jar`

Examples
- `happyhttp-1.2.3-forge-mc1.20.2.jar`
- `happyhttp-1.2.3-forge-mc1.21.1.jar`

## Build tasks (planned)

Root tasks we want:

- `buildAll`
  - builds all supported modules

- `collectJars`
  - collects jars from all supported modules into a single folder like `dist/`

Example

```sh
./gradlew clean buildAll collectJars
```

## Version selection strategy

We choose a small number of supported targets and grow gradually.

Policy
- Avoid adding 15 modules at once
- Start with one “baseline” and one “next target”
- Expand only when the build and port process is repeatable

Recommended initial set
- Forge: `mc_1_20_2` (baseline)
- Forge: one 1.21 target patch (first port)
- NeoForge and Fabric follow after the structure is stable

## Multi-loader + multi-version combined strategy

We treat loader and version as separate dimensions.

How this affects design
- Shared logic is in `common`
- Each loader/version module only contains the minimum glue and resources for that target
- Ports should mostly be mechanical changes to the glue layer, not re-architectures

## What this plan implies for issue tracking

Compatibility issues should not be opened per patch version unless a patch introduces a real break.

Instead:
- Track the 1.21 line under one Forge compatibility tracker issue
- Update `docs/versions.md` with Beta/Stable/Broken/Untested statuses

## Open decisions (to be resolved by technical contributors)

These items require technical confirmation:

- Whether `common/` should be a plain Java module or a shared mod module with limited dependencies
- Whether to use a single root Gradle convention plugin to enforce jar naming and publishing rules
- Whether to create loader-common modules (Forge common, Fabric common, NeoForge common)
- How to share resources across modules cleanly without duplication
- How to implement CI so it builds and uploads all jars for testing

Each decision should be recorded in a GitHub issue and linked here.

## References

- Repo layout: `docs/repo-layout.md`
- Porting guide: `docs/porting-guide.md`
- Build and release: `docs/build-and-release.md`
- Supported versions: `docs/versions.md`
