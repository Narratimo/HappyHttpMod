# CLAUDE.md - Eira Relay

## Project Overview

**Eira Relay** is a Minecraft mod that bridges the in-game world with external systems via HTTP webhooks. Maintained by [Eira](https://www.eira.no), a non-commercial organization teaching kids and teenagers to code.

## Current State (January 2026)

| Attribute | Value |
|-----------|-------|
| **Mod Name** | Eira Relay |
| **Mod ID** | `eirarelay` |
| **Package** | `no.eira.relay` |
| **Status** | Beta - actively maintained |

### Multi-Version Architecture

| Module | MC Version | Java | Status |
|--------|------------|------|--------|
| **neoforge** | 1.21.1 | 21 | Active - Full features |
| **neoforge-1.21.4** | 1.21.4 | 21 | Active - Full features |
| **forge** | 1.20.2 | 17 | Active - Missing recent features |
| **common** | 1.20.2 | 17 | Shared code for forge |
| **eira-core** | 1.21.1 | 21 | Eira Core library mod |
| **fabric** | 1.20.2 | 17 | Disabled - Incomplete |

## Quick Start

```bash
# Build all active modules
./gradlew build

# Build specific module
./gradlew :neoforge:build         # Eira Relay (MC 1.21.1)
./gradlew :neoforge-1.21.4:build  # Eira Relay (MC 1.21.4)
./gradlew :eira-core:build        # Eira Core library
./gradlew :forge:build            # Eira Relay (MC 1.20.2)

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
│       └── utils/               # JsonUtils, NBTConverter, QueryBuilder, PlayerDetector
├── neoforge-1.21.4/             # NeoForge 1.21.4 / Java 21 (ACTIVE)
│   └── src/main/java/no/eira/relay/  # Same structure as neoforge/
├── eira-core/                   # Eira Core library mod (NeoForge 1.21.1 / Java 21)
│   └── src/main/java/org/eira/core/
│       ├── EiraCore.java        # Mod entry point
│       ├── api/                 # Public API (EiraAPI, EiraEventBus, events, teams, players, stories, adventures)
│       └── impl/                # Internal implementations
├── common/                      # MC 1.20.2 / Java 17 - Shared code (full features)
├── forge/                       # MC 1.20.2 / Java 17 - Full features
├── fabric/                      # MC 1.20.2 / Java 17 - Disabled (incomplete)
└── docs/                        # Documentation
```

## Key Files

### Eira Relay (neoforge/)
| File | Purpose |
|------|---------|
| `EiraRelay.java` | Mod entry point, event registration |
| `CommonClass.java` | HTTP server/client lifecycle |
| `Constants.java` | MOD_ID = "eirarelay", MOD_NAME = "Eira Relay" |
| `HttpServerImpl.java` | HTTP server with rate limiting, CORS |
| `HttpClientImpl.java` | HTTP client with retry support |
| `HttpReceiverBlockHandler.java` | Custom endpoint handler with player detection |
| `PlayerDetector.java` | Find players near block positions |

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

---

## Completed PRs

| PR# | Branch | Description | Status |
|-----|--------|-------------|--------|
| 53 | refactor/rename-eira-relay | Rename to Eira Relay | Done |
| 54 | feature/power-modes | Power modes (Toggle/Timer) | Done |
| 55 | refactor/rename-common-forge-fabric-packages | Rename packages | Done |
| 56 | feature/port-modules-mc-1.21.1 | Multi-version builds | Done |
| 57 | feature/forge-http-sender | HTTP Sender for forge/common | Done |
| 58 | feature/forge-power-modes | Power modes for forge/common | Done |
| 59 | fix/common-translations | Fix translation file for forge | Done |
| 60 | feature/sender-power-modes | Power modes for HTTP Sender | Done |
| 61 | feature/global-variables | Global variables | Done |
| 62 | feature/port-binding-helper | Port/IP info in Receiver settings | Done |
| 63 | feature/inline-testing | Test button for HTTP Sender | Done |
| 64 | feature/norwegian-translations | Norwegian translations | Done |
| 65 | feature/auth-and-discord | Auth helpers, Discord integration | Done |
| 66 | feature/webhook-security | Secret token validation | Done |
| 67 | feature/forge-new-endpoints | Port new endpoints to forge | Done |
| 68 | feature/visual-connection-cues | Particles, active state | Done |
| 69 | feature/auth-ux-improvements | Masked fields, copy/generate | Done |
| 70 | docs/api-reference | API reference documentation | Done |
| 71 | feature/event-type-constants | Event TYPE string constants | Done |
| 72 | feature/eira-core-expansion | Teams/Players/Stories APIs | Done |
| 73 | feature/neoforge-1.21.4 | NeoForge 1.21.4 support | Done |
| 74 | feature/scene-sequencer | Scene Sequencer block | Done |
| 75 | docs/adventure-toolkit | Adventure toolkit docs | Done |
| 94 | feature/player-triggers | Player detection on triggers | Done |

---

## Next Session: Recommended PR Plan

### Phase 2 Features (High Priority - Core Functionality)

These features significantly improve the mod's usefulness and should be completed first.

#### PR #95: Response Variables (#76)
**Branch:** `feature/response-variables`
**Complexity:** High
**Files to create:**
- `neoforge/src/main/java/no/eira/relay/variables/VariableStorage.java` - Thread-safe storage for variables
- `neoforge/src/main/java/no/eira/relay/variables/JsonPathExtractor.java` - Extract values from JSON responses
- `neoforge/src/main/java/no/eira/relay/variables/ResponseCapture.java` - Configuration for what to capture
- `neoforge/src/main/java/no/eira/relay/variables/VariableSubstitutor.java` - Replace `{{variable}}` placeholders

**Files to modify:**
- `HttpSenderBlockEntity.java` - Add response capture settings
- `HttpClientImpl.java` - Return response body for capture
- `HttpSenderSettingsScreen.java` - Add capture configuration UI
- Translation files

**Implementation:**
1. Create `VariableStorage` singleton with global/block/player scopes
2. Create `JsonPathExtractor` using Gson for dot-notation paths (e.g., `data.user.name`)
3. Add `ResponseCapture` configuration to sender block entity
4. Modify HTTP client to return response body
5. Add GUI for configuring capture rules
6. Implement variable substitution in URL and parameters

---

#### PR #96: Comparator Output (#77)
**Branch:** `feature/comparator-output`
**Complexity:** Medium
**Files to create:**
- `neoforge/src/main/java/no/eira/relay/http/api/HttpResult.java` - Record with statusCode + body

**Files to modify:**
- `HttpSenderBlockEntity.java` - Track last status code
- `HttpSenderBlock.java` - Add `hasAnalogOutputSignal()`, `getAnalogOutputSignal()`
- `HttpReceiverBlock.java` - Add comparator output methods
- `IHttpClient.java` - Add methods returning HttpResult

**Signal mapping:**
- 15 = Success (2xx)
- 10 = Redirect (3xx)
- 5 = Client error (4xx)
- 2 = Server error (5xx)
- 0 = No response/timeout

---

#### PR #97: Request Templates (#81)
**Branch:** `feature/request-templates`
**Complexity:** Medium
**Files to create:**
- `neoforge/src/main/java/no/eira/relay/templates/RequestTemplate.java` - Template model
- `neoforge/src/main/java/no/eira/relay/templates/RequestTemplateRegistry.java` - Built-in templates

**Files to modify:**
- `HttpSenderSettingsScreen.java` - Replace Discord button with Template button
- Translation files

**Built-in templates:**
1. Discord Webhook
2. Slack Webhook
3. IFTTT Webhooks
4. Home Assistant
5. Pushover
6. ntfy.sh
7. Node-RED
8. Generic REST API

---

#### PR #98: Visual Wiring Preview (#79)
**Branch:** `feature/visual-wiring`
**Complexity:** High (rendering code)
**Files to create:**
- `neoforge/src/main/java/no/eira/relay/client/render/WiringOverlayRenderer.java`
- `neoforge/src/main/java/no/eira/relay/client/debug/HttpBlockDebugInfo.java`

**Files to modify:**
- `EiraRelay.java` - Register render events
- Client mixin for debug overlay toggle

**Features:**
- F3+H toggle for debug overlay
- Lines between related blocks
- Status indicators (last request time, status code)
- Endpoint URL display

---

### Phase 3 Features (Medium Priority - Enhanced Functionality)

#### PR #99: WebSocket Support (#80)
**Branch:** `feature/websocket`
**Complexity:** High
**Description:** Add WebSocket client capability to HTTP Sender for real-time communication

---

#### PR #100: Scheduled Requests (#82)
**Branch:** `feature/scheduled-requests`
**Complexity:** Medium
**Description:** Add timer-based periodic HTTP requests (e.g., every 5 minutes)

---

#### PR #101: Data Flow in Sequences (#83)
**Branch:** `feature/sequence-data-flow`
**Complexity:** Medium
**Description:** Allow Scene Sequencer steps to pass response data to subsequent steps

---

### Recommended Order

| Order | PR | Issue | Rationale |
|-------|-----|-------|-----------|
| 1 | #95 | #76 | Response variables enable many other features |
| 2 | #96 | #77 | Comparator output is simple and highly useful |
| 3 | #97 | #81 | Templates improve UX significantly |
| 4 | #98 | #79 | Visual debugging helps users understand the system |
| 5 | #99 | #80 | WebSocket enables real-time use cases |
| 6 | #100 | #82 | Scheduled requests enable polling scenarios |

---

## Tech Debt to Address

| Item | Priority | Description |
|------|----------|-------------|
| Forge parity | High | Port player detection, Scene Sequencer to forge module |
| Test coverage | High | Add JUnit tests for core functionality |
| Javadoc | Low | Add missing documentation to public APIs |
| GUI refactor | Medium | Extract common GUI code to base class |
| Error handling | Medium | Improve HTTP error messages and logging |

---

## Session Continuation Guide

When continuing work on this project:

1. **Check current branch:** `git status`
2. **Pull latest changes:** `git pull origin main`
3. **Create feature branch:** `git checkout -b feature/xxx`
4. **Read relevant files** before making changes
5. **Build after each change:** `./gradlew :neoforge:build`
6. **Commit with descriptive message** including `Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>`
7. **Create PR** using `gh pr create`

### Common Patterns

**Adding a new block setting:**
1. Add field to `Values` inner class in block entity
2. Update `writeValues()` and `readBuffer()` for network sync
3. Update `loadAdditional()` and `saveAdditional()` for NBT persistence
4. Add GUI control in settings screen
5. Update translations

**Adding a new endpoint:**
1. Create handler class implementing `IHttpHandler`
2. Register in `CommonClass.onServerStarting()`
3. Document in README

**Adding a new utility class:**
1. Create in `utils/` package
2. Keep it stateless if possible
3. Add Javadoc for public methods
