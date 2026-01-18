# Immediate Tasks: Eira Relay

> **Rebranding:** The mod is being renamed from "Happy HTTP" / "HttpAutomator" to "Eira Relay" (www.eira.no)

---

## PR Execution Plan

All work is done via small PRs. Each PR updates documentation.

| PR# | Branch | Description | Status | Deps |
|-----|--------|-------------|--------|------|
| 1 | merge dev→main | HTTP Sender (1.20.2), global params | ✅ Done | - |
| 2 | merge feenixnet→main | NeoForge 1.21 port | ✅ Done | #1 |
| 3 | merge docs→main | CLAUDE.md, doc/new/*.md | ✅ Done | #1,#2 |
| 4 | fix/neoforge-mixin-config | Fix wrong package name | ✅ Done | #2 |
| 5 | feature/neoforge-http-sender | **HTTP Sender for NeoForge 1.21.1** | ✅ Done | #4 |
| 6 | fix/handler-cleanup-on-remove | Fix memory leak (handler cleanup) | ✅ Done | #5 |
| 7 | fix/default-localhost-binding | Security: bind to localhost | ✅ Done | #5 |
| 8 | refactor/rename-eira-relay | Full mod rename to Eira Relay | Pending | #7 |

**Note:** PRs #8-11 from original plan (NeoForge events, networking, registry, config) were already implemented by feenixnet branch.

---

## Critical Priority

### 0. Rename Mod to "Eira Relay"

**Status:** Pending - PR #8 (Next task)

**Scope:** Rename all references from "Happy HTTP" / "HttpAutomator" to "Eira Relay"

**Code Changes Required:**

| Location | Current | New |
|----------|---------|-----|
| `gradle.properties` | `mod_id = httpautomator` | `mod_id = eirarelay` |
| `gradle.properties` | `mod_name = HttpAutomator` | `mod_name = Eira Relay` |
| Package name | `com.clapter.httpautomator` | `no.eira.relay` |
| Config file | `happyhttp-common.toml` | `eirarelay-common.toml` |
| Assets path | `assets/httpautomator/` | `assets/eirarelay/` |
| Mixin configs | `httpautomator` references | `eirarelay` references |
| Constants.java | `MOD_ID = "httpautomator"` | `MOD_ID = "eirarelay"` |

**Documentation Changes Required:**
- README.md - Update title, description, all references
- All doc/*.md files - Update mod name references
- doc/new/*.md files - Already updated with new name
- Code comments and Javadoc

**Asset Changes Required:**
- Rename `assets/httpautomator/` to `assets/eirarelay/`
- Update all blockstate/model JSON references
- Update lang files (en_us.json, etc.)

**Steps:**
1. Create branch `refactor/rename-eira-relay`
2. Update gradle.properties (mod_id, mod_name)
3. Rename package from `com.clapter.httpautomator` to `no.eira.relay`
4. Rename asset folders
5. Update all config file references
6. Update mixin configs
7. Update documentation
8. Test NeoForge build
9. Create PR

**Subtasks:**
- [ ] Update gradle.properties
- [ ] Rename Java packages
- [ ] Rename asset folders
- [ ] Update Constants.java
- [ ] Update mixin configs
- [ ] Update config file names
- [ ] Update README.md
- [ ] Update all doc/*.md files
- [ ] Update lang files
- [ ] Update blockstate/model JSONs
- [ ] Test NeoForge build

---

### ~~1. Merge `dev` Branch to `main`~~ ✅ COMPLETE

**Status:** ✅ Complete (PR #1)

Dev branch merged. Note: dev targets MC 1.20.2, main now uses NeoForge 1.21.1.
HTTP Sender was ported separately to NeoForge 1.21.1 (PR #5).

---

### ~~2. Fix NeoForge Mixin Config~~ ✅ COMPLETE

**Status:** ✅ Complete (PR #4)

**Fixed:**
- Renamed to `httpautomator.neoforge.mixins.json`
- Package set to `com.clapter.httpautomator.mixin`
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

## High Priority

### 4. Clean Up Unused Imports

**Status:** Low priority - only affects disabled modules (common/forge for MC 1.20.2)

**Files:**
- `common/.../block/HttpReceiverBlock.java` line 9 - unused `WorldLoadEvent`
- `forge/.../HttpAutomator.java` line 6 - unused `WorldLoadEvent`

---

### ~~5. Add Handler Cleanup on Block Removal~~ ✅ COMPLETE

**Status:** ✅ Complete (PR #6: fix/handler-cleanup-on-remove)

**Implementation:**
- Added `unregisterHandler(String url)` to `IHttpServer` interface
- Implemented in `HttpServerImpl` to remove from handlerMap and server
- Added `onRemove()` in `HttpReceiverBlock` and `HttpSenderBlock`

---

### ~~6. Default to Localhost Binding~~ ✅ COMPLETE

**Status:** ✅ Complete (PR #7: fix/default-localhost-binding)

**Implementation:**
- Changed `DEFAULT_BIND_ADDRESS` to `127.0.0.1` in `HttpServerImpl.java`

---

## Medium Priority

### 7. Add HTTP Method Enforcement

**Issue:** Receivers should only accept POST, but method not strictly enforced

**Location:** `HttpReceiverBlockHandler.java`

---

### 8. Add Unit Tests for HTTP Layer

**Missing:** No automated tests for HTTP server functionality

**Priority areas:**
- Handler registration
- Request routing
- Parameter parsing
- Response generation

---

### 9. Document Actual Config File Locations

**Issue:** Documentation mentions `happyhttp-common.toml` but code uses different paths
**Action:** Verify and document correct config file paths for each platform. 

---

### 10. Complete Fabric Implementation

**Missing:** Most core functionality

**Lower priority** than NeoForge due to user base

---

## Low Priority

### 11. Remove Commented-Out Code

**Location:** Various files have commented neighbor update code

---

### 12. Add Observability/Logging

**Recommendation:** Add structured logging for:
- HTTP server startup/shutdown
- Request handling
- Handler registration

---

## Task Dependencies (Updated)

```
Phase 1: Branch Merges ✅ COMPLETE
PR#1 (dev) → PR#2 (feenixnet) → PR#3 (docs)

Phase 2: NeoForge Implementation ✅ COMPLETE
PR#4 (mixin) → PR#5 (HTTP Sender port) → PR#6 (cleanup) → PR#7 (security)

Phase 3: Rename (Next)
PR#8 (Eira Relay rename)
```

## PR Summary

| PR# | Description | Status | Branch |
|-----|-------------|--------|--------|
| 1 | Merge dev | ✅ Complete | merge/dev-to-main |
| 2 | Merge feenixnet | ✅ Complete | merge/feenixnet-to-main |
| 3 | Merge docs | ✅ Complete | docs/add-claude-md-and-analysis-structure |
| 4 | Fix mixin config | ✅ Complete | fix/neoforge-mixin-config |
| 5 | Port HTTP Sender | ✅ Complete | feature/neoforge-http-sender |
| 6 | Handler cleanup | ✅ Complete | fix/handler-cleanup-on-remove |
| 7 | Localhost default | ✅ Complete | fix/default-localhost-binding |
| 8 | Eira Relay rename | Pending | refactor/rename-eira-relay |
