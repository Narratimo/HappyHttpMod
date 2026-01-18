# Technical Architecture: Happy HTTP Mod

## Project Structure

```
HappyHttpMod/
├── common/                          # Shared code across all platforms
│   └── src/main/java/com/clapter/httpautomator/
│       ├── CommonClass.java         # Entry point, HTTP server lifecycle
│       ├── http/                    # HTTP server implementation
│       ├── block/                   # Block definitions
│       ├── blockentity/             # Block entity implementations
│       ├── registry/                # Block, item, entity registries
│       ├── network/                 # Network packets
│       ├── client/gui/              # Configuration GUI
│       ├── platform/                # Platform abstraction interfaces
│       └── utils/                   # Utility classes
├── forge/                           # Forge-specific implementation
├── fabric/                          # Fabric implementation (incomplete)
├── neoforge/                        # NeoForge implementation (skeleton)
└── docs/                            # Documentation
```

## Package Organization

**Base Package:** `com.clapter.httpautomator`

| Package | Purpose |
|---------|---------|
| `http/` | HTTP server, handlers, request processing |
| `block/` | Minecraft block definitions |
| `blockentity/` | Block entity state and persistence |
| `registry/` | Deferred registration for blocks, items, entities |
| `network/` | Client-server packet communication |
| `client/gui/` | Configuration screens |
| `platform/` | Platform abstraction layer |

## Core Components

### HTTP Module

```
IHttpServer (interface)
    └── HttpServerImpl
        ├── Uses: com.sun.net.httpserver.HttpServer
        ├── Port: Configurable (default 8080)
        └── Handler registration queue system

IHttpHandler (interface)
    └── HttpReceiverBlockHandler
        ├── Handles POST requests to custom endpoints
        └── Triggers onSignal() on subscribed block entities
```

### Block Module

```
HttpReceiverBlock (extends PoweredBlock, implements EntityBlock)
    ├── POWERED state property
    ├── Redstone signal emission (strength 15)
    └── GUI interaction (creative mode only)

HttpReceiverBlockEntity
    ├── Stores URL configuration
    ├── NBT persistence (load/saveAdditional)
    └── Creates/registers HTTP handler
```

### Registry Module

```
ModBlocks      → DeferredRegister<Block>
ModItems       → DeferredRegister<Item>
ModBlockEntities → DeferredRegister<BlockEntityType>
ModNetworkPackets → Packet registration
```

### Network Module

```
BasePacket (abstract)
    ├── CSyncHttpReceiverValuesPacket (server → client)
    │   └── Triggers: Opens settings screen
    └── SUpdateHttpReceiverValuesPacket (client → server)
        └── Triggers: Updates block entity, registers handler
```

### Platform Abstraction

```
Services (central locator)
    └── Uses ImplLoader (ServiceLoader pattern)

Interfaces:
├── IHttpServerConfig → Port configuration
├── IPlatformHelper → Platform detection
├── IBlockRegistry → Block registration
├── IItemRegistry → Item registration
├── IBlockEntitiesRegistry → Entity registration
└── IPacketHandler → Network packets
```

## Data Flow Diagrams

### HTTP Request Flow

```
External HTTP Client
        │
        │ POST /custom-endpoint
        ▼
┌─────────────────┐
│ HttpServerImpl  │ (port 8080)
└────────┬────────┘
         │ URL lookup in handlerMap
         ▼
┌─────────────────────────┐
│ HttpReceiverBlockHandler│
└────────┬────────────────┘
         │ onSignal() for each subscriber
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

### Configuration Flow

```
Player Right-clicks Block
        │
        ▼
┌─────────────────────────┐
│ HttpReceiverBlock.use() │
│ (creative mode check)   │
└────────┬────────────────┘
         │ CSyncHttpReceiverValuesPacket
         ▼
┌─────────────────────────────┐
│ HttpReceiverSettingsScreen  │
│ (Client GUI)                │
└────────┬────────────────────┘
         │ SUpdateHttpReceiverValuesPacket
         ▼
┌─────────────────────────────┐
│ HttpReceiverBlockEntity     │
│ updateValues()              │
└────────┬────────────────────┘
         │ Register handler
         ▼
┌─────────────────────────────┐
│ HttpServerImpl              │
│ registerHandler()           │
└─────────────────────────────┘
```

## Design Patterns

| Pattern | Usage |
|---------|-------|
| **Service Loader** | Platform implementation binding via Java SPI |
| **Factory** | BlockEntityFactory, ImplLoader |
| **Strategy** | IHttpHandler implementations |
| **Observer** | Block entities subscribe to HTTP handlers by URL |
| **Command** | Network packets as command objects |
| **Facade** | CommonClass centralizes platform complexity |
| **Abstract Factory** | Platform-specific registries |

## Platform-Specific Implementations

### Forge (`forge/`)

| Component | Implementation |
|-----------|----------------|
| Entry Point | `HttpAutomator.java` with @Mod annotation |
| Events | `MinecraftForge.EVENT_BUS` for server lifecycle |
| Config | `ForgeConfigSpec` for port configuration |
| Networking | `SimpleChannel` + `PacketDistributor` |
| Registry | `FMLJavaModLoadingContext.get().getModEventBus()` |

### NeoForge (`neoforge/`)

**Status:** Skeleton only

| Component | Status |
|-----------|--------|
| Entry Point | Basic constructor only |
| Events | Not implemented |
| Config | Not implemented |
| Networking | Not implemented |
| Registry | Not implemented |

### Fabric (`fabric/`)

**Status:** Incomplete

| Component | Status |
|-----------|--------|
| Entry Point | `ModInitializer` implemented |
| Events | Partial |
| Config | Not implemented |
| Networking | Not implemented |

## Key Files Reference

| File | Line Count | Purpose |
|------|------------|---------|
| `CommonClass.java` | ~80 | Entry point, server lifecycle |
| `HttpServerImpl.java` | ~100 | HTTP server implementation |
| `HttpReceiverBlockHandler.java` | ~60 | Request handling |
| `HttpReceiverBlock.java` | ~90 | Block behavior |
| `HttpReceiverBlockEntity.java` | ~80 | Block state persistence |
| `HttpReceiverSettingsScreen.java` | ~120 | Configuration GUI |

## Build System

- **Gradle** with multi-project structure
- **Java 17** toolchain
- **Mixin** 0.8.5 for bytecode modification
- **Platform-specific** build scripts per module
