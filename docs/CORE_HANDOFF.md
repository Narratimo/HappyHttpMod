# Eira Core - Development Handoff
## For Claude Code

**What is this?** A Minecraft NeoForge mod that provides shared APIs for the Eira ecosystem.

**Depends on:** Eira Server (backend) must be running for full functionality.

**Other mods depend on this:** Eira Relay, Eira NPC, and future mods.

---

## 1. What You're Building

Eira Core is a **library mod** that provides:

1. **Event Bus** - Cross-mod communication without direct dependencies
2. **Server Client** - WebSocket connection to Eira Server backend
3. **Local APIs** - Team, Player, Game state (cached from server)
4. **Event Forwarding** - Send Minecraft events to server for checkpoint evaluation

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           EIRA CORE ROLE                                     │
│                                                                              │
│   ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                  │
│   │ Eira Relay  │     │  Eira NPC   │     │ Future Mods │                  │
│   └──────┬──────┘     └──────┬──────┘     └──────┬──────┘                  │
│          │                   │                   │                          │
│          └───────────────────┼───────────────────┘                          │
│                              │                                               │
│                              ▼                                               │
│                    ┌─────────────────┐                                      │
│                    │    EIRA CORE    │                                      │
│                    │                 │                                      │
│                    │ • Event Bus     │                                      │
│                    │ • Server Client │────────► Eira Server (Backend)       │
│                    │ • Local Cache   │◄────────  via WebSocket              │
│                    │ • Player API    │                                      │
│                    └─────────────────┘                                      │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Public API (What Other Mods Use)

### 2.1 Entry Point

```java
// Other mods get the API like this:
EiraAPI eira = EiraAPI.get();

// Or safely check if available:
EiraAPI.ifPresent(api -> {
    // Use API
});
```

### 2.2 Event Bus

```java
// Any mod can publish events
eira.events().publish(new MyCustomEvent(data));

// Any mod can subscribe
eira.events().subscribe(SomeEvent.class, event -> {
    // Handle event
});
```

**You must define these event classes:**

```java
// Events that other mods will publish (you receive):
public record HttpReceivedEvent(String endpoint, String method, Map<String, String> params) {}
public record ExternalTriggerEvent(String source, String triggerId, Map<String, Object> data) {}
public record RedstoneChangeEvent(BlockPos pos, int oldStrength, int newStrength) {}
public record ConversationStartedEvent(String npcId, UUID playerId, String characterId) {}
public record ConversationEndedEvent(String npcId, UUID playerId, int messageCount) {}
public record SecretRevealedEvent(String npcId, UUID playerId, int level, int maxLevel) {}
public record AreaEnterEvent(UUID playerId, String areaId, BlockPos pos) {}
public record ItemObtainedEvent(UUID playerId, String itemId, int count) {}

// Events that you publish (other mods receive):
public record CheckpointCompletedEvent(String gameId, String checkpointId, UUID playerId, UUID teamId) {}
public record CheckpointUnlockedEvent(String gameId, String checkpointId) {}
public record GameCompletedEvent(String gameId, UUID teamId, int score, int timeSeconds) {}
public record ServerCommandEvent(String command, Map<String, Object> params) {}
```

### 2.3 Server Client (CheckIn Forwarding)

```java
// Forward events to server for checkpoint evaluation
eira.server().forwardEvent(event);

// Check connection
boolean connected = eira.server().isConnected();

// Get cached game state
GameState state = eira.server().getGameState(gameId);
CheckpointState cp = eira.server().getCheckpointState(gameId, checkpointId);
```

### 2.4 Team API (Read from Cache)

```java
// Teams are managed by server, this is read-only cache
Optional<Team> team = eira.teams().getTeamOf(player);
Team team = eira.teams().getById(teamId);
List<Player> members = team.getMembers();
```

### 2.5 Player API

```java
EiraPlayer player = eira.players().get(minecraftPlayer);
UUID minecraftUuid = player.getMinecraftUuid();
Optional<String> eiraPlayerId = player.getEiraPlayerId(); // From server
```

---

## 3. Server Communication

### 3.1 Connect on Startup

```java
// In mod initialization
String serverUrl = config.getServerUrl();  // e.g., "https://eira.example.com"
String apiKey = config.getApiKey();
String serverId = config.getServerId();

serverClient.connect(serverUrl, apiKey, serverId);
```

### 3.2 Forward Events to Server

When you receive events from other mods, forward relevant ones to server:

```java
// Subscribe to events that might trigger checkpoints
events.subscribe(SecretRevealedEvent.class, event -> {
    serverClient.sendTrigger(new TriggerPayload(
        "NPC_SECRET",
        findTeamId(event.playerId()),
        event.playerId().toString(),
        Map.of(
            "npcId", event.npcId(),
            "secretLevel", event.level()
        )
    ));
});

events.subscribe(AreaEnterEvent.class, event -> {
    serverClient.sendTrigger(new TriggerPayload(
        "MINECRAFT_AREA",
        findTeamId(event.playerId()),
        event.playerId().toString(),
        Map.of(
            "coordinates", List.of(event.pos().getX(), event.pos().getY(), event.pos().getZ())
        )
    ));
});
```

### 3.3 HTTP Request to Server

```
POST https://eira.example.com/api/v1/checkin
Headers:
  Authorization: Bearer <api_key>
  X-Server-ID: <server_id>
  Content-Type: application/json

Body:
{
  "triggerType": "NPC_SECRET",
  "playerId": "minecraft-uuid",
  "minecraftUuid": "minecraft-uuid",
  "triggerData": {
    "npcId": "oracle",
    "secretLevel": 2,
    "minecraftServer": "escape_room",
    "coordinates": [100, 65, 200]
  }
}

Response:
{
  "success": true,
  "checkpointCompleted": true,
  "isPartialTeamCheckIn": false,
  "checkpointsUnlocked": ["next_checkpoint"],
  "gameProgress": {
    "checkpointsCompleted": 3,
    "checkpointsTotal": 10,
    "score": 150
  }
}
```

### 3.4 WebSocket Messages (From Server)

Connect to: `wss://eira.example.com/api/v1/ws`

**Messages you receive:**

```json
// Checkpoint completed (by anyone in game)
{
  "event": "checkpoint.completed",
  "gameId": "escape_2025",
  "checkpointId": "find_key",
  "checkpointName": "Find the Key",
  "teamId": "team-uuid",
  "playerId": "player-uuid",
  "checkpointsUnlocked": ["open_door"]
}

// Partial check-in (for all_members rule)
{
  "event": "checkpoint.partial",
  "gameId": "escape_2025",
  "checkpointId": "gather_point",
  "teamId": "team-uuid",
  "membersCheckedIn": 2,
  "membersRequired": 4
}

// Game completed
{
  "event": "game.completed",
  "gameId": "escape_2025",
  "teamId": "team-uuid",
  "score": 500,
  "completionTimeSeconds": 2543
}

// Server command (execute in Minecraft)
{
  "event": "command",
  "command": "emit_redstone",
  "params": {
    "position": [100, 65, 200],
    "strength": 15,
    "duration": 40
  }
}

{
  "event": "command",
  "command": "broadcast",
  "params": {
    "teamId": "team-uuid",
    "message": "§aCheckpoint complete!",
    "type": "title"
  }
}
```

**When you receive these, publish local events:**

```java
void handleWebSocketMessage(JsonObject msg) {
    String event = msg.get("event").getAsString();
    
    switch (event) {
        case "checkpoint.completed":
            // Update local cache
            updateCheckpointCache(msg);
            // Publish for other mods
            events.publish(new CheckpointCompletedEvent(...));
            // Execute Minecraft feedback
            showTitleToTeam(msg.get("teamId"), "Checkpoint Complete!");
            break;
            
        case "command":
            // Publish for Relay to handle
            events.publish(new ServerCommandEvent(
                msg.get("command").getAsString(),
                parseParams(msg.get("params"))
            ));
            break;
    }
}
```

---

## 4. Configuration

```toml
# config/eira-core.toml

[server]
    # Eira Server URL (empty = offline mode)
    url = "https://eira.example.com"
    apiKey = "eira_sk_..."
    serverId = "escape_room_mc"
    
    # Reconnection
    reconnectDelayMs = 5000
    maxReconnectAttempts = 10

[cache]
    # How long to cache server data
    teamCacheTtlSeconds = 60
    gameStateCacheTtlSeconds = 30

[events]
    # Forward these event types to server
    forwardEventTypes = [
        "AREA_ENTER",
        "NPC_SECRET",
        "NPC_CONVERSATION",
        "REDSTONE",
        "ITEM_OBTAINED"
    ]
```

---

## 5. File Structure

```
eira-core/
├── src/main/java/org/eira/core/
│   ├── EiraCore.java              # Mod entry point
│   ├── EiraCoreConfig.java        # Configuration
│   │
│   ├── api/                       # PUBLIC API (other mods use this)
│   │   ├── EiraAPI.java           # Main entry point
│   │   ├── events/
│   │   │   ├── EiraEventBus.java
│   │   │   ├── EiraEvent.java
│   │   │   └── ... (all event classes)
│   │   ├── team/
│   │   │   ├── Team.java
│   │   │   └── TeamManager.java
│   │   ├── player/
│   │   │   ├── EiraPlayer.java
│   │   │   └── PlayerManager.java
│   │   └── server/
│   │       ├── ServerClient.java
│   │       ├── GameState.java
│   │       └── CheckpointState.java
│   │
│   └── impl/                      # INTERNAL (implementation)
│       ├── EiraAPIImpl.java
│       ├── SimpleEventBus.java
│       ├── WebSocketClient.java
│       ├── StateCache.java
│       └── EventForwarder.java
│
├── src/main/resources/
│   └── META-INF/
│       └── neoforge.mods.toml
│
└── build.gradle
```

---

## 6. Key Implementation Notes

### 6.1 Event Bus Must Be Thread-Safe

```java
public class SimpleEventBus implements EiraEventBus {
    private final Map<Class<?>, List<Consumer<?>>> subscribers = new ConcurrentHashMap<>();
    
    @Override
    public <T extends EiraEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
            .add(handler);
    }
    
    @Override
    public void publish(EiraEvent event) {
        List<Consumer<?>> handlers = subscribers.get(event.getClass());
        if (handlers != null) {
            for (Consumer handler : handlers) {
                try {
                    handler.accept(event);
                } catch (Exception e) {
                    LOGGER.error("Event handler error", e);
                    // Don't propagate - other handlers should still run
                }
            }
        }
    }
}
```

### 6.2 Server Connection is Async

```java
// Don't block the main thread
CompletableFuture.runAsync(() -> {
    webSocket.connect();
}).exceptionally(e -> {
    LOGGER.error("Connection failed, will retry", e);
    scheduleReconnect();
    return null;
});
```

### 6.3 Cache Server State

```java
// Don't hit server for every query
public class StateCache {
    private final Map<String, CachedValue<GameState>> gameStates = new ConcurrentHashMap<>();
    
    public GameState getGameState(String gameId) {
        CachedValue<GameState> cached = gameStates.get(gameId);
        if (cached != null && !cached.isExpired()) {
            return cached.getValue();
        }
        // Fetch from server or return stale if offline
        return fetchOrStale(gameId);
    }
}
```

### 6.4 Graceful Offline Mode

```java
// If server unavailable, still function with cached data
public void forwardEvent(EiraEvent event) {
    if (isConnected()) {
        sendToServer(event);
    } else {
        // Queue for later
        pendingEvents.add(event);
        LOGGER.warn("Server offline, queued event for later");
    }
}
```

---

## 7. Testing Checklist

- [ ] Event bus: publish/subscribe works across threads
- [ ] Server connection: connects, reconnects on failure
- [ ] Event forwarding: events sent to server with correct format
- [ ] WebSocket: receives messages, updates cache
- [ ] Server commands: publishes ServerCommandEvent for Relay
- [ ] Offline mode: functions with stale cache when disconnected
- [ ] API: other mods can access EiraAPI.get()

---

## 8. Dependencies

```gradle
// build.gradle
dependencies {
    // NeoForge
    implementation "net.neoforged:neoforge:${neoforge_version}"
    
    // HTTP client
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // WebSocket
    implementation 'org.java-websocket:Java-WebSocket:1.5.4'
    
    // JSON
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

---

## 9. Reference Documents

For full context, see:
- `DATA_MODEL.md` - Server data structures
- `EIRA_SERVER_SPEC.md` - Full server API
- `SYSTEM_SPECIFICATION.md` - Overall architecture
