# Technical Architecture: Eira Relay

## Project Structure

```
EiraRelay/ (currently HappyHttpMod/)
├── neoforge/                        # NeoForge 1.21.1 (ACTIVE - COMPLETE)
│   └── src/main/java/no/eira/relay/
│       ├── EiraRelay.java           # Mod entry point
│       ├── CommonClass.java         # HTTP server/client lifecycle
│       ├── Constants.java           # MOD_ID, MOD_NAME
│       ├── http/                    # HTTP server and client
│       │   ├── HttpServerImpl.java  # Embedded HTTP server (localhost:8080)
│       │   ├── HttpClientImpl.java  # HTTP client for GET/POST requests
│       │   └── api/                 # Interfaces (IHttpServer, IHttpClient)
│       ├── block/                   # Block definitions
│       │   ├── HttpReceiverBlock.java  # Receives HTTP → Redstone
│       │   └── HttpSenderBlock.java    # Redstone → Sends HTTP
│       ├── blockentity/             # Block entity implementations
│       │   ├── HttpReceiverBlockEntity.java
│       │   └── HttpSenderBlockEntity.java
│       ├── registry/                # Block, item, entity registries
│       ├── network/                 # Network packets
│       ├── client/gui/              # Configuration GUIs
│       ├── platform/                # Platform services
│       ├── enums/                   # HTTP methods (GET/POST)
│       └── utils/                   # JsonUtils, NBTConverter, QueryBuilder
├── common/                          # Shared code (DISABLED - MC 1.20.2)
├── forge/                           # Forge implementation (DISABLED - MC 1.20.2)
├── fabric/                          # Fabric implementation (incomplete)
└── doc/                             # Documentation
```

## Active Platform: NeoForge 1.21.1 ✅ COMPLETE

**Package:** `no.eira.relay`

| Package | Purpose | Status |
|---------|---------|--------|
| `http/` | HTTP server and client implementations | ✅ Complete |
| `block/` | HttpReceiverBlock, HttpSenderBlock | ✅ Complete |
| `blockentity/` | Block entity state and persistence | ✅ Complete |
| `registry/` | Deferred registration for blocks, items, entities | ✅ Complete |
| `network/` | Client-server packet communication | ✅ Complete |
| `client/gui/` | Configuration screens (Receiver + Sender) | ✅ Complete |
| `platform/` | Platform services and config | ✅ Complete |
| `enums/` | EnumHttpMethod (GET/POST) | ✅ Complete |
| `utils/` | JsonUtils, NBTConverter, QueryBuilder | ✅ Complete |

## Core Components

### HTTP Module ✅ COMPLETE

```
HTTP Server (for Receivers):
IHttpServer (interface)
    └── HttpServerImpl
        ├── Uses: com.sun.net.httpserver.HttpServer
        ├── Port: Configurable (default 8080)
        ├── Bind: localhost (127.0.0.1) for security ✅ Fixed
        ├── Handler registration with URL mapping
        ├── Handler cleanup on unregister ✅ Fixed (memory leak)
        └── unregisterHandler() method for block removal

HTTP Client (for Senders):
IHttpClient (interface)
    └── HttpClientImpl
        ├── Uses: java.net.http.HttpClient
        ├── Supports: GET and POST methods
        ├── JSON body for POST requests
        └── Query string for GET requests
```

### Block Module ✅ COMPLETE

```
HttpReceiverBlock (implements EntityBlock)
    ├── POWERED state property
    ├── Redstone signal emission (strength 15)
    ├── GUI interaction (creative mode only)
    └── Handler cleanup on removal ✅ Fixed

HttpReceiverBlockEntity
    ├── Stores URL configuration
    ├── NBT persistence
    └── Creates/registers HTTP handler

HttpSenderBlock (implements EntityBlock) ✅ Ported to NeoForge 1.21.1
    ├── LIT state property (shows active state)
    ├── Triggers HTTP request on redstone signal
    ├── GUI interaction (creative mode only)
    └── Handler cleanup on removal ✅ Fixed

HttpSenderBlockEntity ✅ Ported to NeoForge 1.21.1
    ├── Stores URL, parameters, HTTP method
    ├── NBT persistence (uses JSON for parameter map)
    └── Executes HTTP requests via HttpClientImpl
```

### Registry Module

```
ModBlocks      → receiver, sender (registered as "eirarelay:receiver", "eirarelay:sender")
ModItems       → Block items for creative tab
ModBlockEntities → httpReceiverBlockEntity, httpSenderBlockEntity
ModNetworkPackets → All network packets
```

### Network Module

```
BasePacket (abstract)
    ├── HTTP Receiver packets:
    │   ├── CSyncHttpReceiverValuesPacket (server → client)
    │   └── SUpdateHttpReceiverValuesPacket (client → server)
    │
    └── HTTP Sender packets:
        ├── CHttpSenderOpenGuiPacket (server → client)
        └── SUpdateHttpSenderValuesPacket (client → server)
```

## Data Flow Diagrams

### HTTP Receiver Flow (Incoming Webhook)

```
External HTTP Client
        │
        │ POST /custom-endpoint
        ▼
┌─────────────────┐
│ HttpServerImpl  │ (localhost:8080)
└────────┬────────┘
         │ URL lookup in handlerMap
         ▼
┌─────────────────────────┐
│ HttpReceiverBlockHandler│
└────────┬────────────────┘
         │ onSignal() on block entity
         ▼
┌─────────────────────────┐
│ HttpReceiverBlockEntity │
└────────┬────────────────┘
         │ Toggle POWERED state
         ▼
┌─────────────────────────┐
│ Redstone Signal (15)    │
└─────────────────────────┘
```

### HTTP Sender Flow (Outgoing Request)

```
Redstone Signal
        │
        ▼
┌─────────────────────────┐
│ HttpSenderBlock         │
│ neighborChanged()       │
└────────┬────────────────┘
         │ onPowered()
         ▼
┌─────────────────────────┐
│ HttpSenderBlockEntity   │
│ onPowered()             │
└────────┬────────────────┘
         │ GET or POST based on config
         ▼
┌─────────────────────────┐
│ HttpClientImpl          │
│ sendGet() / sendPost()  │
└────────┬────────────────┘
         │
         ▼
External HTTP Server
```

## Design Patterns

| Pattern | Usage |
|---------|-------|
| **Service Loader** | Platform implementation binding via Java SPI |
| **Factory** | BlockEntityFactory for block entity creation |
| **Strategy** | IHttpHandler implementations for request handling |
| **Observer** | Block entities subscribe to HTTP handlers by URL |
| **Command** | Network packets as command objects |
| **Facade** | CommonClass centralizes HTTP server/client lifecycle |

## NeoForge Implementation Details

| Component | Implementation |
|-----------|----------------|
| Entry Point | `EiraRelay.java` with @Mod annotation |
| Events | `NeoForge.EVENT_BUS` for server lifecycle |
| Config | `ModConfigSpec` for port configuration |
| Networking | Custom packet system with FriendlyByteBuf |
| Registry | DeferredRegister with IEventBus |
| Mixins | Client-side mixins for title screen |

## Security Considerations

| Feature | Implementation | Status |
|---------|----------------|--------|
| Localhost Binding | Server binds to 127.0.0.1 by default | ✅ Fixed (PR #7) |
| Creative Mode Only | GUIs only accessible in creative mode | ✅ Implemented |
| Handler Cleanup | Handlers unregistered when blocks removed | ✅ Fixed (PR #6) |
| No Authentication | Not implemented (future enhancement) | Backlog |

## Build System

- **Gradle 8.8** with multi-project structure
- **Java 21** toolchain (NeoForge 1.21.1 requirement)
- **NeoGradle 7.0.41** for NeoForge builds
- **Mixin** for bytecode modification

## Key Files Reference (NeoForge)

| File | Purpose |
|------|---------|
| `EiraRelay.java` | Mod entry point, event registration |
| `CommonClass.java` | HTTP server/client instances, lifecycle |
| `Constants.java` | MOD_ID = "eirarelay", MOD_NAME = "Eira Relay" |
| `HttpServerImpl.java` | HTTP server implementation |
| `HttpClientImpl.java` | HTTP client for GET/POST |
| `HttpReceiverBlock.java` | Receiver block behavior |
| `HttpSenderBlock.java` | Sender block behavior |
| `HttpReceiverBlockEntity.java` | Receiver state persistence |
| `HttpSenderBlockEntity.java` | Sender state persistence |
| `HttpReceiverSettingsScreen.java` | Receiver configuration GUI |
| `HttpSenderSettingsScreen.java` | Sender configuration GUI |
