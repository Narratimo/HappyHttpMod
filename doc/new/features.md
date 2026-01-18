# Features Analysis: Eira Relay

> **Rebranding:** The mod is being renamed from "Happy HTTP" / "HttpAutomator" to "Eira Relay" (www.eira.no)

## Implemented Features

### HTTP Receiver Block

**Status:** ✅ Fully Implemented (NeoForge 1.21.1)

| Capability | Implementation |
|------------|----------------|
| Block placement | `HttpReceiverBlock.java` |
| Redstone output | Signal strength 15 when powered |
| Custom endpoint URL | Stored in block entity |
| GUI configuration | `HttpReceiverSettingsScreen.java` |
| HTTP POST handling | `HttpReceiverBlockHandler.java` |
| NBT persistence | Block entity save/load |
| Handler cleanup | Unregisters on block removal ✅ Fixed |

**Behavior:**
- Player right-clicks block (creative mode) → Opens settings screen
- Player configures endpoint URL (e.g., `/secret/door`)
- External system sends POST to `http://localhost:8080/secret/door`
- Block toggles POWERED state and emits redstone signal

### HTTP Sender Block

**Status:** ✅ Fully Implemented (NeoForge 1.21.1) - Ported via PR #5

| Capability | Implementation |
|------------|----------------|
| Block placement | `HttpSenderBlock.java` |
| HTTP GET/POST | Configurable via GUI |
| Custom URL | Stored in block entity |
| Parameter mapping | Key/value pairs with JSON serialization |
| GUI configuration | `HttpSenderSettingsScreen.java` |
| NBT persistence | Block entity save/load |
| Handler cleanup | Unregisters on block removal ✅ Fixed |

**Behavior:**
- Player right-clicks block (creative mode) → Opens settings screen
- Player configures URL, parameters, and HTTP method
- When block receives redstone signal → Sends HTTP request
- GET: Parameters as query string
- POST: Parameters as JSON body

### HTTP Server

**Status:** ✅ Fully Implemented (NeoForge 1.21.1)

| Capability | Implementation |
|------------|----------------|
| Embedded server | `com.sun.net.httpserver.HttpServer` |
| Port configuration | Default 8080, configurable in TOML |
| Localhost binding | Default 127.0.0.1 for security ✅ Fixed |
| Handler registration | Queue system for deferred registration |
| Handler cleanup | unregisterHandler() method ✅ Fixed |
| Multiple endpoints | HashMap-based URL routing |

### HTTP Client

**Status:** ✅ Fully Implemented (NeoForge 1.21.1) - Added via PR #5

| Capability | Implementation |
|------------|----------------|
| HTTP Client | `java.net.http.HttpClient` |
| GET requests | Query string parameters |
| POST requests | JSON body |
| Async execution | Non-blocking requests |

### Platform Support

| Platform | Status |
|----------|--------|
| NeoForge 1.21.1 | ✅ Complete (both blocks) |
| Forge 1.20.2 | Disabled (code exists) |
| Fabric | Basic structure (incomplete) |

---

## Merged from Dev Branch ✅

The following features have been merged from the dev branch (MC 1.20.2) and ported to NeoForge 1.21.1:

### HTTP Sender Block ✅ Ported to NeoForge 1.21.1

**Status:** ✅ Complete (PR #5: feature/neoforge-http-sender)

**Files ported:**
- `HttpSenderBlock.java` - Block definition
- `HttpSenderBlockEntity.java` - State persistence (uses JSON for parameter map)
- `HttpSenderSettingsScreen.java` - Configuration GUI
- `HttpClientImpl.java` / `IHttpClient.java` - HTTP client for sending requests
- `SUpdateHttpSenderValuesPacket.java` - Network sync
- `CHttpSenderOpenGuiPacket.java` - GUI trigger packet

### Parameter Utilities ✅ Ported to NeoForge 1.21.1

**Status:** ✅ Complete (PR #5)

**Files ported:**
- `QueryBuilder.java` - Query string construction
- `JsonUtils.java` - JSON parsing and parameter serialization
- `NBTConverter.java` - NBT to/from conversion
- `EnumHttpMethod.java` - GET/POST enum

### Features Not Yet Ported (Backlog)

The following features exist in the dev branch (MC 1.20.2) but have not been ported to NeoForge 1.21.1:

| Feature | Status | Notes |
|---------|--------|-------|
| Power Modes (Toggle/Timer) | Backlog | EnumPoweredType, EnumTimerUnit |
| Global Variables | Backlog | GlobalParam, config support |
| Parameter Editing Widgets | Backlog | EditBoxPair, ScrollableWidget |

---

## Not Yet Implemented

### Redirect Handling

**Status:** Not implemented in any branch

**Documented Capabilities:**
- HTTP 308 redirect after processing
- Configurable redirect URL
- Global missing parameter redirect

---

## Planned Features (from docs/features.md)

### Tier 1: Setup & Support

| ID | Feature | Priority |
|----|---------|----------|
| #37 | Port binding helper | High |
| #26 | Visual connection cues | High |
| #27 | Inline testing & per-block logs | High |
| #31 | Authentication helpers | High |

### Tier 2: Builder Experience

| ID | Feature | Priority |
|----|---------|----------|
| #25 | Reusable configuration presets | Medium |
| #28 | Expand Receiver output modes | Medium |
| #32 | Per-block rate limiting | Medium |
| #33 | Compact status indicators | Medium |
| #36 | Adventure map toolkit | Medium |

### Tier 3: Advanced Mechanics

| ID | Feature | Priority |
|----|---------|----------|
| #34 | Receiver payload mapping | Low |
| #35 | Sender outcomes | Low |
| #30 | Scene Sequencer block | Low |
| #29 | HTTP Filter block | Low |

---

## Feature Gap Summary

| Category | NeoForge 1.21.1 | Dev Branch (1.20.2) | Status |
|----------|-----------------|---------------------|--------|
| HTTP Receiver | ✅ Complete | ✅ Complete | Done |
| HTTP Sender | ✅ Complete | ✅ Complete | Ported (PR #5) |
| HTTP Server | ✅ Complete | ✅ Complete | Done |
| HTTP Client | ✅ Complete | ✅ Complete | Ported (PR #5) |
| Parameters | ✅ Basic | ✅ Advanced | Basic ported |
| Power modes | ❌ Not ported | ✅ Implemented | Backlog |
| Global vars | ❌ Not ported | ✅ Implemented | Backlog |
| JSON utils | ✅ Complete | ✅ Complete | Ported (PR #5) |
| Handler cleanup | ✅ Fixed | ❌ Missing | Fixed (PR #6) |
| Localhost binding | ✅ Fixed | ❌ Missing | Fixed (PR #7) |
| Fabric | ❌ Incomplete | ❌ Incomplete | Backlog |

## Implementation Priority

Based on current state, recommended order:

1. ✅ **Merge `dev` to `main`** - Complete (PR #1)
2. ✅ **Merge `feenixnet` to `main`** - Complete (PR #2)
3. ✅ **Port HTTP Sender to NeoForge 1.21.1** - Complete (PR #5)
4. ✅ **Fix security & memory leaks** - Complete (PRs #6, #7)
5. **Rename to Eira Relay** - Pending (PR #8)
6. **Port remaining features** - Backlog (power modes, global vars)
7. **Fabric Completion** - Backlog
8. **Tier 1 Features** - Backlog (port binding helper, visual cues, testing, auth)
