# User Stories: Eira Relay

> **Rebranding:** The mod is being renamed from "Happy HTTP" / "HttpAutomator" to "Eira Relay" (www.eira.no)

## Core Use Case Stories

### Smart Home Integration

**As an** adventure map creator,
**I want** to trigger smart home devices from Minecraft,
**so that** players can interact with the physical world through in-game actions.

**Acceptance Criteria:**
- HTTP Sender block sends requests to smart home APIs
- Can trigger lights, locks, speakers from redstone signals
- Configurable endpoint URLs for different devices

**Current Status:** Ready after `dev` branch merge (HTTP Sender implemented in dev)

---

### Server-wide Alerts

**As a** server administrator,
**I want** to send Discord notifications when in-game events happen,
**so that** players can be notified of raids, boss kills, or base intrusions.

**Acceptance Criteria:**
- HTTP Sender block can POST to Discord webhook URLs
- JSON payload with customizable message content
- Works with any webhook-compatible service (Slack, etc.)

**Current Status:** Ready after `dev` branch merge

---

### Redstone Puzzle Locks

**As an** adventure map builder,
**I want** doors that only open when the correct parameters are sent,
**so that** players must solve puzzles or find correct codes.

**Acceptance Criteria:**
- HTTP Receiver validates specific parameter combinations
- Different parameter values trigger different outcomes
- Can require multiple correct parameters

**Current Status:** Ready after `dev` branch merge (Parameter matching implemented in dev)

---

### QR-Powered Treasure Hunt

**As an** event organizer,
**I want** physical QR codes that trigger in-game actions,
**so that** I can create hybrid physical/digital scavenger hunts.

**Acceptance Criteria:**
- Scanning QR code sends POST to Minecraft receiver
- Different QR codes trigger different receivers
- Can use with real-world smart locks for physical rewards

**Current Status:** Works with basic implementation

---

### Educational Workshop

**As an** educator,
**I want** to teach HTTP fundamentals using Minecraft,
**so that** students learn web concepts in an engaging environment.

**Acceptance Criteria:**
- Visual feedback when HTTP requests are received
- Can demonstrate GET vs POST differences
- Shows parameter passing concepts

**Current Status:** Partially working - basic HTTP works

---

## Feature Request Stories

### #37 Port Binding Helper

**As a** first-time user,
**I want** clear diagnostics when the HTTP server fails to start,
**so that** I can quickly resolve port conflicts or network issues.

**Acceptance Criteria:**
- Server shows available network interfaces
- Warns if port is already in use
- Provides reachability test option
- Suggests fixes for common problems

---

### #26 Visual Connection Cues

**As a** map builder,
**I want** to see endpoint previews on receiver blocks,
**so that** I can quickly identify which blocks handle which endpoints.

**Acceptance Criteria:**
- Block shows resolved endpoint URL on hover
- Parameter list visible in GUI
- Activity indicator shows recent requests
- Different visual state for active/inactive

---

### #27 Inline Testing

**As a** developer,
**I want** to test HTTP requests directly from the block GUI,
**so that** I can verify my configuration without external tools.

**Acceptance Criteria:**
- "Test" button in receiver settings
- Shows request log per block
- Displays last request details (time, parameters)
- Can simulate incoming requests

---

### #31 Authentication Helpers

**As a** security-conscious user,
**I want** built-in authentication options,
**so that** I can protect my endpoints from unauthorized access.

**Acceptance Criteria:**
- Bearer token validation option
- Basic auth support
- API key parameter requirement
- Shared secret verification

---

### #25 Reusable Presets

**As a** map builder,
**I want** to save and reuse block configurations,
**so that** I can quickly set up multiple similar endpoints.

**Acceptance Criteria:**
- Save current config as named preset
- Apply preset to new blocks
- Delete unused presets
- Export/import for adventure map distribution

---

### #28 Output Modes

**As a** redstone engineer,
**I want** different power output patterns,
**so that** I can create complex automation without external circuits.

**Acceptance Criteria:**
- Toggle mode (current behavior)
- Timer mode with configurable duration
- Repeating clock mode
- Extendable timer (reset on new request)

---

### #32 Rate Limiting

**As a** server admin,
**I want** to limit requests per block,
**so that** I can prevent spam and DoS attacks.

**Acceptance Criteria:**
- Configurable cooldown between triggers
- Max requests per time window
- Visual indicator when rate limited
- Per-block settings

---

### #33 Status Indicators

**As a** player,
**I want** to see block status without opening GUI,
**so that** I can quickly check if automation is working.

**Acceptance Criteria:**
- In-world visual feedback
- Different colors/textures for states
- Shows connection status
- Indicates recent activity

---

### #30 Scene Sequencer

**As a** cinematic builder,
**I want** to trigger multi-step sequences,
**so that** I can create timed events and cutscenes.

**Acceptance Criteria:**
- Define sequence of actions with delays
- Branch based on conditions
- Loop or one-shot execution
- Trigger other receivers in sequence

---

### #29 HTTP Filter Block

**As an** advanced user,
**I want** to validate and transform requests,
**so that** I can add middleware logic without external servers.

**Acceptance Criteria:**
- Validate request format
- Transform parameters
- Route to different receivers
- Block invalid requests

---

## Platform Stories

### NeoForge Support

**As a** NeoForge user,
**I want** the mod to work on my modloader,
**so that** I can use it with my existing mod setup.

**Acceptance Criteria:**
- Full feature parity with Forge
- Proper event handling
- Network packets work
- Configuration loads correctly

**Current Status:** Skeleton only, major work needed

---

### Fabric Support

**As a** Fabric user,
**I want** the mod to work on my modloader,
**so that** I can use it in my Fabric-based server.

**Acceptance Criteria:**
- Full feature parity with Forge
- Fabric API integration
- Works with common Fabric mods

**Current Status:** Incomplete, basic structure exists

---

### Multi-Version Support

**As a** player on newer Minecraft versions,
**I want** the mod to support 1.21.x,
**so that** I can use it with the latest game version.

**Acceptance Criteria:**
- Works on Minecraft 1.21.x
- No breaking changes to existing worlds
- Configuration migrates cleanly

**Current Status:** Not started, requires version abstraction
