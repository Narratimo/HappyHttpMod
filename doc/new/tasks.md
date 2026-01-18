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
| 6 | fix/unused-imports | Remove WorldLoadEvent imports | Pending | #5 |
| 7 | fix/handler-cleanup | Unregister handlers on block remove | Pending | #5 |
| 8 | fix/localhost-default | Security: default to 127.0.0.1 | Pending | #5 |
| 9 | refactor/rename-eira-relay | Full mod rename | Pending | #8 |

**Note:** PRs #8-11 from original plan (NeoForge events, networking, registry, config) were already implemented by feenixnet branch.

---

## Critical Priority

### 0. Rename Mod to "Eira Relay"

**Status:** New - Blocking for release

**Scope:** Rename all references from "Happy HTTP" / "HttpAutomator" to "Eira Relay"

**Code Changes Required:**

| Location | Current | New |
|----------|---------|-----|
| `gradle.properties` | `mod_id = httpautomator` | `mod_id = eirarelay` |
| `gradle.properties` | `mod_name = HttpAutomator` | `mod_name = Eira Relay` |
| Package name | `com.clapter.httpautomator` | `com.eira.relay` (or similar) |
| Config file | `happyhttp-common.toml` | `eirarelay-common.toml` |
| Assets path | `assets/happyhttp/` | `assets/eirarelay/` |
| Mixin configs | `httpautomator` references | `eirarelay` references |
| Constants.java | `MOD_ID = "httpautomator"` | `MOD_ID = "eirarelay"` |

**Documentation Changes Required:**
- README.md - Update title, description, all references
- All doc/*.md files - Update mod name references
- doc/new/*.md files - Already updated with new name
- Code comments and Javadoc

**Asset Changes Required:**
- Rename `assets/happyhttp/` to `assets/eirarelay/`
- Update all blockstate/model JSON references
- Update lang files (en_us.json, etc.)

**Steps:**
1. Create branch `refactor/rename-to-eira-relay`
2. Update gradle.properties (mod_id, mod_name)
3. Rename package from `com.clapter.httpautomator` to new package
4. Rename asset folders
5. Update all config file references
6. Update mixin configs
7. Update documentation
8. Test build on all platforms
9. Create PR

**Subtasks:**
- [ ] Update gradle.properties
- [ ] Rename Java packages
- [ ] Rename asset folders
- [ ] Update Constants.java
- [ ] Update mixin configs (common, forge, neoforge)
- [ ] Update config file names
- [ ] Update README.md
- [ ] Update all doc/*.md files
- [ ] Update lang files
- [ ] Update blockstate/model JSONs
- [ ] Test Forge build
- [ ] Test NeoForge build
- [ ] Test Fabric build

---

### 1. Merge `dev` Branch to `main`

**Status:** Blocking all other feature work

**What's in dev branch:**
- HTTP Sender Block (complete implementation)
- Global parameters support
- HTTP client for sending requests
- Enhanced GUIs with parameter editing
- Power modes (Toggle/Timer)
- JSON utilities
- Many bug fixes and improvements

**Files to merge:**
```
common/src/main/java/.../block/HttpSenderBlock.java
common/src/main/java/.../blockentity/HttpSenderBlockEntity.java
common/src/main/java/.../client/gui/HttpSenderSettingsScreen.java
common/src/main/java/.../client/gui/BaseBlockScreen.java
common/src/main/java/.../client/gui/widgets/EditBoxPair.java
common/src/main/java/.../client/gui/widgets/ScrollableWidget.java
common/src/main/java/.../http/HttpClientImpl.java
common/src/main/java/.../http/api/IHttpClient.java
common/src/main/java/.../enums/EnumHttpMethod.java
common/src/main/java/.../enums/EnumPoweredType.java
common/src/main/java/.../enums/EnumTimerUnit.java
common/src/main/java/.../platform/config/GlobalParam.java
common/src/main/java/.../utils/JsonUtils.java
common/src/main/java/.../utils/NBTConverter.java
common/src/main/java/.../utils/ParameterReader.java
common/src/main/java/.../utils/QueryBuilder.java
+ network packets, registry updates, assets
```

**Steps:**
1. Create PR from `dev` to `main`
2. Resolve any merge conflicts
3. Test both blocks work correctly
4. Merge and tag release

---

### 2. Fix NeoForge Mixin Config

**File:** `neoforge/src/main/resources/neoforge.neoforge.mixins.json`

**Issue:** Package name is wrong
```json
"package": "com.example.examplemod.mixin"  // WRONG
```

**Fix:**
```json
"package": "com.clapter.httpautomator.mixin"  // CORRECT
```

---

### 3. Complete NeoForge Implementation

**Missing components:**
- Server lifecycle event handlers (ServerStartingEvent, etc.)
- Network packet registration and handling
- HTTP server integration
- Configuration loading

**Reference:** Use Forge implementation as template

---

## High Priority

### 4. Clean Up Unused Imports

**Files:**
- `common/.../block/HttpReceiverBlock.java` line 9 - unused `WorldLoadEvent`
- `forge/.../HttpAutomator.java` line 6 - unused `WorldLoadEvent`

---

### 5. Add Handler Cleanup on Block Removal

**Issue:** HTTP handlers are not removed when blocks are destroyed

**Location:** `HttpReceiverBlock.onRemove()` and `HttpSenderBlock.onRemove()`

**Fix:** Call `httpServer.unregisterHandler(url)` when block is removed

---

### 6. Default to Localhost Binding

**Security Issue:** Default bind to `192.168.0.1` exposes server to LAN

**Recommendation:** Default to `127.0.0.1` for security, require explicit config for LAN

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

## Task Dependencies

```
Phase 1: Branch Merges
PR#1 (dev) → PR#2 (feenixnet) → PR#3 (docs)

Phase 2: Quick Fixes (parallel after PR#1)
PR#1 → PR#4 (mixin) ─┬→ PR#8 (events) → PR#9 (network) → PR#10 (registry) → PR#11 (config)
PR#1 → PR#5 (imports)│
PR#1 → PR#6 (cleanup)│
PR#1 → PR#7 (security)┘

Phase 3: Rename (after NeoForge complete)
PR#11 → PR#12 (Eira Relay rename)
```

## Estimated Effort by PR

| PR# | Description | Effort | Files |
|-----|-------------|--------|-------|
| 1 | Merge dev | Medium | ~108 files |
| 2 | Merge feenixnet | Medium | ~76 files |
| 3 | Merge docs | Low | ~8 files |
| 4 | Fix mixin config | Trivial | 1 file |
| 5 | Clean imports | Trivial | 2 files |
| 6 | Handler cleanup | Low | 2 files |
| 7 | Localhost default | Low | 1 file |
| 8 | NeoForge events | Medium | 1 file |
| 9 | NeoForge networking | High | 1+ files |
| 10 | NeoForge registry | Medium | 3 files |
| 11 | NeoForge config | Low | 1 file |
| 12 | Eira Relay rename | High | 50+ files |
