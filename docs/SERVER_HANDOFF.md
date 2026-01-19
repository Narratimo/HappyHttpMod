# Eira Server - Development Handoff
## For Claude Code

**What is this?** The backend service that manages games, players, checkpoints, and check-ins.

**This is the authority** for all checkpoint state. Minecraft mods are clients.

**Stack:** Node.js + Fastify + PostgreSQL + Prisma + WebSocket

---

## 1. What You're Building

Eira Server is the **central authority** that:

1. **Stores** games, checkpoints, players, teams
2. **Receives** check-in triggers from all sources
3. **Evaluates** checkpoint completion rules
4. **Broadcasts** state changes via WebSocket
5. **Provides** REST API for queries and admin

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         EIRA SERVER ROLE                                     │
│                                                                              │
│   TRIGGER SOURCES                                                            │
│                                                                              │
│   ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐                       │
│   │QR Code  │  │ Sensor  │  │Minecraft│  │  Admin  │                       │
│   │ (HTTP)  │  │ (HTTP)  │  │ (Core)  │  │  (API)  │                       │
│   └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘                       │
│        │            │            │            │                              │
│        └────────────┴─────┬──────┴────────────┘                              │
│                           │                                                  │
│                           ▼                                                  │
│                  ┌─────────────────┐                                        │
│                  │  EIRA SERVER    │                                        │
│                  │                 │                                        │
│                  │ ┌─────────────┐ │                                        │
│                  │ │  Check-In   │ │                                        │
│                  │ │  Service    │ │                                        │
│                  │ └──────┬──────┘ │                                        │
│                  │        │        │                                        │
│                  │        ▼        │                                        │
│                  │ ┌─────────────┐ │      ┌─────────────┐                  │
│                  │ │  PostgreSQL │ │      │  WebSocket  │                  │
│                  │ │  Database   │ │      │  Broadcast  │                  │
│                  │ └─────────────┘ │      └──────┬──────┘                  │
│                  └─────────────────┘             │                          │
│                           │                      │                          │
│                           └──────────────────────┼──────────────────┐       │
│                                                  │                  │       │
│                                                  ▼                  ▼       │
│                                           ┌───────────┐     ┌───────────┐  │
│                                           │ MC Server │     │ Dashboard │  │
│                                           │ (Core)    │     │ (Admin)   │  │
│                                           └───────────┘     └───────────┘  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Core Concepts

### 2.1 Entity Hierarchy

```
CHECKPOINT (template)
  │
  │ used in many
  ▼
GAME
  │
  ├── has GAME_CHECKPOINTS (with rules)
  │
  ├── has TEAMS
  │      └── has TEAM_MEMBERS (players)
  │
  └── has GAME_PLAYERS
         └── linked to PLAYER
```

### 2.2 Key Relationships

- **Checkpoint** = reusable template (QR code, location, etc.)
- **Game** = event/competition using checkpoints
- **GameCheckpoint** = checkpoint configured for a specific game (order, rules, points)
- **Player** = person (may have Minecraft account)
- **Team** = group of players in a game
- **CheckIn** = record of player checking into a checkpoint

---

## 3. Database Schema

Use the Prisma schema from `/prisma/schema.prisma`. Key tables:

```prisma
model Checkpoint {
  id           String   @id
  code         String   @unique    // "LIBRARY_QR"
  triggerType  String              // QR_CODE, GEOLOCATION, etc.
  triggerConfig Json               // Type-specific config
}

model Game {
  id     String @id
  code   String @unique            // "ESCAPE_2025"
  status String                    // draft, active, completed
  config Json                      // Rules, scoring, etc.
}

model GameCheckpoint {
  id             String   @id
  gameId         String
  checkpointId   String
  orderIndex     Int                // Sequence order
  prerequisiteIds String[]          // Must complete these first
  checkInRule    Json               // any_one, all_members, etc.
  points         Int
}

model CheckIn {
  id                 String @id
  gameCheckpointId   String
  playerId           String
  teamId             String?
  completesCheckpoint Boolean        // False if partial
  isPartial          Boolean
}
```

---

## 4. API Endpoints

### 4.1 Check-In (Most Important!)

```typescript
// POST /api/v1/checkin
// This is the MAIN entry point for all triggers

interface CheckInRequest {
  // Identify checkpoint (one of these)
  checkpointId?: string;
  checkpointCode?: string;
  triggerId?: string;           // For QR codes
  
  // Identify player (one of these)
  playerId?: string;
  minecraftUuid?: string;
  
  // Trigger details
  triggerType: string;
  triggerData?: {
    enteredCode?: string;        // For CODE_ENTRY
    latitude?: number;           // For GEOLOCATION
    longitude?: number;
    coordinates?: [number, number, number];  // For MINECRAFT_*
    npcId?: string;              // For NPC_*
    secretLevel?: number;
  };
  
  // Optional
  gameId?: string;               // If player in multiple games
}

interface CheckInResponse {
  success: boolean;
  checkpointCompleted: boolean;
  isPartialTeamCheckIn: boolean;
  teamMembersCheckedIn?: number;
  teamMembersRequired?: number;
  checkpointsUnlocked: string[];
  gameProgress: {
    checkpointsCompleted: number;
    checkpointsTotal: number;
    score: number;
  };
  error?: string;
}
```

### 4.2 Simple QR Endpoint

```typescript
// GET /t/:checkpointCode?team=xxx
// Simplified endpoint for QR codes

// If no team, show team selection page
// If team provided, process check-in and show result page
```

### 4.3 Games API

```typescript
GET    /api/v1/games                    // List games
POST   /api/v1/games                    // Create game
GET    /api/v1/games/:id                // Get game
PATCH  /api/v1/games/:id                // Update game
GET    /api/v1/games/:id/checkpoints    // Get game's checkpoints
POST   /api/v1/games/:id/checkpoints    // Add checkpoint to game
GET    /api/v1/games/:id/teams          // Get teams
GET    /api/v1/games/:id/leaderboard    // Get leaderboard
```

### 4.4 Teams API

```typescript
GET    /api/v1/games/:gameId/teams      // List teams in game
POST   /api/v1/games/:gameId/teams      // Create team
GET    /api/v1/teams/:id                // Get team
POST   /api/v1/teams/:id/members        // Add member
DELETE /api/v1/teams/:id/members/:playerId  // Remove member
GET    /api/v1/teams/:id/progress       // Team's checkpoint progress
```

### 4.5 Checkpoints API

```typescript
GET    /api/v1/checkpoints              // List checkpoint templates
POST   /api/v1/checkpoints              // Create checkpoint
GET    /api/v1/checkpoints/:id          // Get checkpoint
PATCH  /api/v1/checkpoints/:id          // Update checkpoint
```

### 4.6 Players API

```typescript
GET    /api/v1/players/:id              // Get player
POST   /api/v1/players                  // Create player
PATCH  /api/v1/players/:id              // Update player
GET    /api/v1/players/:id/games        // Player's games
```

---

## 5. Check-In Service (Core Logic)

This is the most important service. See `/src/services/CheckInService.ts`.

### 5.1 Flow

```
1. Receive trigger
   └── Find checkpoint by code/id/triggerId

2. Find player
   └── By playerId or minecraftUuid

3. Find active game
   └── Game must be 'active', player must be in it

4. Validate check-in
   ├── Not already checked in
   ├── Prerequisites met
   ├── Time constraints OK
   ├── Access code correct (if required)
   └── Trigger validation (geolocation radius, etc.)

5. Apply check-in rule
   ├── any_one → complete immediately
   ├── all_members → track partial, complete when all done
   └── minimum(N) → track partial, complete when N reached

6. Record check-in
   └── Create CheckIn record

7. If completed:
   ├── Update score
   ├── Execute actions (webhooks)
   ├── Find newly unlocked checkpoints
   └── Check if game completed

8. Broadcast via WebSocket
   └── To game channel, team channel, server channel
```

### 5.2 Check-In Rules

```typescript
type CheckInRule = 
  | { type: 'any_one' }                    // Any member completes it
  | { type: 'all_members' }                // ALL must check in
  | { type: 'minimum', count: number }     // At least N members
  | { type: 'with_code', code: string }    // Must provide code
  | { type: 'specific_player', playerId: string }  // Only this person

// Example: all_members
if (rule.type === 'all_members') {
  // Get existing partial check-ins
  const existing = await prisma.checkIn.findMany({
    where: { gameCheckpointId, teamId, isPartial: true }
  });
  
  const checkedInIds = new Set(existing.map(c => c.playerId));
  checkedInIds.add(currentPlayerId);
  
  if (checkedInIds.size >= team.members.length) {
    // All members checked in!
    return { checkpointCompleted: true, isPartial: false };
  } else {
    return { 
      checkpointCompleted: false, 
      isPartial: true,
      membersCheckedIn: checkedInIds.size,
      membersRequired: team.members.length
    };
  }
}
```

### 5.3 Trigger Validation

```typescript
function validateTrigger(checkpoint, triggerData): boolean {
  switch (checkpoint.triggerType) {
    case 'GEOLOCATION':
      const distance = calculateDistance(
        checkpoint.triggerConfig.latitude,
        checkpoint.triggerConfig.longitude,
        triggerData.latitude,
        triggerData.longitude
      );
      return distance <= checkpoint.triggerConfig.radiusMeters;
      
    case 'CODE_ENTRY':
      return checkpoint.triggerConfig.validCodes
        .map(c => c.toUpperCase())
        .includes(triggerData.enteredCode?.toUpperCase());
        
    case 'MINECRAFT_AREA':
      const [x, y, z] = triggerData.coordinates;
      const [minX, minY, minZ] = checkpoint.triggerConfig.areaMin;
      const [maxX, maxY, maxZ] = checkpoint.triggerConfig.areaMax;
      return x >= minX && x <= maxX && 
             y >= minY && y <= maxY && 
             z >= minZ && z <= maxZ;
             
    case 'NPC_SECRET':
      return triggerData.secretLevel >= checkpoint.triggerConfig.minSecretLevel;
      
    default:
      return true; // QR_CODE, etc. - already validated by trigger receipt
  }
}
```

---

## 6. WebSocket

### 6.1 Connection

```typescript
// Client connects to: wss://server/api/v1/ws
// With headers:
//   Authorization: Bearer <api_key>
//   X-Server-ID: <minecraft_server_id>

interface WSMessage {
  event: string;
  [key: string]: any;
}
```

### 6.2 Subscribe to Channels

```typescript
// Client sends:
{ "action": "subscribe", "channel": "game:escape_2025" }
{ "action": "subscribe", "channel": "team:team-uuid" }
{ "action": "subscribe", "channel": "server:mc-server-id" }
```

### 6.3 Events Broadcast

```typescript
// Checkpoint completed
{
  "event": "checkpoint.completed",
  "gameId": "escape_2025",
  "checkpointId": "find_key",
  "checkpointName": "Find the Key",
  "teamId": "team-uuid",
  "playerId": "player-uuid",
  "checkpointsUnlocked": ["open_door"]
}

// Partial check-in (all_members rule)
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
  "completionTimeSeconds": 2543,
  "rank": 1
}

// Server command (for Minecraft to execute)
{
  "event": "command",
  "command": "emit_redstone",
  "params": {
    "position": [100, 65, 200],
    "strength": 15,
    "duration": 40
  }
}
```

---

## 7. Authentication

### 7.1 Minecraft Servers

```typescript
// Servers connect with API key
// Header: Authorization: Bearer eira_sk_...

// Validate on each request:
async function authenticateServer(request) {
  const apiKey = request.headers['authorization']?.replace('Bearer ', '');
  const serverId = request.headers['x-server-id'];
  
  const server = await prisma.minecraftServer.findFirst({
    where: { id: serverId, apiKey }
  });
  
  if (!server) throw new AuthError('Invalid credentials');
  
  // Update last seen
  await prisma.minecraftServer.update({
    where: { id: serverId },
    data: { lastSeen: new Date(), isOnline: true }
  });
  
  return server;
}
```

### 7.2 Admin Users

```typescript
// JWT for admin dashboard
// POST /api/v1/admin/login → returns JWT
// Include in requests: Authorization: Bearer <jwt>
```

### 7.3 QR Code Triggers (Public)

```typescript
// GET /t/:code?team=xxx
// This endpoint is PUBLIC (no auth)
// But team must exist and be in active game
```

---

## 8. Pending Commands

When server needs Minecraft to do something:

```typescript
// Create pending command
await prisma.pendingCommand.create({
  data: {
    serverId: 'mc-server-id',
    command: 'emit_redstone',
    params: { position: [100, 65, 200], strength: 15, duration: 40 }
  }
});

// Minecraft polls or receives via WebSocket
// When executed:
await prisma.pendingCommand.update({
  where: { id: commandId },
  data: { status: 'executed', executedAt: new Date() }
});
```

---

## 9. File Structure

```
eira-server/
├── src/
│   ├── index.ts                 # Entry point
│   ├── app.ts                   # Fastify app setup
│   │
│   ├── api/                     # Route handlers
│   │   ├── checkin.ts           # POST /api/v1/checkin
│   │   ├── games.ts
│   │   ├── teams.ts
│   │   ├── players.ts
│   │   ├── checkpoints.ts
│   │   └── admin.ts
│   │
│   ├── services/                # Business logic
│   │   ├── CheckInService.ts    # MAIN SERVICE
│   │   ├── GameService.ts
│   │   ├── TeamService.ts
│   │   ├── PlayerService.ts
│   │   └── WebhookService.ts
│   │
│   ├── websocket/
│   │   ├── index.ts
│   │   └── broadcaster.ts
│   │
│   ├── middleware/
│   │   ├── auth.ts
│   │   └── rateLimit.ts
│   │
│   └── utils/
│       ├── geo.ts               # Distance calculations
│       └── validation.ts
│
├── prisma/
│   ├── schema.prisma
│   └── migrations/
│
├── tests/
│   ├── checkin.test.ts
│   └── ...
│
├── package.json
├── tsconfig.json
└── docker-compose.yml
```

---

## 10. Configuration

```typescript
// src/config.ts
export const config = {
  port: parseInt(process.env.PORT || '3000'),
  
  database: {
    url: process.env.DATABASE_URL
  },
  
  auth: {
    jwtSecret: process.env.JWT_SECRET
  },
  
  rateLimit: {
    max: 100,
    windowMs: 60000
  },
  
  webhook: {
    timeout: 5000,
    retries: 3
  }
};
```

---

## 11. Quick Start

```bash
# Install dependencies
npm install

# Setup database
npx prisma migrate dev

# Seed with test data (optional)
npx prisma db seed

# Run development server
npm run dev

# Run tests
npm test
```

---

## 12. Testing Checklist

- [ ] POST /api/v1/checkin works for all trigger types
- [ ] Check-in rules work (any_one, all_members, minimum)
- [ ] Prerequisites prevent early check-in
- [ ] Partial check-ins tracked correctly
- [ ] WebSocket broadcasts on check-in
- [ ] Game completion detected
- [ ] Leaderboard updates
- [ ] QR endpoint (/t/:code) works
- [ ] Auth works for MC servers
- [ ] Rate limiting works

---

## 13. Key Files to Reference

- `/DATA_MODEL.md` - Full entity definitions
- `/prisma/schema.prisma` - Database schema
- `/src/services/CheckInService.ts` - Main check-in logic
