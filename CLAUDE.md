# Eira Relay - Project Guide

> **Rebranding in progress:** The mod is being renamed from "Happy HTTP" / "HttpAutomator" to "Eira Relay"
> Website: www.eira.no
> Package rename: `com.clapter.httpautomator` → `no.eira.relay`

## Workflow Rules

- **Always use PRs** - All changes must go through pull requests
- **Document immediately** - Update all relevant docs right after any change
- **Best practice branching** - Use descriptive feature branch names (e.g., `doc/add-feature`, `fix/bug-name`, `feature/new-capability`)
- **Small tasks only** - Break work into small, reviewable PRs
- **Plan first** - Define PR scope before starting implementation

## Project Overview

**Eira Relay** (formerly Happy HTTP Mod) bridges Minecraft with external systems via HTTP webhooks. It provides:
- **HTTP Receiver Block** - Triggers redstone signals from incoming webhooks
- **HTTP Sender Block** - Sends HTTP requests on redstone signals (GET/POST)

**Target Audience:** Adventure map creators, educators teaching HTTP/web concepts, smart home integrators.

## Current State (January 2026)

| Platform | MC Version | Status |
|----------|------------|--------|
| **NeoForge** | **1.21.1** | **Active - Both blocks working** |
| Forge | 1.20.2 | Disabled (code exists but not compiled) |
| Fabric | - | Incomplete |

**Active Development:** NeoForge 1.21.1 module only. Common/Forge modules are disabled in settings.gradle.

## Key Directories

```
neoforge/   - NeoForge 1.21.1 implementation (ACTIVE)
common/     - Shared code for MC 1.20.2 (DISABLED)
forge/      - Forge 1.20.2 implementation (DISABLED)
fabric/     - Fabric implementation (incomplete)
doc/        - Existing documentation
doc/new/    - Analysis documentation
```

## Build Commands

```bash
./gradlew build                 # Build NeoForge module
./gradlew :neoforge:build       # Build NeoForge only
./gradlew :neoforge:runClient   # Run dev client
./gradlew :neoforge:runServer   # Run dev server
```

## Key Source Files (NeoForge)

| File | Purpose |
|------|---------|
| `neoforge/.../HttpAutomator.java` | Main mod entry point |
| `neoforge/.../CommonClass.java` | HTTP server/client lifecycle |
| `neoforge/.../block/HttpReceiverBlock.java` | Receiver block (webhook → redstone) |
| `neoforge/.../block/HttpSenderBlock.java` | Sender block (redstone → HTTP) |
| `neoforge/.../http/HttpServerImpl.java` | HTTP server for receivers |
| `neoforge/.../http/HttpClientImpl.java` | HTTP client for senders |
| `neoforge/.../client/gui/*.java` | Configuration GUIs |
| `gradle.properties` | Version configuration |

## PR Branches Ready for Review

| Branch | Description |
|--------|-------------|
| `merge/dev-to-main` | Original dev branch merge (MC 1.20.2 code) |
| `merge/feenixnet-to-main` | NeoForge 1.21 port |
| `docs/add-claude-md-and-analysis-structure` | Documentation updates |
| `fix/neoforge-mixin-config` | Fix mixin package name |
| `feature/neoforge-http-sender` | HTTP Sender for NeoForge 1.21.1 |
| `fix/handler-cleanup-on-remove` | Memory leak fix |
| `fix/default-localhost-binding` | Security: bind to localhost |

## Remaining Work

### PR #8: Rename to Eira Relay
- mod_id: `httpautomator` → `eirarelay`
- mod_name: `HttpAutomator` → `Eira Relay`
- Package: `com.clapter.httpautomator` → `no.eira.relay`
- Assets: `assets/httpautomator/` → `assets/eirarelay/`

## Technical Details

- **Package:** `com.clapter.httpautomator` (will become `no.eira.relay`)
- **Mod ID:** `httpautomator` (will become `eirarelay`)
- **Default HTTP Port:** 8080 (configurable)
- **Default Bind Address:** 127.0.0.1 (localhost only for security)

## Documentation

- `doc/features.md` - Feature specifications
- `doc/architecture.md` - Technical architecture
- `doc/USE_CASES.md` - Real-world usage examples
- `doc/ARCHITECTURE_SECURITY_REVIEW.md` - Security analysis
- `doc/new/` - Analysis documentation
  - `analysis.md` - Gap analysis
  - `architecture.md` - Architecture details
  - `features.md` - Feature status
  - `tasks.md` - Task tracking
  - `technical-debt.md` - Debt resolution
  - `backlog.md` - Feature backlog
