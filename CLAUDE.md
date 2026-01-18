# CLAUDE.md - Eira Relay

## Project Overview

**Eira Relay** is a Minecraft mod that bridges the in-game world with external systems via HTTP webhooks. Maintained by [Eira](https://www.eira.no), a non-commercial organization teaching kids and teenagers to code.

## Current State (January 2026)

| Attribute | Value |
|-----------|-------|
| **Mod Name** | Eira Relay |
| **Mod ID** | `eirarelay` |
| **Package** | `no.eira.relay` |
| **Status** | ✅ Multi-version support |

### Multi-Version Architecture

| Module | MC Version | Java | Status |
|--------|------------|------|--------|
| **neoforge** | 1.21.1 | 21 | ✅ Active - Full features |
| **forge** | 1.20.2 | 17 | ✅ Active - HTTP Receiver only |
| **common** | 1.20.2 | 17 | ✅ Shared code for forge |
| **fabric** | 1.20.2 | 17 | ⏸️ Disabled - Incomplete |

## Quick Start

```bash
# Build all active modules
./gradlew build

# Build specific module
./gradlew :neoforge:build   # MC 1.21.1
./gradlew :forge:build      # MC 1.20.2

# Run client
./gradlew :neoforge:runClient
```

## Project Structure

```
EiraRelay/
├── neoforge/                    # NeoForge 1.21.1 / Java 21 (ACTIVE)
│   └── src/main/java/no/eira/relay/
│       ├── EiraRelay.java       # Mod entry point
│       ├── CommonClass.java     # HTTP server/client lifecycle
│       ├── Constants.java       # MOD_ID, MOD_NAME
│       ├── block/               # HttpReceiverBlock, HttpSenderBlock
│       ├── blockentity/         # Block entity implementations
│       ├── http/                # HTTP server and client
│       ├── network/             # Network packets
│       ├── client/gui/          # Configuration GUIs
│       ├── platform/            # Platform services, config, registry
│       ├── registry/            # ModBlocks, ModItems, ModBlockEntities
│       ├── enums/               # EnumHttpMethod, EnumPoweredType, EnumTimerUnit
│       ├── mixin/               # Client mixins
│       └── utils/               # JsonUtils, NBTConverter, QueryBuilder
├── common/                      # MC 1.20.2 / Java 17 - Shared code
├── forge/                       # MC 1.20.2 / Java 17 - HTTP Receiver only
├── fabric/                      # MC 1.20.2 / Java 17 - Disabled (incomplete)
└── doc/                         # Documentation
```

## Core Features

### HTTP Receiver Block
- Listens for incoming HTTP POST requests
- Emits redstone signal (strength 15) when triggered
- Configurable endpoint URL
- Handler cleanup on block removal

### HTTP Sender Block
- Sends HTTP GET/POST requests on redstone signal
- Configurable URL, method, and parameters
- JSON body for POST, query string for GET

### HTTP Server
- Embedded `com.sun.net.httpserver.HttpServer`
- Binds to localhost (127.0.0.1) by default for security
- Configurable port (default: 8080)
- Handler registration with URL mapping

## Key Files

| File | Purpose |
|------|---------|
| `EiraRelay.java` | Mod entry point, event registration |
| `CommonClass.java` | HTTP server/client lifecycle |
| `Constants.java` | MOD_ID = "eirarelay", MOD_NAME = "Eira Relay" |
| `HttpServerImpl.java` | HTTP server implementation |
| `HttpClientImpl.java` | HTTP client for requests |
| `HttpReceiverBlock.java` | Receiver block behavior |
| `HttpSenderBlock.java` | Sender block behavior |

## Build Output

```
neoforge/build/libs/Eira Relay-neoforge-1.21.1-1.1.0.jar
```

## Workflow Rules

1. **Always use PRs** - All changes go through pull requests
2. **Document first** - Update docs with each code change
3. **Small tasks only** - Each PR does one thing well
4. **Test builds** - Verify `./gradlew :neoforge:build` passes

## Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Stable releases |
| `feature/*` | New features |
| `fix/*` | Bug fixes |
| `refactor/*` | Code refactoring |
| `docs/*` | Documentation updates |

## Next Actions

1. **Port HTTP Sender to forge** - Add HttpSenderBlock to MC 1.20.2 forge module
2. **Port power modes to forge** - Add EnumPoweredType, EnumTimerUnit to forge
3. **Complete fabric module** - Finish fabric implementation for MC 1.20.2

## Completed PRs

| PR# | Branch | Description | Status |
|-----|--------|-------------|--------|
| 53 | refactor/rename-eira-relay | Rename to Eira Relay | ✅ |
| 54 | feature/power-modes | Power modes (Toggle/Timer) | ✅ |
| 55 | refactor/rename-common-forge-fabric-packages | Rename packages in common/forge/fabric | ✅ |
| 56 | feature/multi-version-support | Enable multi-version builds | 🔄 In Progress |
