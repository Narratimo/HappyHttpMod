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
| **forge** | 1.20.2 | 17 | ✅ Active - Full features |
| **common** | 1.20.2 | 17 | ✅ Shared code for forge |
| **eira-core** | 1.21.1 | 21 | ✅ Eira Core library mod |
| **fabric** | 1.20.2 | 17 | ⏸️ Disabled - Incomplete |

## Quick Start

```bash
# Build all active modules
./gradlew build

# Build specific module
./gradlew :neoforge:build   # Eira Relay (MC 1.21.1)
./gradlew :eira-core:build  # Eira Core library
./gradlew :forge:build      # Eira Relay (MC 1.20.2)

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
│       ├── http/                # HTTP server, client, handlers
│       ├── network/             # Network packets
│       ├── client/gui/          # Configuration GUIs
│       ├── platform/            # Platform services, config, registry
│       ├── registry/            # ModBlocks, ModItems, ModBlockEntities
│       ├── enums/               # EnumHttpMethod, EnumPoweredType, EnumTimerUnit
│       ├── mixin/               # Client mixins
│       └── utils/               # JsonUtils, NBTConverter, QueryBuilder
├── eira-core/                   # Eira Core library mod (NeoForge 1.21.1 / Java 21)
│   └── src/main/java/org/eira/core/
│       ├── EiraCore.java        # Mod entry point
│       ├── api/                 # Public API (EiraAPI, EiraEventBus, events)
│       └── impl/                # Internal implementations
├── common/                      # MC 1.20.2 / Java 17 - Shared code (full features)
├── forge/                       # MC 1.20.2 / Java 17 - Full features
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
- Rate limiting (configurable, disabled by default)
- CORS support (configurable, disabled by default)

### HTTP Endpoints
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/trigger/{id}` | POST | Named triggers for QR codes/sensors |
| `/status` | GET | Health check with uptime, version |
| `/redstone` | POST | Emit redstone at coordinates |
| `/broadcast` | POST | Send chat/title/actionbar messages |

### Eira Core (Library Mod)
- Shared event bus for cross-mod communication
- Thread-safe publish/subscribe pattern
- Event types: HttpReceived, ExternalTrigger, RedstoneChange, ServerCommand, CheckpointCompleted
- Debug subscribers log events at DEBUG level for troubleshooting
- Eira Relay publishes events automatically when eira-core is present

## Key Files

### Eira Relay (neoforge/)
| File | Purpose |
|------|---------|
| `EiraRelay.java` | Mod entry point, event registration |
| `CommonClass.java` | HTTP server/client lifecycle |
| `Constants.java` | MOD_ID = "eirarelay", MOD_NAME = "Eira Relay" |
| `HttpServerImpl.java` | HTTP server with rate limiting, CORS |
| `HttpClientImpl.java` | HTTP client with retry support |
| `TriggerHandler.java` | /trigger/{id} endpoint |
| `StatusHandler.java` | /status health check |
| `RedstoneHandler.java` | /redstone emission |
| `BroadcastHandler.java` | /broadcast messages |
| `RateLimiter.java` | Per-IP rate limiting |

### Eira Core (eira-core/)
| File | Purpose |
|------|---------|
| `EiraCore.java` | Mod entry point |
| `EiraAPI.java` | Public API interface |
| `EiraEventBus.java` | Event bus interface |
| `SimpleEventBus.java` | Thread-safe event bus implementation |

## Build Output

```
neoforge/build/libs/Eira Relay-neoforge-1.21.1-1.1.0.jar
eira-core/build/libs/Eira Core-neoforge-1.21.1-1.1.0.jar
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

### Phase 6: Eira Server (Future)
- Create separate Node.js repository for game/checkpoint management
- REST API for game state, teams, and players

### Deferred
- Complete Fabric module

## Completed PRs

| PR# | Branch | Description | Status |
|-----|--------|-------------|--------|
| 53 | refactor/rename-eira-relay | Rename to Eira Relay | ✅ |
| 54 | feature/power-modes | Power modes (Toggle/Timer) | ✅ |
| 55 | refactor/rename-common-forge-fabric-packages | Rename packages in common/forge/fabric | ✅ |
| 56 | feature/port-modules-mc-1.21.1 | Enable multi-version builds | ✅ |
| 57 | feature/forge-http-sender | HTTP Sender for forge/common | ✅ |
| 58 | feature/forge-power-modes | Power modes for forge/common | ✅ |
| 59 | fix/common-translations | Fix translation file for forge | ✅ |
| 60 | feature/sender-power-modes | Power modes for HTTP Sender | ✅ |
| 61 | feature/global-variables | Global variables | ✅ |
| 62 | feature/port-binding-helper | Port/IP info in Receiver settings | ✅ |
| 63 | feature/inline-testing | Test button for HTTP Sender | ✅ |
| 64 | feature/norwegian-translations | Norwegian translations | ✅ |
| 65 | feature/auth-and-discord | Auth helpers, Discord integration, parameter editor | ✅ |
| 66 | feature/webhook-security | Secret token validation for HTTP Receiver | ✅ |
| 67 | feature/forge-new-endpoints | Port new endpoints and features to forge/common | ✅ |
| 68 | feature/visual-connection-cues | Visual connection cues (particles, active state) | ✅ |
| 69 | feature/auth-ux-improvements | Auth UX: masked fields, copy/generate buttons | ✅ |

## Recent Commits (Eira Ecosystem)

| Commit | Description |
|--------|-------------|
| 71d4ce6 | Phase 1-4: New endpoints, security, retry, eira-core module |
| 6a33035 | Phase 5: Integrate Eira Core event publishing |
| 241e167 | Add debug event subscribers to Eira Core |
| b6b60eb | Port new endpoints and features to forge/common |
