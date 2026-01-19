# Eira Relay - Implementation Roadmap

Based on: RELAY_HANDOFF.md, CORE_HANDOFF.md, SERVER_HANDOFF.md, ECOSYSTEM.md

---

## Current State (Already Implemented)

| Feature | Status | Notes |
|---------|--------|-------|
| HTTP Server | Done | Using `com.sun.net.httpserver` |
| HTTP Client | Done | POST/GET with headers |
| HTTP Receiver Block | Done | Emits redstone on trigger |
| HTTP Sender Block | Done | Sends HTTP on redstone |
| Secret Token Auth | Done | Bearer header or query param |
| Power Modes | Done | Switch/Timer |
| Parameter Editor | Done | Key/value pairs for JSON body |
| Auth Types | Done | None/Bearer/Basic/Custom |
| Translations | Done | English + Norwegian |

---

## Phase 1: Standalone Improvements (No Dependencies)

These can be implemented now, before Eira Core exists.

### 1.1 New HTTP Endpoints

| Endpoint | Method | Purpose | Priority |
|----------|--------|---------|----------|
| `/trigger/{triggerId}` | POST | Main trigger endpoint for QR codes, sensors | High |
| `/redstone` | POST | Direct redstone control at position | Medium |
| `/broadcast` | POST | Send message to nearby players | Medium |
| `/status` | GET | Health check with uptime, triggers list | Low |

**Trigger Endpoint Spec:**
```
POST /trigger/{triggerId}
Body: { "teamId": "uuid", "playerId": "uuid", "data": {...} }
Response: { "success": true, "triggerId": "qr_entrance", "eventPublished": true }
```

**Redstone Endpoint Spec:**
```
POST /redstone
Body: { "x": 100, "y": 64, "z": 200, "strength": 15, "duration": 40, "pattern": "constant" }
```

**Broadcast Endpoint Spec:**
```
POST /broadcast
Body: { "message": "Hello!", "type": "chat|title|actionbar", "radius": 50, "position": [x,y,z] }
```

### 1.2 Rate Limiting

Add rate limiting to prevent abuse:
- Configurable requests per minute per IP
- Return 429 Too Many Requests when exceeded
- Config option: `rateLimitPerMinute = 100`

### 1.3 Multiple API Keys

Expand auth to support multiple API keys:
```toml
[http.server]
    requireAuth = false
    apiKeys = ["key1", "key2", "key3"]
```

### 1.4 CORS Support

For browser-based triggers:
```toml
[http.server]
    allowCors = true
    corsOrigins = ["*"]
```

### 1.5 Redstone Patterns

Add pattern support to HTTP Receiver:

| Pattern | Description |
|---------|-------------|
| `CONSTANT` | Steady signal for full duration |
| `PULSE` | Single short pulse (5 ticks) |
| `PULSE_3X` | Three pulses |
| `FADE` | Gradually decreases from 15 to 0 |
| `SOS` | Morse code SOS pattern |

### 1.6 Webhook Retry Logic

Improve HTTP Sender with retry on failure:
- Max 3 retries
- 1 second delay between retries
- Log failures after all retries exhausted

---

## Phase 2: Eira Core Integration (Requires Core)

These require Eira Core mod to exist first.

### 2.1 Get Core API

```java
EiraAPI eira = EiraAPI.get();
if (eira == null) {
    LOGGER.warn("Eira Core not found. Running in standalone mode.");
    return;
}
```

### 2.2 Publish Events

When HTTP triggers are received, publish to event bus:

| Event | When Published |
|-------|----------------|
| `HttpReceivedEvent` | Any HTTP request received |
| `ExternalTriggerEvent` | POST /trigger/{id} received |
| `RedstoneChangeEvent` | Redstone detector block sees change |

```java
eira.events().publish(new ExternalTriggerEvent(
    "http",           // source
    triggerId,        // trigger ID
    requestData       // payload
));
```

### 2.3 Subscribe to Events

Listen for server commands from Core:

| Event | Action |
|-------|--------|
| `ServerCommandEvent("emit_redstone", ...)` | Emit redstone at position |
| `ServerCommandEvent("send_webhook", ...)` | Send HTTP to URL |
| `CheckpointCompletedEvent` | Send configured webhooks |

```java
eira.events().subscribe(ServerCommandEvent.class, event -> {
    switch (event.command()) {
        case "emit_redstone" -> handleEmitRedstone(event.params());
        case "send_webhook" -> handleSendWebhook(event.params());
    }
});
```

### 2.4 Redstone Detector Block (New)

Add block that publishes `RedstoneChangeEvent` when redstone changes:

```java
@Override
public void neighborChanged(...) {
    int newStrength = level.getSignal(pos, direction);
    int oldStrength = getStoredStrength(pos);

    if (newStrength != oldStrength) {
        EiraAPI.get().events().publish(new RedstoneChangeEvent(pos, oldStrength, newStrength));
        setStoredStrength(pos, newStrength);
    }
}
```

---

## Phase 3: Advanced Features

### 3.1 Webhook Configuration

Per-trigger and per-event webhooks:

```toml
[webhooks]
    onCheckpointComplete = "http://lights.local/checkpoint"
    onGameComplete = "http://celebration.local/trigger"

    [[webhooks.rules]]
        trigger = "qr_entrance"
        url = "http://door.local/unlock"
        method = "POST"
```

### 3.2 Relay Controller Block (Optional)

Central hub for managing multiple triggers in one GUI:
- List all registered triggers
- Configure webhooks per trigger
- View trigger history/stats

### 3.3 Trigger Edge Configuration

HTTP Sender trigger on rising/falling/both edge:

```java
public enum TriggerEdge {
    RISING,   // 0 -> 1+ only
    FALLING,  // 1+ -> 0 only
    BOTH      // Any change
}
```

---

## Priority Order

| Priority | Task | Effort |
|----------|------|--------|
| 1 | POST /trigger/{id} endpoint | Low |
| 2 | Rate limiting | Low |
| 3 | Redstone patterns | Medium |
| 4 | POST /redstone endpoint | Low |
| 5 | POST /broadcast endpoint | Medium |
| 6 | Webhook retry logic | Low |
| 7 | Multiple API keys | Low |
| 8 | CORS support | Low |
| 9 | GET /status endpoint | Low |
| 10 | Eira Core integration | High (when Core exists) |
| 11 | Redstone Detector Block | Medium |
| 12 | Webhook configuration | Medium |
| 13 | Relay Controller Block | High |

---

## Reference: Other Ecosystem Components

### Eira Core (separate mod)

Library mod providing:
- Event bus for cross-mod communication
- WebSocket client to Eira Server
- Team/Player APIs
- State caching

**Relay depends on Core for:** Event publishing/subscribing

### Eira Server (backend)

Node.js backend providing:
- Check-in service for checkpoint evaluation
- Database for games, teams, players
- WebSocket broadcasting
- REST API for admin

**Relay communicates via:** HTTP triggers, receives commands via Core

---

## Files That Need Changes

### Phase 1 (Standalone)

| File | Changes |
|------|---------|
| `http/HttpServerImpl.java` | Add new endpoints, rate limiting, CORS |
| `http/handlers/TriggerHandler.java` | New handler for /trigger/{id} |
| `http/handlers/RedstoneHandler.java` | New handler for /redstone |
| `http/handlers/BroadcastHandler.java` | New handler for /broadcast |
| `http/handlers/StatusHandler.java` | New handler for /status |
| `http/HttpClientImpl.java` | Add retry logic |
| `enums/EnumRedstonePattern.java` | New enum for patterns |
| `blockentity/HttpReceiverBlockEntity.java` | Add pattern support |
| `client/gui/HttpReceiverSettingsScreen.java` | Add pattern selector |

### Phase 2 (Core Integration)

| File | Changes |
|------|---------|
| `integration/CoreIntegration.java` | New - handles Core API |
| `EiraRelay.java` | Check for Core, register event handlers |
| `block/RedstoneDetectorBlock.java` | New block |
| `blockentity/RedstoneDetectorBlockEntity.java` | New block entity |

---

## Testing Checklist

### Phase 1
- [ ] POST /trigger/{id} receives trigger, emits redstone
- [ ] Rate limiting returns 429 when exceeded
- [ ] Redstone patterns work (constant, pulse, fade, etc.)
- [ ] POST /redstone emits at specified position
- [ ] POST /broadcast sends message to players
- [ ] Webhook retries on failure

### Phase 2
- [ ] Core integration works when Core present
- [ ] Events published to Core event bus
- [ ] ServerCommandEvent triggers redstone
- [ ] Redstone detector publishes change events
- [ ] Works in standalone mode when Core absent
