# Feature Backlog: Eira Relay

## Priority Overview

| Priority | Category | Status |
|----------|----------|--------|
| P0 | Critical | ✅ All Complete |
| P1 | Tier 1 | Backlog |
| P2 | Tier 2 | Backlog |
| P3 | Tier 3 | Backlog |
| P4 | Platform | Backlog |

---

## P0: Critical (All Complete) ✅

### ~~Rename to "Eira Relay"~~ ✅ COMPLETE

**Status:** ✅ Complete (PR #8)

**Completed:**
- mod_id: `httpautomator` → `eirarelay`
- mod_name: `HttpAutomator` → `Eira Relay`
- Package: `com.clapter.httpautomator` → `no.eira.relay`
- Assets: `assets/httpautomator/` → `assets/eirarelay/`

---

### ~~Merge `dev` Branch to `main`~~ ✅ COMPLETE

**Status:** ✅ Complete (PR #1)

---

### ~~Complete NeoForge Implementation~~ ✅ COMPLETE

**Status:** ✅ Complete (PRs #2, #4, #5, #6, #7)

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

---

### #26 Visual Connection Cues

**Goal:** Show block status at a glance

**Features:**
- Resolved endpoint preview on block hover
- Parameter list visible in GUI
- Activity indicator (recent requests)
- Different visual states (active/inactive/error)

---

### #27 Inline Testing & Per-Block Logs

**Goal:** Debug without external tools

**Features:**
- "Test" button in block GUI
- Request log per block
- Last request details (time, params, response)
- Simulate incoming requests

---

### #31 Built-in Authentication Helpers

**Goal:** Secure endpoints without external middleware

**Features:**
- Bearer token validation
- Basic auth support
- API key parameter requirement
- Shared secret verification
- Per-block or global settings

---

## P2: Tier 2 - Builder Experience

### #25 Reusable Configuration Presets

**Goal:** Speed up map building

**Features:**
- Save current config as named preset
- Apply preset to new blocks
- Delete unused presets
- Export/import for adventure map distribution

---

### #28 Expand Receiver Output Modes

**Goal:** More automation options without external circuits

**Features:**
- Toggle mode (current)
- Timer mode (pulse for duration)
- Repeating clock mode
- Extendable timer (reset on new request)
- Configurable durations

---

### #32 Per-Block Rate Limiting

**Goal:** Prevent spam and DoS

**Features:**
- Configurable cooldown between triggers
- Max requests per time window
- Visual indicator when rate limited
- Per-block or global settings

---

## P3: Tier 3 - Advanced Mechanics

### #34 Receiver Payload Mapping

**Goal:** Dynamic redstone output based on request

**Features:**
- Map request content to redstone strength
- Different values for different parameters
- Range mapping (0-15)
- Comparator output support

---

### #35 Sender Outcomes

**Goal:** React to HTTP response

**Features:**
- Branch logic based on response
- Success/failure detection
- Response code handling
- Conditional redstone output

---

### #30 Scene Sequencer Block

**Goal:** Orchestrate complex automation

**Features:**
- Multi-step sequences with delays
- Branch based on conditions
- Loop or one-shot execution
- Trigger other blocks in sequence
- Visual sequence editor

---

## P4: Platform Support

### Fabric Support

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

Phase 3: Rename ✅ COMPLETE
└── PR#8: Rename to Eira Relay ✅

Phase 4: Feature Development (Backlog)
├── Port power modes from dev branch
├── Port global variables from dev branch
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
| 8 | refactor/rename-eira-relay | Full rename | ✅ Complete |

## Feature Backlog

| Item | Status | Notes |
|------|--------|-------|
| Power modes | Backlog | Port from dev branch |
| Global vars | Backlog | Port from dev branch |
| #37 Port helper | Backlog | Tier 1 |
| #26 Visual cues | Backlog | Tier 1 |
| #27 Testing | Backlog | Tier 1 |
| #31 Auth | Backlog | Tier 1 |
