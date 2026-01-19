# Eira Development Guide
## Which Document for Which Component

---

## Recommended Development Order

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DEVELOPMENT ORDER                                    │
│                                                                              │
│   PHASE 1: Foundation                                                        │
│   ═══════════════════                                                        │
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │  1. EIRA SERVER (Backend)                                            │   │
│   │     • Database schema                                                │   │
│   │     • Check-in service (core logic)                                  │   │
│   │     • REST API endpoints                                             │   │
│   │     • WebSocket broadcasting                                         │   │
│   │                                                                      │   │
│   │     WHY FIRST: Everything else depends on server API                 │   │
│   │     HANDOFF: handoff/SERVER_HANDOFF.md                               │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                     │                                        │
│                                     ▼                                        │
│   PHASE 2: Minecraft Client                                                  │
│   ═════════════════════════                                                  │
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │  2. EIRA CORE (MC Library Mod)                                       │   │
│   │     • Event bus (local cross-mod communication)                      │   │
│   │     • Server client (WebSocket + HTTP)                               │   │
│   │     • Event forwarding to server                                     │   │
│   │     • Local cache of server state                                    │   │
│   │                                                                      │   │
│   │     WHY SECOND: Other mods need this to communicate                  │   │
│   │     HANDOFF: handoff/CORE_HANDOFF.md                                 │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                     │                                        │
│                                     ▼                                        │
│   PHASE 3: Feature Mods (Can be parallel)                                   │
│   ═══════════════════════════════════════                                   │
│                                                                              │
│   ┌───────────────────────────┐     ┌───────────────────────────┐          │
│   │  3a. EIRA RELAY           │     │  3b. EIRA NPC             │          │
│   │      • HTTP server        │     │      • NPC entity         │          │
│   │      • Redstone blocks    │     │      • LLM integration    │          │
│   │      • Webhooks           │     │      • Character system   │          │
│   │                           │     │      • Secret revelation  │          │
│   │  HANDOFF:                 │     │                           │          │
│   │  handoff/RELAY_HANDOFF.md │     │  HANDOFF:                 │          │
│   └───────────────────────────┘     │  handoff/NPC_HANDOFF.md   │          │
│                                     └───────────────────────────┘          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Document Assignment

### For Server (Backend) Development

**Give Claude Code:**
```
1. handoff/SERVER_HANDOFF.md        ← START HERE
2. DATA_MODEL.md                    ← Entity definitions
3. prisma/schema.prisma             ← Database schema
4. src/services/CheckInService.ts   ← Reference implementation
```

**Key tasks:**
- Set up Fastify + Prisma + PostgreSQL
- Implement check-in service with all validation
- REST API endpoints
- WebSocket for real-time updates

---

### For Eira Core (MC Library) Development

**Give Claude Code:**
```
1. handoff/CORE_HANDOFF.md          ← START HERE
2. DATA_MODEL.md                    ← For understanding server data
```

**Key tasks:**
- Event bus for cross-mod communication
- WebSocket client connecting to server
- Forward relevant events to server
- Cache server state locally
- Execute commands received from server

**Needs:** Server API must be defined (but doesn't need to be fully implemented)

---

### For Eira Relay Development

**Give Claude Code:**
```
1. handoff/RELAY_HANDOFF.md         ← START HERE
```

**Key tasks:**
- HTTP server for triggers
- Redstone emitter blocks
- Webhook client
- Integration with Core event bus

**Needs:** Eira Core must exist (provides event bus)

---

### For Eira NPC Development

**Give Claude Code:**
```
1. handoff/NPC_HANDOFF.md           ← START HERE
```

**Key tasks:**
- NPC entity
- LLM provider integration
- Character JSON loading
- Secret revelation system
- Integration with Core event bus

**Needs:** Eira Core must exist (provides event bus)

---

## Interface Contract

All components agree on these **event classes** (defined in Eira Core):

```java
// Events published by Relay → consumed by Core
record HttpReceivedEvent(String endpoint, String method, Map<String, Object> params)
record ExternalTriggerEvent(String source, String triggerId, Map<String, Object> data)
record RedstoneChangeEvent(BlockPos pos, int oldStrength, int newStrength)

// Events published by NPC → consumed by Core
record ConversationStartedEvent(String npcId, UUID playerId, String characterId)
record ConversationEndedEvent(String npcId, UUID playerId, int messageCount)
record SecretRevealedEvent(String npcId, UUID playerId, int level, int maxLevel)

// Events published by Core (from server) → consumed by Relay, NPC
record CheckpointCompletedEvent(String gameId, String checkpointId, UUID playerId, UUID teamId)
record ServerCommandEvent(String command, Map<String, Object> params)
```

All components agree on this **server API**:

```
POST /api/v1/checkin    ← All triggers go here
GET  /api/v1/ws         ← WebSocket for updates
```

---

## Parallel Development Strategy

If you have multiple Claude Code instances:

```
Week 1:
├── Instance A: Server (backend)
│   └── Focus on: Check-in service, database, basic API
│
└── Instance B: Core (MC mod) 
    └── Focus on: Event bus, WebSocket client stub
    └── Can mock server responses

Week 2:
├── Instance A: Server (continued)
│   └── Focus on: WebSocket, admin API, webhooks
│
├── Instance B: Core (continued)
│   └── Focus on: Event forwarding, command handling
│
├── Instance C: Relay
│   └── Focus on: HTTP server, redstone blocks
│   └── Uses Core's event bus
│
└── Instance D: NPC
    └── Focus on: Entity, LLM integration
    └── Uses Core's event bus

Week 3:
└── Integration testing across all components
```

---

## Testing Integration

### Test Scenario: QR Code → Checkpoint → Redstone

```
1. QR code scanned (phone → Server)
   POST /api/v1/checkin { checkpointCode: "QR_123", ... }

2. Server evaluates, broadcasts:
   WS → { event: "checkpoint.completed", ... }

3. Core receives, publishes locally:
   CheckpointCompletedEvent

4. Relay receives, checks for redstone action:
   If configured, emits redstone at position

5. Physical door unlocks!
```

### Test Scenario: NPC Secret → Checkpoint

```
1. Player talks to NPC
   NPC publishes: ConversationStartedEvent

2. Core forwards to server:
   POST /api/v1/checkin { triggerType: "NPC_CONVERSATION", ... }

3. Player gets NPC to reveal secret
   NPC publishes: SecretRevealedEvent(level: 2)

4. Core forwards to server:
   POST /api/v1/checkin { triggerType: "NPC_SECRET", secretLevel: 2, ... }

5. Server evaluates, broadcasts:
   WS → { event: "checkpoint.completed", ... }

6. Core receives, shows title to player:
   "Checkpoint Complete!"
```

---

## File Summary

```
eira-ecosystem/
├── handoff/
│   ├── SERVER_HANDOFF.md    ← For backend development
│   ├── CORE_HANDOFF.md      ← For Eira Core mod
│   ├── RELAY_HANDOFF.md     ← For Eira Relay mod
│   └── NPC_HANDOFF.md       ← For Eira NPC mod
│
├── DATA_MODEL.md            ← Entity definitions (reference)
├── SYSTEM_SPECIFICATION.md  ← Full architecture (reference)
├── CHECKPOINT_ARCHITECTURE.md ← How checkpoints work (reference)
│
├── prisma/
│   └── schema.prisma        ← Database schema
│
├── src/services/
│   └── CheckInService.ts    ← Reference implementation
│
└── specs/                   ← Detailed specs (reference)
    ├── EIRA_CORE_SPEC.md
    ├── EIRA_RELAY_SPEC.md
    ├── EIRA_NPC_SPEC.md
    └── EIRA_SERVER_SPEC.md
```

---

## Quick Reference: What Each Component Does

| Component | Publishes | Subscribes To | Talks To |
|-----------|-----------|---------------|----------|
| **Server** | WebSocket events | HTTP requests | Database, Webhooks |
| **Core** | CheckpointCompletedEvent, ServerCommandEvent | All mod events | Server (HTTP/WS) |
| **Relay** | HttpReceivedEvent, ExternalTriggerEvent, RedstoneChangeEvent | ServerCommandEvent, CheckpointCompletedEvent | Core event bus |
| **NPC** | ConversationStartedEvent, SecretRevealedEvent | ExternalTriggerEvent, CheckpointCompletedEvent | Core event bus, LLM |
