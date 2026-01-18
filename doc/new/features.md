# Features Analysis: Happy HTTP Mod

## Implemented Features

### HTTP Receiver Block

**Status:** Fully Implemented

| Capability | Implementation |
|------------|----------------|
| Block placement | `HttpReceiverBlock.java` |
| Redstone output | Signal strength 15 when powered |
| Custom endpoint URL | Stored in block entity |
| GUI configuration | `HttpReceiverSettingsScreen.java` |
| HTTP POST handling | `HttpReceiverBlockHandler.java` |
| NBT persistence | Block entity save/load |

**Behavior:**
- Player right-clicks block (creative mode) → Opens settings screen
- Player configures endpoint URL (e.g., `/secret/door`)
- External system sends POST to `http://<server-ip>:8080/secret/door`
- Block toggles POWERED state and emits redstone signal

### HTTP Server

**Status:** Fully Implemented

| Capability | Implementation |
|------------|----------------|
| Embedded server | `com.sun.net.httpserver.HttpServer` |
| Port configuration | Default 8080, configurable in TOML |
| Handler registration | Queue system for deferred registration |
| Multiple endpoints | HashMap-based URL routing |

### Platform Support

| Platform | Status |
|----------|--------|
| Forge 1.20.2 | Complete |
| NeoForge | Skeleton (incomplete) |
| Fabric | Basic structure (incomplete) |

---

## Implemented in Dev Branch (Not Merged to Main)

### HTTP Sender Block

**Status:** Implemented in `dev` branch, NOT merged to `main`

**Implemented in dev branch:**
- `HttpSenderBlock.java` - Block definition
- `HttpSenderBlockEntity.java` - State persistence
- `HttpSenderSettingsScreen.java` - Configuration GUI
- `HttpClientImpl.java` - HTTP client for sending requests
- `SUpdateHttpSenderValuesPacket.java` - Network sync
- `CHttpSenderOpenGuiPacket.java` - GUI trigger packet

**Capabilities:**
- Send HTTP GET or POST on redstone signal
- Configurable destination URL
- Parameter mapping (key/value pairs)
- JSON body support for POST
- Query string for GET

### Parameter Matching

**Status:** Implemented in `dev` branch

**Dev branch implementation:**
- `ParameterReader.java` - Parameter parsing utilities
- `QueryBuilder.java` - Query string construction
- `EditBoxPair.java` - GUI widget for key/value pairs
- `ScrollableWidget.java` - Scrollable parameter list

### Power Modes

**Status:** Implemented in `dev` branch

**Dev branch implementation:**
- `EnumPoweredType.java` - Toggle vs Timer modes
- `EnumTimerUnit.java` - Ticks or seconds
- Enhanced block entity with mode storage

### Global Variables

**Status:** Implemented in `dev` branch

**Dev branch implementation:**
- `GlobalParam.java` - Global parameter model
- Config file support in `IHttpServerConfig.java`
- Parameter injection in handlers

### JSON Utilities

**Status:** Implemented in `dev` branch

**Dev branch implementation:**
- `JsonUtils.java` - JSON parsing and building
- `NBTConverter.java` - NBT to/from conversion

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

| Category | Main Branch | Dev Branch | Action Needed |
|----------|-------------|------------|---------------|
| HTTP Sender | Missing | Implemented | Merge dev |
| Parameters | Missing | Implemented | Merge dev |
| Power modes | Missing | Implemented | Merge dev |
| Global vars | Missing | Implemented | Merge dev |
| JSON utils | Missing | Implemented | Merge dev |
| Redirects | Missing | Missing | New implementation |
| NeoForge | Skeleton | Skeleton | Complete implementation |
| Fabric | Incomplete | Incomplete | Complete implementation |

## Implementation Priority

Based on gap analysis, recommended order:

1. **Merge `dev` to `main`** - Brings HTTP Sender, parameters, power modes, global vars
2. **NeoForge Completion** - Platform support
3. **Redirect Handling** - New implementation needed
4. **Fabric Completion** - Platform support
5. **Tier 1 Features** - Port binding, visual cues, testing, auth
