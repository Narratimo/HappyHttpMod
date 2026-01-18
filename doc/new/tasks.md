# Immediate Tasks: Eira Relay

---

## PR Execution Plan - All Complete

All critical work has been completed via PRs.

| PR# | Branch | Description | Status |
|-----|--------|-------------|--------|
| 1 | merge/dev-to-main | HTTP Sender (1.20.2), global params | ✅ Complete |
| 2 | merge/feenixnet-to-main | NeoForge 1.21 port | ✅ Complete |
| 3 | docs/add-claude-md-and-analysis-structure | CLAUDE.md, doc/new/*.md | ✅ Complete |
| 4 | fix/neoforge-mixin-config | Fix wrong package name | ✅ Complete |
| 5 | feature/neoforge-http-sender | **HTTP Sender for NeoForge 1.21.1** | ✅ Complete |
| 6 | fix/handler-cleanup-on-remove | Fix memory leak (handler cleanup) | ✅ Complete |
| 7 | fix/default-localhost-binding | Security: bind to localhost | ✅ Complete |
| 8 | refactor/rename-eira-relay | Full mod rename to Eira Relay | ✅ Complete |
| 9 | feature/power-modes | **Power modes (Switch/Timer)** | ✅ Complete |
| 55 | refactor/rename-common-forge-fabric-packages | Rename packages to no.eira.relay | ✅ Complete |
| 56 | feature/port-modules-mc-1.21.1 | Multi-version support | ✅ Complete |
| 57 | feature/forge-http-sender | **HTTP Sender for forge/common** | ✅ Complete |
| 58 | feature/forge-power-modes | **Power modes for forge/common** | ✅ Complete |
| 59 | fix/common-translations | Fix translation file for forge | ✅ Complete |

---

## Completed Tasks

### ~~0. Rename Mod to "Eira Relay"~~ ✅ COMPLETE

**Status:** ✅ Complete (PR #8)

**Completed Changes:**

| Location | Old | New |
|----------|-----|-----|
| `gradle.properties` | `mod_id = httpautomator` | `mod_id = eirarelay` |
| `gradle.properties` | `mod_name = HttpAutomator` | `mod_name = Eira Relay` |
| Package name | `com.clapter.httpautomator` | `no.eira.relay` |
| Assets path | `assets/httpautomator/` | `assets/eirarelay/` |
| Mixin configs | `httpautomator` references | `eirarelay` references |
| Constants.java | `MOD_ID = "httpautomator"` | `MOD_ID = "eirarelay"` |
| Main class | `HttpAutomator.java` | `EiraRelay.java` |

**Build verified:** `Eira Relay-neoforge-1.21.1-1.1.0.jar`

---

### ~~1. Merge `dev` Branch to `main`~~ ✅ COMPLETE

**Status:** ✅ Complete (PR #1)

Dev branch merged. Note: dev targets MC 1.20.2, main now uses NeoForge 1.21.1.
HTTP Sender was ported separately to NeoForge 1.21.1 (PR #5).

---

### ~~2. Fix NeoForge Mixin Config~~ ✅ COMPLETE

**Status:** ✅ Complete (PR #4)

**Fixed:**
- Renamed to `eirarelay.neoforge.mixins.json`
- Package set to `no.eira.relay.mixin`
- Compatibility level updated to JAVA_21

---

### ~~3. Complete NeoForge Implementation~~ ✅ COMPLETE

**Status:** ✅ Complete (PRs #2, #4, #5, #6, #7)

All components implemented:
- ✅ Server lifecycle event handlers (feenixnet branch, PR #2)
- ✅ Network packet registration and handling (feenixnet branch, PR #2)
- ✅ HTTP server integration (feenixnet branch, PR #2)
- ✅ HTTP Sender Block (PR #5)
- ✅ Handler cleanup (PR #6)
- ✅ Localhost binding (PR #7)

---

### ~~4. Add Handler Cleanup on Block Removal~~ ✅ COMPLETE

**Status:** ✅ Complete (PR #6: fix/handler-cleanup-on-remove)

**Implementation:**
- Added `unregisterHandler(String url)` to `IHttpServer` interface
- Implemented in `HttpServerImpl` to remove from handlerMap and server
- Added `onRemove()` in `HttpReceiverBlock` and `HttpSenderBlock`

---

### ~~5. Default to Localhost Binding~~ ✅ COMPLETE

**Status:** ✅ Complete (PR #7: fix/default-localhost-binding)

**Implementation:**
- Changed `DEFAULT_BIND_ADDRESS` to `127.0.0.1` in `HttpServerImpl.java`

---

## Next Immediate Action

### Merge to Main

**Create PR:** https://github.com/Narratimo/HappyHttpMod/compare/main...refactor/rename-eira-relay?expand=1

The `refactor/rename-eira-relay` branch contains all changes from PRs #4-8 and needs to be merged to `main`.

---

## Ready for Review

### PR #9: Port Power Modes from Dev Branch

**Status:** ✅ Ready for PR
**Branch:** `feature/power-modes`
**Create PR:** https://github.com/Narratimo/HappyHttpMod/pull/new/feature/power-modes

**Implemented:**
- ✅ `EnumPoweredType.java` - Toggle (SWITCH) vs Timer modes
- ✅ `EnumTimerUnit.java` - Ticks or Seconds options
- ✅ `HttpReceiverBlockEntity.java` - Timer tracking with tick() method
- ✅ `HttpReceiverBlock.java` - Ticker, switchSignal(), setPowered() methods
- ✅ `HttpReceiverSettingsScreen.java` - Power mode GUI controls
- ✅ `en_us.json` - Translations for power mode labels

**Behavior:**
- **SWITCH mode:** Block toggles on/off on each HTTP signal (original behavior)
- **TIMER mode:** Block powers on for configured duration (ticks or seconds)

---

## Remaining Work (Backlog)

### Future Features

| Feature | Priority | Notes |
|---------|----------|-------|
| ~~Power modes (Toggle/Timer)~~ | ✅ **Ready for PR** | PR #9 |
| Global variables | Medium | Port from dev branch |
| Port binding helper | Low | Tier 1 feature |
| Visual connection cues | Low | Tier 1 feature |
| Inline testing | Low | Tier 1 feature |
| Authentication helpers | Low | Tier 1 feature |

---

## PR Summary

```
All 8 PRs Complete:

PR#1 (dev merge) ─────────┐
                          ├── PR#4 (mixin) ─── PR#5 (HTTP Sender) ─── PR#6 (cleanup) ─── PR#7 (security) ─── PR#8 (rename)
PR#2 (feenixnet merge) ───┤
                          │
PR#3 (docs merge) ────────┘
```
