# docs/repo-layout.md

This document defines the intended repository layout for Happy HTTP.

It exists so contributors can cooperate without stepping on each other, and so refactors for multi-version and multi-loader support stay consistent.

## Design goals

- Multi-version support without separate repositories
- Multi-loader support without duplicating core logic
- Clear module boundaries so ports are mechanical
- One module produces one jar

## Top-level layout

Expected top-level folders:

```text
common/
forge/
neoforge/
fabric/
docs/
.github/
```

Additional folders may exist, but these define the supported structure.

## The common module

`common/` contains shared logic.

Rules
- `common/` must not depend on Minecraft classes or loader APIs
- No Forge/Fabric/NeoForge imports
- No mixins or loader-specific event hooks
- Keep it stable and reusable

Good candidates for `common/`
- Data models (block configuration models)
- Parsing and validation utilities
- HTTP request construction and response parsing
- Shared domain logic (what should happen when a request matches)

## Loader folders

Each loader folder contains one or more Minecraft version modules.

### Forge layout

```text
forge/
  mc_1_20_2/
  mc_1_21_1/
```

### NeoForge layout

```text
neoforge/
  mc_1_21_1/
```

### Fabric layout

```text
fabric/
  mc_1_21_1/
```

Rules
- Each `mc_<version>/` folder is a Gradle module
- Each module builds exactly one jar
- Module code should be mostly glue and integration

## Version modules

A version module contains:

- Mod entrypoint and lifecycle integration
- Block and item registration
- Block entities, networking, UI registration
- Resources for that target (assets, data, metadata)
- Mixins for that specific target (if used)

Rules
- Keep code thin
- Put reusable logic into `common/` where possible
- Avoid creating cross-dependencies between version modules

## Resources policy

Resources are often the hardest part to share.

Guidelines
- Prefer keeping resources per module to avoid accidental conflicts
- If resources are identical across modules, consider a shared resources strategy, but document it clearly because it can create confusing build behaviour
- Never rely on manual copying as the long-term process

If you introduce shared resources, document:
- where they live
- how they get included in each module jar
- how conflicts are resolved

## Root Gradle tasks

The root project should provide:

- `buildAll` to build all supported modules
- `collectJars` to collect all module jars into one folder (for example `dist/`)

This removes friction for contributors and makes CI straightforward.

## Naming conventions

### Module names

- `mc_1_20_2` not `1.20.2` to keep Gradle happy
- Use consistent naming across loaders

### Jar names

Jar names must include loader and Minecraft version.

Recommended
- `happyhttp-<mod_version>-<loader>-mc<minecraft_version>.jar`

## Where docs live

- Project docs: `docs/`
- Contributor workflow: `CONTRIBUTING.md`, `GOVERNANCE.md`, `MAINTAINERS.md`
- Security: `SECURITY.md`

Docs should be treated as part of the product. If you change structure, update docs in the same PR.

## Quick links

- Support matrix: `docs/versions.md`
- Porting guide: `docs/porting-guide.md`
- Testing: `docs/testing.md`
- Build and release: `docs/build-and-release.md`
