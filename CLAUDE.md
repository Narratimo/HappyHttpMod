# CLAUDE.md - Eira Relay

## Project Overview

**Eira Relay** is a Minecraft mod that bridges the in-game world with external systems via HTTP webhooks. Maintained by [Eira](https://www.eira.no), a non-commercial organization teaching kids and teenagers to code.

## Current State (January 2026)

| Attribute | Value |
|-----------|-------|
| **Mod Name** | Eira Relay |
| **Mod ID** | `eirarelay` |
| **Package** | `no.eira.relay` |
| **Active Platform** | NeoForge 1.21.1 |
| **Java Version** | 21 |
| **Status** | ✅ Complete - Both blocks functional |

## Quick Start

```bash
# Build NeoForge module
./gradlew :neoforge:build

# Run client
./gradlew :neoforge:runClient
```

## Project Structure

```
EiraRelay/
├── neoforge/                    # NeoForge 1.21.1 (ACTIVE)
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
├── common/                      # Disabled (MC 1.20.2) - no.eira.relay
├── forge/                       # Disabled (MC 1.20.2) - no.eira.relay
├── fabric/                      # Disabled (MC 1.20.2) - no.eira.relay
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

## Next Action: Port common/forge/fabric to MC 1.21.1

Now that all modules use `no.eira.relay` package, the next step is to port common/forge/fabric to MC 1.21.1:
1. Update gradle.properties for MC 1.21.1 / Java 21
2. Update Forge/Fabric API dependencies
3. Re-enable modules in settings.gradle
4. Port code to match neoforge (HTTP Sender, power modes, utilities)

## Completed PRs

| PR# | Branch | Description | Status |
|-----|--------|-------------|--------|
| 53 | refactor/rename-eira-relay | Rename to Eira Relay | ✅ |
| 54 | feature/power-modes | Power modes (Toggle/Timer) | ✅ |
| 55 | refactor/rename-common-forge-fabric-packages | Rename packages in common/forge/fabric | ✅ |
