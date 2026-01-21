# Eira Relay API Reference

Complete API documentation for Eira Relay HTTP endpoints and integration.

## Table of Contents

- [HTTP Server Configuration](#http-server-configuration)
- [HTTP Endpoints](#http-endpoints)
- [Webhook Payload Formats](#webhook-payload-formats)
- [Event Type Constants](#event-type-constants)
- [Error Codes](#error-codes)
- [Authentication](#authentication)

---

## HTTP Server Configuration

### Server Binding

| Setting | Default | Description |
|---------|---------|-------------|
| `port` | `8080` | HTTP server port |
| `bind_address` | `127.0.0.1` | Bind address (localhost for security) |
| `rate_limit` | `0` | Requests per minute per IP (0 = disabled) |
| `cors_origins` | `""` | Comma-separated allowed origins |

Configuration file: `.minecraft/config/eirarelay-common.toml`

---

## HTTP Endpoints

### GET /status

Health check endpoint with uptime and trigger information.

**Request:**
```http
GET /status HTTP/1.1
Host: localhost:8080
```

**Response 200:**
```json
{
  "status": "online",
  "modId": "eirarelay",
  "modName": "Eira Relay",
  "version": "1.1.0",
  "uptimeSeconds": 3600,
  "uptimeFormatted": "1h 0m 0s",
  "server": {
    "port": 8080
  },
  "registeredTriggers": ["qr_entrance", "sensor_1"],
  "triggerCount": 2
}
```

---

### POST /trigger/{triggerId}

Named trigger endpoint for QR codes, sensors, and external systems.

**Request:**
```http
POST /trigger/qr_entrance HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "teamId": "550e8400-e29b-41d4-a716-446655440000",
  "playerId": "550e8400-e29b-41d4-a716-446655440001",
  "data": {
    "location": "entrance",
    "scanTime": "2026-01-19T12:00:00Z"
  }
}
```

**Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `teamId` | string | No | Team UUID |
| `playerId` | string | No | Player UUID |
| `data` | object | No | Custom payload data |

**Response 200:**
```json
{
  "success": true,
  "triggerId": "qr_entrance",
  "blocksTriggered": 2,
  "eventPublished": true,
  "teamId": "550e8400-e29b-41d4-a716-446655440000",
  "playerId": "550e8400-e29b-41d4-a716-446655440001"
}
```

**Response 400 (Missing trigger ID):**
```json
{
  "success": false,
  "error": "Missing trigger ID. Use /trigger/{triggerId}"
}
```

**Events Published:**
- `ExternalTriggerEvent("http-trigger", triggerId, data)`

---

### POST /redstone

Direct redstone emission at specified world coordinates.

**Request:**
```http
POST /redstone HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "x": 100,
  "y": 64,
  "z": 200,
  "strength": 15,
  "duration": 40
}
```

**Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `x` | integer | Yes | - | X coordinate |
| `y` | integer | Yes | - | Y coordinate |
| `z` | integer | Yes | - | Z coordinate |
| `strength` | integer | No | 15 | Signal strength (0-15) |
| `duration` | integer | No | 20 | Duration in ticks (20 ticks = 1 second) |

**Response 200:**
```json
{
  "success": true,
  "position": {
    "x": 100,
    "y": 64,
    "z": 200
  },
  "strength": 15,
  "durationTicks": 40
}
```

**Response 400 (Missing coordinates):**
```json
{
  "success": false,
  "error": "Missing coordinates (x, y, z required)"
}
```

**Response 503 (Server unavailable):**
```json
{
  "success": false,
  "error": "Server not available"
}
```

**Events Published:**
- `RedstoneChangeEvent(pos, 0, strength)` - when emission starts
- `RedstoneChangeEvent(pos, strength, 0)` - when emission ends

---

### POST /broadcast

Send messages to players in-game.

**Request:**
```http
POST /broadcast HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "message": "Welcome to the adventure!",
  "type": "title",
  "radius": 50,
  "position": [100, 64, 200]
}
```

**Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `message` | string | Yes | - | Message text to display |
| `type` | string | No | "chat" | Display type: `chat`, `title`, `actionbar` |
| `radius` | integer | No | - | Filter by distance from position (blocks) |
| `position` | array[3] | No | - | Center point `[x, y, z]` for radius filter |

**Message Types:**

| Type | Description |
|------|-------------|
| `chat` | Normal chat message |
| `title` | Large centered title text |
| `actionbar` | Text above hotbar |

**Response 200:**
```json
{
  "success": true,
  "message": "Welcome to the adventure!",
  "type": "title",
  "broadcast": true
}
```

**Response 400 (Invalid request):**
```json
{
  "success": false,
  "error": "Invalid request body. Expected: {\"message\": \"string\", \"type\": \"chat|title|actionbar\", \"radius\": int, \"position\": [x,y,z]}"
}
```

---

### POST /{custom-endpoint}

Custom endpoints configured via HTTP Receiver blocks in-game.

**Request:**
```http
POST /my-secret-door HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Authorization: Bearer your-secret-token

{
  "key": "value"
}
```

**Response 200 (JSON format):**
```json
{
  "status": "ok",
  "message": "Signal sent to 2 block(s)",
  "blocks": [
    {
      "x": 100,
      "y": 64,
      "z": 200,
      "player": {
        "uuid": "550e8400-e29b-41d4-a716-446655440000",
        "name": "Steve",
        "distance": 3.52
      }
    },
    {
      "x": 105,
      "y": 64,
      "z": 200
    }
  ]
}
```

**Block Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `x`, `y`, `z` | integer | Block coordinates |
| `player` | object | Present only if player detection enabled and player found |
| `player.uuid` | string | Player's Minecraft UUID |
| `player.name` | string | Player's display name |
| `player.distance` | number | Distance from block center (blocks) |

**Player Detection:**
- Enable in block GUI with configurable radius (1-64 blocks)
- Detects nearest player within radius when triggered
- Player info included in response and Eira Core events

**Response 401 (Unauthorized):**
```json
{
  "error": "Unauthorized"
}
```

**Response 308 (Redirect):**
Redirects to configured URL if global parameters don't match.

**Events Published:**
- `HttpReceivedEvent(endpoint, method, params)` - includes `triggeredBlocks` with player info

---

## Webhook Payload Formats

### HTTP Sender Block - Outgoing Requests

#### GET Requests

Parameters are sent as URL query string:

```
GET https://example.com/webhook?param1=value1&param2=value2
```

- Values are URL-encoded (UTF-8)
- No Content-Type header

#### POST Requests

Parameters are sent as JSON body:

```http
POST https://example.com/webhook HTTP/1.1
Content-Type: application/json

{
  "param1": "value1",
  "param2": "value2"
}
```

- All values are strings
- Uses Google Gson for JSON serialization

### Authentication Headers (Outgoing)

| Auth Type | Header Format |
|-----------|---------------|
| `NONE` | No authentication header |
| `BEARER` | `Authorization: Bearer {token}` |
| `BASIC` | `Authorization: Basic {base64(credentials)}` |
| `CUSTOM_HEADER` | `{headerName}: {headerValue}` |

### Retry Behavior

- **Max retries:** 3 attempts
- **Backoff:** Exponential (1s, 2s, 4s)
- **Success:** HTTP 2xx status codes

---

## Event Type Constants

These event types are used for eira-core integration. All events implement `EiraEvent` interface.

### Events Published by Eira Relay

| Event Class | Type String | Trigger |
|-------------|-------------|---------|
| `HttpReceivedEvent` | `HTTP_RECEIVED` | Any HTTP request to custom endpoint |
| `ExternalTriggerEvent` | `EXTERNAL_TRIGGER` | POST /trigger/{id} received |
| `RedstoneChangeEvent` | `REDSTONE_CHANGE` | Redstone emission starts/ends |

### Events Subscribed by Eira Relay

| Event Class | Type String | Action |
|-------------|-------------|--------|
| `CheckpointCompletedEvent` | `CHECKPOINT_COMPLETED` | Trigger configured webhooks |
| `ServerCommandEvent` | `SERVER_COMMAND` | Execute redstone/broadcast commands |

### Event Schemas

#### HttpReceivedEvent

```java
record HttpReceivedEvent(
    String endpoint,      // e.g., "/my-endpoint"
    String method,        // "POST"
    Map<String, Object> params  // Request parameters
) implements EiraEvent {}
```

#### ExternalTriggerEvent

```java
record ExternalTriggerEvent(
    String source,        // "http-trigger"
    String triggerId,     // e.g., "qr_entrance"
    Map<String, Object> data  // Custom payload
) implements EiraEvent {}
```

#### RedstoneChangeEvent

```java
record RedstoneChangeEvent(
    BlockPos pos,         // World position
    int oldStrength,      // Previous signal (0-15)
    int newStrength       // New signal (0-15)
) implements EiraEvent {}
```

#### CheckpointCompletedEvent

```java
record CheckpointCompletedEvent(
    String gameId,        // e.g., "escape_2025"
    String checkpointId,  // e.g., "find_key"
    UUID playerId,        // Minecraft player UUID
    UUID teamId           // Team UUID
) implements EiraEvent {}
```

#### ServerCommandEvent

```java
record ServerCommandEvent(
    String command,       // "emit_redstone", "broadcast", "send_webhook"
    Map<String, Object> params  // Command-specific parameters
) implements EiraEvent {}
```

### Event Bus Usage

```java
// Subscribe to events
EiraAPI.ifPresent(api -> {
    api.events().subscribe(HttpReceivedEvent.class, event -> {
        System.out.println("HTTP received: " + event.endpoint());
    });
});

// Publish events
EiraAPI.ifPresent(api -> {
    api.events().publish(new ExternalTriggerEvent(
        "http-trigger",
        "my-trigger",
        Map.of("key", "value")
    ));
});
```

---

## Error Codes

| HTTP Code | Meaning | When |
|-----------|---------|------|
| 200 | Success | Request processed successfully |
| 400 | Bad Request | Missing required parameters or malformed body |
| 401 | Unauthorized | Invalid or missing authentication token |
| 308 | Permanent Redirect | Global parameter mismatch (redirects to configured URL) |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Unexpected server error |
| 503 | Service Unavailable | Minecraft server not running/available |

---

## Authentication

### HTTP Receiver Block (Incoming Requests)

**Secret Token Authentication:**

1. Configure token in block GUI
2. Send token via Authorization header (preferred):
   ```http
   Authorization: Bearer your-secret-token
   ```
3. Or send as query parameter (fallback):
   ```
   POST /endpoint?token=your-secret-token
   ```

### HTTP Sender Block (Outgoing Requests)

Configure in block GUI:

| Auth Type | Configuration |
|-----------|---------------|
| `None` | No authentication |
| `Bearer` | Token value in auth field |
| `Basic` | `username:password` in auth field |
| `Custom Header` | Header name + Header value |

---

## Quick Reference

### Endpoints Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | /status | Health check |
| POST | /trigger/{id} | External triggers |
| POST | /redstone | Direct redstone control |
| POST | /broadcast | Player messaging |
| POST | /{custom} | Block-configured endpoints |

### Example: QR Code to Open Door

1. Configure HTTP Receiver block at `/secret/door` with token `abc123`
2. Connect to redstone mechanism
3. QR code links to:
   ```
   curl -X POST http://server:8080/secret/door \
     -H "Authorization: Bearer abc123"
   ```

### Example: Discord Notification on Checkpoint

1. Configure HTTP Sender block with Discord webhook URL
2. Set method to POST
3. Add `content` parameter: `"Player reached checkpoint!"`
4. Power with redstone when checkpoint triggered
