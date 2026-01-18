# Feature Backlog: Eira Relay

> **Rebranding:** The mod is being renamed from "Happy HTTP" / "HttpAutomator" to "Eira Relay" (www.eira.no)

## Priority Overview

| Priority | Category | Items |
|----------|----------|-------|
| P0 | Critical | **Rename to Eira Relay**, Merge dev, Fix NeoForge |
| P1 | Tier 1 | Setup & Support features |
| P2 | Tier 2 | Builder Experience features |
| P3 | Tier 3 | Advanced Mechanics |
| P4 | Platform | Multi-version support |

---

## P0: Critical (Blocking)

### Rename to "Eira Relay"

**Status:** Pending - PR #8 (Next task)
**Impact:** Brand consistency, release blocker
**Effort:** Medium (many files, but mechanical changes)
**Website:** www.eira.no

**Scope:**
- Rename mod_id: `httpautomator` → `eirarelay`
- Rename mod_name: `HttpAutomator` → `Eira Relay`
- Rename package: `com.clapter.httpautomator` → `no.eira.relay`
- Rename assets: `assets/httpautomator/` → `assets/eirarelay/`
- Rename configs: `httpautomator-*.toml` → `eirarelay-*.toml`
- Update all documentation

See `doc/new/tasks.md` for detailed subtasks.

---

### ~~Merge `dev` Branch to `main`~~ ✅ COMPLETE

**Status:** ✅ Complete (PR #1)

Dev branch merged. HTTP Sender ported separately to NeoForge 1.21.1.

---

### ~~Complete NeoForge Implementation~~ ✅ COMPLETE

**Status:** ✅ Complete (PRs #2, #4, #5, #6, #7)

All tasks completed:
1. ✅ Fix mixin config package name (PR #4)
2. ✅ Server lifecycle events (feenixnet branch, PR #2)
3. ✅ Networking layer (feenixnet branch, PR #2)
4. ✅ Registry system (feenixnet branch, PR #2)
5. ✅ HTTP Sender port (PR #5)
6. ✅ Handler cleanup (PR #6)
7. ✅ Localhost binding (PR #7)

---

## P1: Tier 1 - Setup & Support

### #37 Port Binding Helper

**Goal:** Reduce support load for network setup issues

**Features:**
- Server diagnostics on startup
- IP selection UI in settings
- Port conflict detection with suggestions
- Reachability testing tool
- Clear error messages for common issues

**Acceptance Criteria:**
- [ ] Shows available network interfaces
- [ ] Warns on port conflict
- [ ] Provides fix suggestions
- [ ] Test button for external reachability

---

### #26 Visual Connection Cues

**Goal:** Show block status at a glance

**Features:**
- Resolved endpoint preview on block hover
- Parameter list visible in GUI
- Activity indicator (recent requests)
- Different visual states (active/inactive/error)

**Acceptance Criteria:**
- [ ] Endpoint URL shown on hover
- [ ] Block texture changes with state
- [ ] Last request timestamp visible
- [ ] Error state indicator

---

### #27 Inline Testing & Per-Block Logs

**Goal:** Debug without external tools

**Features:**
- "Test" button in block GUI
- Request log per block
- Last request details (time, params, response)
- Simulate incoming requests

**Acceptance Criteria:**
- [ ] Test button sends sample request
- [ ] Log shows last N requests
- [ ] Details include all parameters
- [ ] Clear log button

---

### #31 Built-in Authentication Helpers

**Goal:** Secure endpoints without external middleware

**Features:**
- Bearer token validation
- Basic auth support
- API key parameter requirement
- Shared secret verification
- Per-block or global settings

**Acceptance Criteria:**
- [ ] Token validation works
- [ ] Unauthorized requests rejected
- [ ] Multiple auth methods supported
- [ ] Clear error messages

---

## P2: Tier 2 - Builder Experience

### #25 Reusable Configuration Presets

**Goal:** Speed up map building

**Features:**
- Save current config as named preset
- Apply preset to new blocks
- Delete unused presets
- Export/import for adventure map distribution

**Acceptance Criteria:**
- [ ] Save preset from block GUI
- [ ] Load preset list
- [ ] Apply preset to block
- [ ] Export to file

---

### #28 Expand Receiver Output Modes

**Goal:** More automation options without external circuits

**Features:**
- Toggle mode (current)
- Timer mode (pulse for duration)
- Repeating clock mode
- Extendable timer (reset on new request)
- Configurable durations

**Acceptance Criteria:**
- [ ] Mode selection in GUI
- [ ] Timer works in ticks/seconds
- [ ] Clock generates repeating pulses
- [ ] Extendable resets properly

---

### #32 Per-Block Rate Limiting

**Goal:** Prevent spam and DoS

**Features:**
- Configurable cooldown between triggers
- Max requests per time window
- Visual indicator when rate limited
- Per-block or global settings

**Acceptance Criteria:**
- [ ] Cooldown enforced
- [ ] Excess requests dropped
- [ ] Block shows "rate limited" state
- [ ] Configurable limits

---

### #33 Compact Status Indicators

**Goal:** In-world feedback without GUI

**Features:**
- Visual block state changes
- Different colors/textures
- Shows connection status
- Recent activity indicator

**Acceptance Criteria:**
- [ ] Block texture changes
- [ ] Colors indicate status
- [ ] No GUI needed for quick check

---

### #36 Adventure Map Toolkit

**Goal:** Help new builders get started

**Features:**
- Example worlds with pre-configured setups
- Template configurations
- Documentation within game
- Sample automation circuits

**Acceptance Criteria:**
- [ ] Example world downloadable
- [ ] Templates for common use cases
- [ ] In-game help book/item

---

## P3: Tier 3 - Advanced Mechanics

### #34 Receiver Payload Mapping

**Goal:** Dynamic redstone output based on request

**Features:**
- Map request content to redstone strength
- Different values for different parameters
- Range mapping (0-15)
- Comparator output support

**Acceptance Criteria:**
- [ ] Parameter values map to signal strength
- [ ] Configurable mapping rules
- [ ] Works with comparators

---

### #35 Sender Outcomes

**Goal:** React to HTTP response

**Features:**
- Branch logic based on response
- Success/failure detection
- Response code handling
- Conditional redstone output

**Acceptance Criteria:**
- [ ] Different output for success/failure
- [ ] Response code accessible
- [ ] Timeout handling

---

### #30 Scene Sequencer Block

**Goal:** Orchestrate complex automation

**Features:**
- Multi-step sequences with delays
- Branch based on conditions
- Loop or one-shot execution
- Trigger other blocks in sequence
- Visual sequence editor

**Acceptance Criteria:**
- [ ] Define sequence steps
- [ ] Delays work correctly
- [ ] Loops work
- [ ] Can trigger receivers/senders

---

### #29 HTTP Filter Block

**Goal:** Middleware without external servers

**Features:**
- Validate request format
- Transform parameters
- Route to different receivers
- Block invalid requests
- Chain multiple filters

**Acceptance Criteria:**
- [ ] Validation rules work
- [ ] Can transform parameters
- [ ] Routing works
- [ ] Invalid requests blocked

---

## P4: Platform Support

### Forge 1.21.x Support

**Goal:** Support latest Minecraft version

**Tasks:**
- Update mappings
- Fix breaking API changes
- Test all features
- Update dependencies

---

### Complete Fabric Support

**Goal:** Support Fabric modloader

**Tasks:**
- Implement Fabric event handlers
- Port networking to Fabric API
- Implement Fabric config
- Test feature parity

---

### Multi-Version Build System

**Goal:** Single codebase, multiple versions

**Tasks:**
- Version abstraction layer
- Conditional compilation
- Automated multi-version builds
- Version-specific fixes

---

## Backlog Prioritization Criteria

| Factor | Weight | Description |
|--------|--------|-------------|
| User Impact | High | How many users benefit |
| Support Load | High | Reduces support requests |
| Dependencies | Medium | Enables other features |
| Effort | Medium | Implementation complexity |
| Risk | Low | Technical uncertainty |

---

## Implementation Order (Updated)

```
Phase 1: Branch Merges ✅ COMPLETE
├── PR#1: Merge dev branch (HTTP Sender for MC 1.20.2) ✅
├── PR#2: Merge feenixnet branch (NeoForge 1.21.1 port) ✅
└── PR#3: Merge docs branch (CLAUDE.md, analysis) ✅

Phase 2: NeoForge Implementation ✅ COMPLETE
├── PR#4: Fix NeoForge mixin config ✅
├── PR#5: Port HTTP Sender to NeoForge 1.21.1 ✅
├── PR#6: Handler cleanup on block remove ✅
└── PR#7: Default to localhost binding ✅

Phase 3: Rename (Next)
└── PR#8: Rename to Eira Relay (pending)

Phase 4: Feature Development (Backlog)
├── #37 Port binding helper
├── #26 Visual connection cues
├── #27 Inline testing
└── ... (Tier 1, 2, 3 features)
```

---

## PR Tracking

| PR# | Branch | Description | Status |
|-----|--------|-------------|--------|
| 1 | merge/dev-to-main | HTTP Sender (MC 1.20.2) | ✅ Complete |
| 2 | merge/feenixnet-to-main | NeoForge 1.21.1 port | ✅ Complete |
| 3 | docs/add-claude-md-and-analysis-structure | Documentation | ✅ Complete |
| 4 | fix/neoforge-mixin-config | Fix mixin package | ✅ Complete |
| 5 | feature/neoforge-http-sender | Port HTTP Sender | ✅ Complete |
| 6 | fix/handler-cleanup-on-remove | Memory leak fix | ✅ Complete |
| 7 | fix/default-localhost-binding | Security fix | ✅ Complete |
| 8 | refactor/rename-eira-relay | Full rename | Pending |

## Feature Backlog

| Item | Status | Notes |
|------|--------|-------|
| Power modes | Backlog | Port from dev branch |
| Global vars | Backlog | Port from dev branch |
| #37 Port helper | Backlog | Tier 1 |
| #26 Visual cues | Backlog | Tier 1 |
| #27 Testing | Backlog | Tier 1 |
| #31 Auth | Backlog | Tier 1 |
