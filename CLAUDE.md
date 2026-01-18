# Eira Relay - Project Guide

> **Rebranding in progress:** The mod is being renamed from "Happy HTTP" / "HttpAutomator" to "Eira Relay"
> Website: www.eira.no

## Workflow Rules

- **Always use PRs** - All changes must go through pull requests
- **Document immediately** - Update all relevant docs right after any change
- **Best practice branching** - Use descriptive feature branch names (e.g., `doc/add-feature`, `fix/bug-name`, `feature/new-capability`)
- **Small tasks only** - Break work into small, reviewable PRs
- **Plan first** - Define PR scope before starting implementation

## Project Overview

**Eira Relay** (formerly Happy HTTP Mod) bridges Minecraft with external systems via HTTP webhooks. It provides:
- **HTTP Receiver Block** - Triggers redstone signals from incoming webhooks
- **HTTP Sender Block** - Sends HTTP requests on redstone signals

**Note:** HTTP Sender is implemented in `dev` branch but not yet merged to `main`.

**Target Audience:** Adventure map creators, educators teaching HTTP/web concepts, smart home integrators.

## Key Directories

```
common/     - Shared code across all platforms
forge/      - Forge-specific implementation (functional)
fabric/     - Fabric implementation (incomplete)
neoforge/   - NeoForge implementation (skeleton only)
doc/       - Existing documentation
doc/new/   - Analysis documentation
```

## Build Commands

```bash
./gradlew build              # Build all platforms
./gradlew :forge:build       # Build Forge only
./gradlew :forge:runClient   # Run dev client
./gradlew :forge:runServer   # Run dev server
```

## Key Source Files

| File | Purpose |
|------|---------|
| `common/.../CommonClass.java` | Main entry point, HTTP server lifecycle |
| `common/.../http/HttpServerImpl.java` | HTTP server implementation |
| `common/.../http/HttpReceiverBlockHandler.java` | Handles incoming webhooks |
| `common/.../block/HttpReceiverBlock.java` | Receiver block logic |
| `common/.../blockentity/HttpReceiverBlockEntity.java` | Block entity with URL config |
| `common/.../client/gui/HttpReceiverSettingsScreen.java` | Configuration GUI |
| `gradle.properties` | Version configuration |

## Current State

| Platform | Version | Status |
|----------|---------|--------|
| Minecraft | 1.20.2 | Supported |
| Forge | 48.0.49 | Functional |
| NeoForge | 20.2.86 | Skeleton only |
| Fabric | 0.91.0+1.20.2 | Incomplete |

## Branch Status

| Branch | Status | Content |
|--------|--------|---------|
| `main` | Stable but incomplete | HTTP Receiver only |
| `dev` | Feature-complete | HTTP Sender, global params, enhancements |
| `feenixnet` | Documentation | Working version docs |

**Priority:** Merge `dev` to `main` to complete core functionality.

## Technical Debt Summary

- **NeoForge module:** Only skeleton, missing event handlers and network layer
- **HTTP Sender Block:** Documented but not implemented
- **Forge-specific APIs:** `FMLJavaModLoadingContext`, `SimpleChannel` need abstraction for multi-platform
- **Mixin config:** NeoForge has wrong package name in mixins.json

## Documentation

- `doc/features.md` - Feature specifications with prioritization
- `doc/architecture.md` - Technical architecture
- `doc/USE_CASES.md` - Real-world usage examples
- `doc/ARCHITECTURE_SECURITY_REVIEW.md` - Security analysis
- `doc/new/` - Analysis documentation (this project)

## Quick Reference

- **Package:** `com.clapter.httpautomator`
- **Mod ID:** `httpautomator`
- **Default HTTP Port:** 8080
- **Config File:** `happyhttp-common.toml`
