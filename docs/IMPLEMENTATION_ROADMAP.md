# Eira Relay - Implementation Roadmap

**Last updated:** January 2026

---

## Current State (Implemented)

### Core Features (Complete)

| Feature | Status | PR | Notes |
|---------|--------|-----|-------|
| HTTP Server | Done | - | Using `com.sun.net.httpserver` |
| HTTP Client | Done | - | POST/GET with headers, retry logic |
| HTTP Receiver Block | Done | - | Emits redstone on trigger |
| HTTP Sender Block | Done | - | Sends HTTP on redstone |
| Secret Token Auth | Done | #66 | Bearer header or query param |
| Power Modes | Done | #54, #60 | Switch/Timer for both blocks |
| Parameter Editor | Done | #65 | Key/value pairs for JSON body |
| Auth Types | Done | #65 | None/Bearer/Basic/Custom headers |
| Translations | Done | #64 | English + Norwegian |
| Visual Feedback | Done | #68 | Particles and active glow state |
| Test Button | Done | #63 | Verify sender config in-game |
| Discord Preset | Done | #65 | One-click Discord webhook setup |

### HTTP Endpoints (Complete)

| Endpoint | Method | Status | PR |
|----------|--------|--------|-----|
| `/trigger/{id}` | POST | Done | #67 |
| `/status` | GET | Done | #67 |
| `/redstone` | POST | Done | #67 |
| `/broadcast` | POST | Done | #67 |
| `/{custom}` | POST | Done | - |

### Infrastructure (Complete)

| Feature | Status | PR | Notes |
|---------|--------|-----|-------|
| Rate Limiting | Done | #67 | Per-IP, configurable |
| CORS Support | Done | #67 | Configurable origins |
| Eira Core Integration | Done | #71, #72 | Event bus, Teams/Players APIs |
| NeoForge 1.21.4 | Done | #73 | Multi-version support |
| Scene Sequencer | Done | #74 | Chain HTTP calls with delays |
| Player Detection | Done | #94 | Detect nearby players on trigger |

---

## Phase 2: Advanced Features (In Progress)

### Response Variables (#76)
**Priority:** High
**Status:** Not started

Parse API response data and store for use in subsequent requests.

**Implementation:**
- `VariableStorage` - Thread-safe singleton for variable storage
- `JsonPathExtractor` - Extract values using dot notation (e.g., `data.user.name`)
- `ResponseCapture` - Configure what to capture from responses
- `VariableSubstitutor` - Replace `{{variable}}` placeholders in URLs/params

**Scopes:**
- Global - Available everywhere
- Block - Per block entity
- Player - Per player (for player-specific triggers)

---

### Comparator Output (#77)
**Priority:** High
**Status:** Not started

Analog redstone signal based on HTTP response status.

**Signal Mapping:**
| Status | Signal | Description |
|--------|--------|-------------|
| 2xx | 15 | Success |
| 3xx | 10 | Redirect |
| 4xx | 5 | Client error |
| 5xx | 2 | Server error |
| Timeout | 0 | No response |

**Implementation:**
- Add `HttpResult` record with statusCode + body
- Track `lastStatusCode` in block entity
- Implement `hasAnalogOutputSignal()` and `getAnalogOutputSignal()`

---

### Request Templates (#81)
**Priority:** Medium
**Status:** Not started

Save and reuse block configurations as presets.

**Built-in Templates:**
1. Discord Webhook
2. Slack Webhook
3. IFTTT Webhooks
4. Home Assistant
5. Pushover
6. ntfy.sh
7. Node-RED
8. Generic REST API

**Implementation:**
- `RequestTemplate` model class with Builder pattern
- `RequestTemplateRegistry` for built-in templates
- Template button replacing Discord button in GUI

---

### Visual Wiring Preview (#79)
**Priority:** Medium
**Status:** Not started

Debug overlay showing HTTP block connections and status.

**Features:**
- F3+H toggle for debug overlay
- Lines between related blocks
- Last request time and status code display
- Endpoint URL labels

---

## Phase 3: Enhanced Functionality (Planned)

| Feature | Issue | Priority | Description |
|---------|-------|----------|-------------|
| WebSocket Support | #80 | High | Real-time bidirectional communication |
| Scheduled Requests | #82 | High | Time-based periodic HTTP triggers |
| Data Flow in Sequences | #83 | Medium | Pass response data between Scene Sequencer steps |
| Conditional Logic Block | #84 | Medium | API-driven redstone decisions |
| Batch/Multicast Sender | #85 | Medium | Send to multiple endpoints |
| Audio Integration | #86 | Low | Play sounds from HTTP triggers |

---

## Phase 4: Platform Features (Future)

| Feature | Issue | Description |
|---------|-------|-------------|
| Mobile Companion App | #87 | Trigger endpoints from phone |
| Eira Hub Cloud Relay | #88 | Easy internet access without port forwarding |
| Integration Marketplace | #89 | Community-shared presets |
| Visual Flow Builder | #90 | Drag-and-drop sequence designer |

---

## Not Started / Deferred

| Feature | Issue | Status | Notes |
|---------|-------|--------|-------|
| Redstone Patterns | - | Deferred | PULSE, FADE, SOS patterns |
| Multiple API Keys | - | Deferred | Array of valid tokens |
| Per-Block Rate Limiting | #32 | Not started | Cooldown controls |
| HTTP Filter Block | #29 | Not started | Validate/transform requests |
| Redstone Detector Block | - | Not started | Publish events on redstone change |
| Relay Controller Block | - | Not started | Central hub GUI |
| Fabric Module | - | Disabled | Incomplete, indefinitely paused |

---

## Files Changed by Feature

### Response Variables (#76)

| Action | File |
|--------|------|
| Create | `variables/VariableStorage.java` |
| Create | `variables/JsonPathExtractor.java` |
| Create | `variables/ResponseCapture.java` |
| Create | `variables/VariableSubstitutor.java` |
| Modify | `HttpSenderBlockEntity.java` |
| Modify | `HttpClientImpl.java` |
| Modify | `HttpSenderSettingsScreen.java` |
| Modify | Translation files |

### Comparator Output (#77)

| Action | File |
|--------|------|
| Create | `http/api/HttpResult.java` |
| Modify | `HttpSenderBlockEntity.java` |
| Modify | `HttpSenderBlock.java` |
| Modify | `HttpReceiverBlock.java` |
| Modify | `IHttpClient.java` |
| Modify | `HttpClientImpl.java` |

### Request Templates (#81)

| Action | File |
|--------|------|
| Create | `templates/RequestTemplate.java` |
| Create | `templates/RequestTemplateRegistry.java` |
| Modify | `HttpSenderSettingsScreen.java` |
| Modify | Translation files |

---

## Testing Checklist

### Phase 2
- [ ] Response variables capture JSON values
- [ ] Variable substitution works in URL and parameters
- [ ] Comparator outputs correct signal for each status range
- [ ] Templates populate all fields correctly
- [ ] Debug overlay renders without performance issues

### Regression Tests
- [ ] Player detection returns correct JSON format
- [ ] Scene Sequencer steps execute in order
- [ ] Secret token validation works
- [ ] Rate limiting blocks excessive requests
- [ ] Eira Core events publish correctly
