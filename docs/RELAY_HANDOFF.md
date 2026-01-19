# Eira Relay - Development Handoff
## For Claude Code

**What is this?** A Minecraft NeoForge mod that bridges physical world (HTTP) and Minecraft (redstone/events).

**Depends on:** Eira Core (must be present)

**Talks to:** Eira Server (backend) via Eira Core, or directly for some triggers

---

## 1. What You're Building

Eira Relay provides:

1. **HTTP Server** - Receives triggers from QR codes, sensors, external systems
2. **HTTP Client** - Sends webhooks when events happen
3. **Redstone Blocks** - Emit/detect redstone based on HTTP or server commands
4. **Event Bridge** - Translate HTTP ↔ Eira events

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           EIRA RELAY ROLE                                    │
│                                                                              │
│   PHYSICAL WORLD                    MINECRAFT                                │
│                                                                              │
│   ┌─────────┐                                                               │
│   │QR Code  │──┐                    ┌─────────────┐                         │
│   └─────────┘  │                    │             │                         │
│                │   HTTP POST        │ EIRA RELAY  │      ┌─────────────┐   │
│   ┌─────────┐  ├──────────────────► │             │ ────►│  Redstone   │   │
│   │ Sensor  │──┤   /trigger/xxx     │ HTTP Server │      │  Blocks     │   │
│   └─────────┘  │                    │             │      └─────────────┘   │
│                │                    │             │                         │
│   ┌─────────┐  │                    │             │      ┌─────────────┐   │
│   │ Button  │──┘                    │  Publishes  │ ────►│ Eira Core   │   │
│   └─────────┘                       │  Events     │      │ Event Bus   │   │
│                                     └─────────────┘      └──────┬──────┘   │
│                                                                  │          │
│   ┌─────────┐                       ┌─────────────┐              │          │
│   │ Lights  │◄──────────────────────│             │◄─────────────┘          │
│   └─────────┘      HTTP POST        │ HTTP Client │   ServerCommandEvent    │
│                    (webhook)        │             │   (from server)         │
│   ┌─────────┐                       │             │                         │
│   │ Doors   │◄──────────────────────│             │                         │
│   └─────────┘                       └─────────────┘                         │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Integration with Eira Core

### 2.1 Get Core API

```java
// In your mod initialization
EiraAPI eira = EiraAPI.get();
if (eira == null) {
    LOGGER.error("Eira Core not found! Relay requires Core.");
    return;
}
```

### 2.2 Publish Events (When HTTP Received)

When your HTTP server receives a request, publish events:

```java
// When POST /trigger/{triggerId} received
public void onTriggerReceived(String triggerId, Map<String, Object> data) {
    EiraAPI eira = EiraAPI.get();
    
    // Publish generic HTTP event
    eira.events().publish(new HttpReceivedEvent(
        "/trigger/" + triggerId,
        "POST",
        data
    ));
    
    // Publish trigger event (Core will forward to server)
    eira.events().publish(new ExternalTriggerEvent(
        "http",           // source
        triggerId,        // trigger ID
        data              // payload
    ));
}
```

### 2.3 Subscribe to Events (Execute Commands)

Subscribe to server commands that come through Core:

```java
// When server says "emit redstone", do it
eira.events().subscribe(ServerCommandEvent.class, event -> {
    switch (event.command()) {
        case "emit_redstone":
            handleEmitRedstone(event.params());
            break;
        case "send_webhook":
            handleSendWebhook(event.params());
            break;
    }
});

private void handleEmitRedstone(Map<String, Object> params) {
    int[] pos = (int[]) params.get("position");
    int strength = (int) params.get("strength");
    int duration = (int) params.getOrDefault("duration", 20);
    
    BlockPos blockPos = new BlockPos(pos[0], pos[1], pos[2]);
    emitRedstoneAt(blockPos, strength, duration);
}
```

### 2.4 Redstone Change → Event

When redstone changes near your detector blocks:

```java
// In your RedstoneDetectorBlock
@Override
public void neighborChanged(...) {
    int newStrength = level.getSignal(pos, direction);
    int oldStrength = getStoredStrength(pos);
    
    if (newStrength != oldStrength) {
        EiraAPI.get().events().publish(new RedstoneChangeEvent(
            pos, 
            oldStrength, 
            newStrength
        ));
        setStoredStrength(pos, newStrength);
    }
}
```

---

## 3. HTTP Server Specification

### 3.1 Endpoints to Implement

```yaml
# Trigger endpoint (main entry point for QR codes, sensors)
POST /trigger/{triggerId}
  Headers:
    X-Api-Key: string (optional, if auth enabled)
    Content-Type: application/json
  Body:
    {
      "teamId": "uuid",           # Optional - associate with team
      "playerId": "uuid",         # Optional - associate with player
      "data": { ... }             # Optional - custom payload
    }
  Response 200:
    {
      "success": true,
      "triggerId": "qr_entrance",
      "eventPublished": true
    }
  Response 401:
    { "error": "Invalid API key" }
  Response 429:
    { "error": "Rate limit exceeded", "retryAfter": 1000 }

# Direct redstone control
POST /redstone
  Body:
    {
      "x": 100, "y": 64, "z": 200,
      "strength": 15,
      "duration": 40,              # ticks
      "pattern": "constant"        # constant, pulse, pulse_3x
    }
  Response 200:
    { "success": true }

# Send message to nearby players
POST /broadcast
  Body:
    {
      "message": "Hello!",
      "type": "chat",              # chat, title, actionbar
      "radius": 50,                # blocks from spawn
      "position": [100, 64, 200]   # optional center
    }

# Status
GET /status
  Response:
    {
      "status": "online",
      "version": "1.0.0",
      "uptime": 3600,
      "triggers": [
        { "id": "qr_entrance", "lastTriggered": "2025-01-18T..." }
      ]
    }
```

### 3.2 Server Configuration

```toml
# config/eira-relay.toml

[http.server]
    enabled = true
    host = "0.0.0.0"
    port = 8080
    
    # Security
    requireAuth = false
    apiKeys = ["key1", "key2"]     # If requireAuth = true
    
    # Rate limiting
    rateLimitPerMinute = 100
    
    # CORS (for browser-based triggers)
    allowCors = true
    corsOrigins = ["*"]
```

### 3.3 Implementation Notes

```java
// Use a lightweight HTTP server (not Minecraft's network thread!)
// Recommended: Javalin, NanoHTTPD, or Undertow

import io.javalin.Javalin;

public class RelayHttpServer {
    private Javalin app;
    
    public void start(int port) {
        app = Javalin.create(config -> {
            config.showJavalinBanner = false;
        }).start(port);
        
        // Trigger endpoint
        app.post("/trigger/{triggerId}", ctx -> {
            String triggerId = ctx.pathParam("triggerId");
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            
            // Validate API key if required
            if (config.requireAuth && !validateApiKey(ctx.header("X-Api-Key"))) {
                ctx.status(401).json(Map.of("error", "Invalid API key"));
                return;
            }
            
            // Publish event (on main Minecraft thread!)
            MinecraftServer server = getMinecraftServer();
            server.execute(() -> {
                EiraAPI.get().events().publish(new ExternalTriggerEvent(
                    "http", triggerId, body
                ));
            });
            
            ctx.json(Map.of("success", true, "triggerId", triggerId));
        });
        
        // Status endpoint
        app.get("/status", ctx -> {
            ctx.json(Map.of(
                "status", "online",
                "version", MOD_VERSION
            ));
        });
    }
    
    public void stop() {
        if (app != null) app.stop();
    }
}
```

---

## 4. Blocks to Implement

### 4.1 HTTP Receiver Block

Emits redstone when HTTP trigger received.

```java
public class HttpReceiverBlock extends Block implements EntityBlock {
    // Block that emits redstone when triggered via HTTP
}

public class HttpReceiverBlockEntity extends BlockEntity {
    private String triggerId;        // Unique trigger ID
    private int signalStrength = 15;
    private int signalDuration = 20; // ticks
    private int cooldown = 40;       // ticks between triggers
    private long lastTriggered = 0;
    
    public void trigger() {
        if (System.currentTimeMillis() - lastTriggered < cooldown * 50) {
            return; // Still in cooldown
        }
        lastTriggered = System.currentTimeMillis();
        
        // Emit redstone for duration
        setEmitting(true);
        scheduleOff(signalDuration);
    }
}
```

**Configuration GUI:**
- Trigger ID (text field)
- Signal strength (slider 1-15)
- Signal duration (slider 1-100 ticks)
- Cooldown (slider 0-200 ticks)

### 4.2 HTTP Sender Block

Sends HTTP request when redstone activated.

```java
public class HttpSenderBlock extends Block implements EntityBlock {
    // Block that sends HTTP when receiving redstone
}

public class HttpSenderBlockEntity extends BlockEntity {
    private String targetUrl;
    private String method = "POST";
    private String bodyTemplate = "{}";
    private TriggerEdge triggerOn = TriggerEdge.RISING;
    
    public void onRedstoneChange(int oldStrength, int newStrength) {
        boolean shouldTrigger = switch (triggerOn) {
            case RISING -> oldStrength == 0 && newStrength > 0;
            case FALLING -> oldStrength > 0 && newStrength == 0;
            case BOTH -> oldStrength == 0 != (newStrength == 0);
        };
        
        if (shouldTrigger) {
            sendHttpRequest();
        }
    }
    
    private void sendHttpRequest() {
        // Run async - don't block game thread!
        CompletableFuture.runAsync(() -> {
            httpClient.send(targetUrl, method, bodyTemplate);
        });
    }
}
```

**Configuration GUI:**
- Target URL (text field)
- Method (dropdown: GET, POST)
- Body template (text area)
- Trigger on (dropdown: Rising, Falling, Both)

### 4.3 Relay Controller Block (Optional)

Central hub for managing multiple triggers.

---

## 5. Webhook Client (Outgoing HTTP)

### 5.1 Subscribe to Checkpoint Events

```java
// When checkpoints complete, send webhooks
eira.events().subscribe(CheckpointCompletedEvent.class, event -> {
    // Check if this checkpoint has webhook configured
    String webhookUrl = getWebhookForCheckpoint(event.checkpointId());
    if (webhookUrl != null) {
        sendWebhook(webhookUrl, Map.of(
            "event", "checkpoint.completed",
            "gameId", event.gameId(),
            "checkpointId", event.checkpointId(),
            "teamId", event.teamId()
        ));
    }
});
```

### 5.2 Webhook Configuration

```toml
# config/eira-relay.toml

[webhooks]
    # Global webhooks
    onCheckpointComplete = "http://lights.local/checkpoint"
    onGameComplete = "http://celebration.local/trigger"
    
    # Per-trigger webhooks
    [[webhooks.rules]]
        trigger = "qr_entrance"
        url = "http://door.local/unlock"
        method = "POST"
```

### 5.3 Retry Logic

```java
public class WebhookClient {
    private final OkHttpClient client;
    private final int maxRetries = 3;
    private final int retryDelayMs = 1000;
    
    public void send(String url, Map<String, Object> payload) {
        CompletableFuture.runAsync(() -> {
            for (int attempt = 0; attempt < maxRetries; attempt++) {
                try {
                    Response response = client.newCall(buildRequest(url, payload)).execute();
                    if (response.isSuccessful()) {
                        return;
                    }
                    LOGGER.warn("Webhook failed (attempt {}): {} {}", 
                        attempt + 1, response.code(), response.message());
                } catch (IOException e) {
                    LOGGER.warn("Webhook error (attempt {}): {}", attempt + 1, e.getMessage());
                }
                
                try { Thread.sleep(retryDelayMs); } catch (InterruptedException e) { break; }
            }
            LOGGER.error("Webhook failed after {} attempts: {}", maxRetries, url);
        });
    }
}
```

---

## 6. Redstone Patterns

### 6.1 Available Patterns

```java
public enum RedstonePattern {
    CONSTANT,    // Steady signal for full duration
    PULSE,       // Single short pulse (5 ticks)
    PULSE_3X,    // Three pulses
    FADE,        // Gradually decreases from 15 to 0
    SOS          // Morse code SOS pattern
}

public int getStrengthAtTick(RedstonePattern pattern, int tick, int maxTicks) {
    return switch (pattern) {
        case CONSTANT -> 15;
        case PULSE -> tick < 5 ? 15 : 0;
        case PULSE_3X -> (tick % 10) < 5 ? 15 : 0;
        case FADE -> Math.max(0, 15 - (tick * 15 / maxTicks));
        case SOS -> sosPattern(tick);
    };
}
```

### 6.2 Emit Redstone Programmatically

```java
// Called from ServerCommandEvent handler
public void emitRedstoneAt(BlockPos pos, int strength, int duration, RedstonePattern pattern) {
    // Create virtual redstone source
    RedstoneEmitter emitter = new RedstoneEmitter(pos, strength, duration, pattern);
    activeEmitters.add(emitter);
    
    // Update block
    level.updateNeighborsAt(pos, Blocks.REDSTONE_BLOCK);
}

// In tick handler
public void tick() {
    Iterator<RedstoneEmitter> it = activeEmitters.iterator();
    while (it.hasNext()) {
        RedstoneEmitter emitter = it.next();
        emitter.tick();
        
        if (emitter.isComplete()) {
            it.remove();
            level.updateNeighborsAt(emitter.pos, Blocks.AIR);
        }
    }
}
```

---

## 7. Event Classes You Need

These are defined by Core, but here's what you'll work with:

```java
// Events you PUBLISH:
record HttpReceivedEvent(String endpoint, String method, Map<String, Object> params) implements EiraEvent {}
record ExternalTriggerEvent(String source, String triggerId, Map<String, Object> data) implements EiraEvent {}
record RedstoneChangeEvent(BlockPos pos, int oldStrength, int newStrength) implements EiraEvent {}

// Events you SUBSCRIBE to:
record ServerCommandEvent(String command, Map<String, Object> params) implements EiraEvent {}
record CheckpointCompletedEvent(String gameId, String checkpointId, UUID playerId, UUID teamId) implements EiraEvent {}
```

---

## 8. File Structure

```
eira-relay/
├── src/main/java/org/eira/relay/
│   ├── EiraRelay.java              # Mod entry point
│   ├── EiraRelayConfig.java        # Configuration
│   │
│   ├── http/
│   │   ├── RelayHttpServer.java    # Incoming HTTP server
│   │   ├── WebhookClient.java      # Outgoing HTTP client
│   │   └── RateLimiter.java
│   │
│   ├── block/
│   │   ├── HttpReceiverBlock.java
│   │   ├── HttpReceiverBlockEntity.java
│   │   ├── HttpSenderBlock.java
│   │   ├── HttpSenderBlockEntity.java
│   │   └── RelayControllerBlock.java
│   │
│   ├── redstone/
│   │   ├── RedstoneEmitter.java
│   │   ├── RedstonePattern.java
│   │   └── RedstoneManager.java
│   │
│   └── integration/
│       └── CoreIntegration.java    # Eira Core event handling
│
├── src/main/resources/
│   ├── assets/eira-relay/
│   │   ├── blockstates/
│   │   ├── models/
│   │   └── textures/
│   └── META-INF/
│       └── neoforge.mods.toml
│
└── build.gradle
```

---

## 9. Testing Checklist

- [ ] HTTP server starts on configured port
- [ ] POST /trigger/{id} publishes ExternalTriggerEvent
- [ ] Rate limiting works
- [ ] API key auth works (when enabled)
- [ ] ServerCommandEvent → emits redstone
- [ ] HTTP Receiver block emits when triggered
- [ ] HTTP Sender block sends on redstone
- [ ] Webhooks retry on failure
- [ ] All patterns work (constant, pulse, etc.)
- [ ] Works with Eira Core present

---

## 10. Dependencies

```gradle
dependencies {
    // NeoForge
    implementation "net.neoforged:neoforge:${neoforge_version}"
    
    // Eira Core (required dependency)
    implementation project(":eira-core")
    // OR from maven: implementation "org.eira:eira-core:${eira_version}"
    
    // HTTP server (choose one)
    implementation 'io.javalin:javalin:5.6.3'
    // OR: implementation 'org.nanohttpd:nanohttpd:2.3.1'
    
    // HTTP client
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // JSON
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

---

## 11. Quick Reference

### Events You Publish
| Event | When |
|-------|------|
| `HttpReceivedEvent` | Any HTTP request received |
| `ExternalTriggerEvent` | POST /trigger/{id} received |
| `RedstoneChangeEvent` | Redstone detector sees change |

### Events You Subscribe To
| Event | Action |
|-------|--------|
| `ServerCommandEvent("emit_redstone", ...)` | Emit redstone at position |
| `ServerCommandEvent("send_webhook", ...)` | Send HTTP to URL |
| `CheckpointCompletedEvent` | Send configured webhooks |

### HTTP Endpoints
| Endpoint | Purpose |
|----------|---------|
| `POST /trigger/{id}` | Receive external triggers |
| `POST /redstone` | Direct redstone control |
| `GET /status` | Health check |
