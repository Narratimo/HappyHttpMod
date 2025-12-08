# Build and release

This document describes how to build the mod locally and how releases should be produced once the multi-module layout is in place.

It is written to support a repository that builds multiple jars, one per loader and Minecraft version.

## Principles

- Every jar is built by exactly one module (loader + Minecraft version)
- Jar names must include loader and Minecraft version so they never overwrite
- The root project provides tasks to build all targets and collect jars into one folder
- Built jars should not be committed to the repository

## Prerequisites

- Git
- JDK as required by the target Minecraft version and loader
- Gradle wrapper working (`./gradlew`)

If you are unsure which Java version you need, check the module Gradle configuration and the relevant loader documentation.

## Build

### Build a single module

Use the module path in Gradle.

Examples

```sh
./gradlew :forge:mc_1_20_2:build
./gradlew :forge:mc_1_21_1:build
./gradlew :fabric:mc_1_21_1:build
```

To see available modules and tasks

```sh
./gradlew projects
```

### Build everything

Expected root task

```sh
./gradlew buildAll
```

If this task does not exist yet, the current equivalent is usually:

```sh
./gradlew build
```

In the target architecture, `buildAll` should explicitly build every supported version module.

### Collect jars

Expected root task

```sh
./gradlew collectJars
```

Expected behaviour
- Builds all supported targets
- Copies jars into a single folder, for example:
  - `dist/` or `build/dist/`

The dist folder should contain multiple jars without overwriting.

## Output locations

By default, Gradle outputs jars here:

- `<module>/build/libs/`

Example

- `forge/mc_1_20_2/build/libs/`
- `forge/mc_1_21_1/build/libs/`

The `collectJars` task exists so you do not need to browse module folders manually.

## Jar naming rules

Jar names must include both loader and Minecraft version.

Recommended format

- `happyhttp-<mod_version>-<loader>-mc<minecraft_version>.jar`

Examples

- `happyhttp-1.2.3-forge-mc1.20.2.jar`
- `happyhttp-1.2.3-forge-mc1.21.1.jar`
- `happyhttp-1.2.3-fabric-mc1.21.1.jar`

This prevents accidental overwrites and makes releases unambiguous.

## Local verification checklist

Before opening a PR, verify:

- If you changed one module: that module builds and launches in dev
- If you changed `common`: all modules build
- Jars are versioned correctly (name includes loader + MC version)
- docs/versions.md updated when a target status changes

Minimum smoke test (for a new target)
- Launch dev client
- Load a world
- Place and configure both blocks
- Receiver triggers redstone on request
- Sender sends a request on redstone input
- Config file generates correctly

## Release process (recommended)

### 1) Decide what you are releasing

A release should be explicit about:
- loader
- Minecraft version(s)
- mod version

Avoid “universal” jars unless you can prove they work across targets.

### 2) Update version and changelog

At minimum
- bump the mod version in the canonical version file (wherever the project keeps it)
- prepare release notes (GitHub Release description is acceptable early on)

Recommended
- maintain a `CHANGELOG.md` and keep it updated per release

### 3) Build release artifacts

Run

```sh
./gradlew clean buildAll collectJars
```

Verify
- dist folder contains exactly the jars you expect
- jar names include loader and MC version
- no jar overwrites occurred

### 4) Create a GitHub Release

- Tag the release (for example `v1.2.3`)
- Attach the jar(s) from the dist folder
- Include release notes:
  - supported loader and MC versions
  - highlights
  - known issues
  - links to key PRs and issues

GitHub Releases page
https://github.com/Narratimo/HappyHttpMod/releases

### 5) Update the supported versions matrix

After release:
- update [docs/versions.md](versions.md) status to reflect the release
- include the release link where helpful

## CI recommendations (optional but useful)

- Run `./gradlew buildAll` on every PR
- Upload jar artifacts for PR builds so testers can try them without building locally
- Keep a simple cache for Gradle dependencies to speed up CI

## Policy: do not commit built jars

Built `.jar` files should not be committed to the repo.

If you see jars committed at the root or elsewhere, remove them and add patterns to `.gitignore` so they do not return.
